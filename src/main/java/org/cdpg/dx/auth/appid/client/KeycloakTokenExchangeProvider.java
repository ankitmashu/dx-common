package org.cdpg.dx.auth.appid.client;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Obtains Keycloak tokens for authenticating outbound gRPC calls.
 *
 * <p>Two modes:
 * <ul>
 *   <li>{@link #getServiceToken()} — client_credentials grant. Used when no user JWT is available
 *       (AppId auth path). The resulting token carries {@code azp = clientId} so dx-controlplane's
 *       {@code ServiceAuthInterceptor} can identify the calling service.
 *   <li>{@link #exchange(String, String)} — RFC 8693 token exchange. Used when a user JWT is
 *       available (JWT auth path). The resulting token carries the user's {@code sub} AND
 *       {@code azp = clientId}, giving dx-controlplane both service and user identity.
 * </ul>
 *
 * <p>Config keys (read by the calling verticle and passed to the constructor):
 * <ul>
 *   <li>{@code keycloakTokenUrl} — Keycloak token endpoint, e.g.
 *       {@code http://keycloak:8080/realms/dx/protocol/openid-connect/token}
 *   <li>{@code grpcClientId} — this service's Keycloak client_id, e.g. {@code svc-dx-dataplane}
 *   <li>{@code grpcClientSecret} — this service's Keycloak client_secret
 * </ul>
 */
public class KeycloakTokenExchangeProvider {

  private static final Logger LOGGER = LogManager.getLogger(KeycloakTokenExchangeProvider.class);

  private static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
  private static final String GRANT_TOKEN_EXCHANGE =
      "urn:ietf:params:oauth:grant-type:token-exchange";
  private static final String TOKEN_TYPE_ACCESS =
      "urn:ietf:params:oauth:token-type:access_token";
  private static final String GRPC_SCOPE = "grpc:controlplane";
  private static final int REFRESH_BUFFER_SECONDS = 60;

  private final WebClient webClient;
  private final String tokenUrl;
  private final String clientId;
  private final String clientSecret;

  // client_credentials cache — single token
  private volatile String serviceToken;
  private volatile Instant serviceTokenExpiresAt = Instant.EPOCH;

  // token-exchange cache — keyed by "userSub|audience"
  private final Map<String, CachedToken> exchangeCache = new ConcurrentHashMap<>();

  public KeycloakTokenExchangeProvider(
      Vertx vertx, String tokenUrl, String clientId, String clientSecret) {
    this.webClient =
        WebClient.create(vertx, new WebClientOptions().setConnectTimeout(5000));
    this.tokenUrl = tokenUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /**
   * Returns a cached service token obtained via client_credentials grant.
   * Re-fetches when within {@value REFRESH_BUFFER_SECONDS}s of expiry.
   */
  public Future<String> getServiceToken() {
    if (serviceToken != null
        && Instant.now().isBefore(serviceTokenExpiresAt.minusSeconds(REFRESH_BUFFER_SECONDS))) {
      return Future.succeededFuture(serviceToken);
    }
    return fetchServiceToken();
  }

  private Future<String> fetchServiceToken() {
    LOGGER.debug("Fetching Keycloak service token via client_credentials for client_id={}", clientId);
    return webClient
        .postAbs(tokenUrl)
        .putHeader("Content-Type", "application/x-www-form-urlencoded")
        .sendForm(
            MultiMap.caseInsensitiveMultiMap()
                .add("grant_type", GRANT_CLIENT_CREDENTIALS)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", GRPC_SCOPE))
        .compose(
            response -> {
              if (response.statusCode() != 200) {
                LOGGER.error(
                    "Keycloak client_credentials failed: HTTP {} body={}",
                    response.statusCode(),
                    response.bodyAsString());
                return Future.failedFuture(
                    "Keycloak service token request returned HTTP " + response.statusCode());
              }
              var body = response.bodyAsJsonObject();
              String token = body.getString("access_token");
              int expiresIn = body.getInteger("expires_in", 300);
              if (token == null || token.isBlank()) {
                return Future.failedFuture("Keycloak response missing access_token");
              }
              serviceToken = token;
              serviceTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
              LOGGER.debug(
                  "Keycloak service token acquired: client_id={} expiresIn={}s", clientId, expiresIn);
              return Future.succeededFuture(token);
            });
  }

  /**
   * Exchanges the given user JWT for a short-lived service-scoped token (RFC 8693).
   * The returned token carries the user's {@code sub} AND this service's {@code azp}.
   * Cached per {@code (userSub, targetAudience)}.
   *
   * @param userJwt        raw JWT string from the incoming HTTP request
   * @param targetAudience Keycloak client_id of the target service, e.g. {@code dx-controlplane}
   */
  public Future<String> exchange(String userJwt, String targetAudience) {
    String userSub = extractSub(userJwt);
    if (userSub == null) {
      return Future.failedFuture("Cannot extract sub from user JWT — token may be malformed");
    }
    String cacheKey = userSub + "|" + targetAudience;
    CachedToken cached = exchangeCache.get(cacheKey);
    if (cached != null
        && Instant.now().isBefore(cached.expiresAt.minusSeconds(REFRESH_BUFFER_SECONDS))) {
      LOGGER.debug("Token exchange cache hit: sub={} audience={}", userSub, targetAudience);
      return Future.succeededFuture(cached.token);
    }
    return fetchExchangedToken(userJwt, targetAudience, cacheKey);
  }

  private Future<String> fetchExchangedToken(
      String userJwt, String targetAudience, String cacheKey) {
    LOGGER.debug(
        "Calling Keycloak token exchange: client_id={} audience={}", clientId, targetAudience);
    return webClient
        .postAbs(tokenUrl)
        .putHeader("Content-Type", "application/x-www-form-urlencoded")
        .sendForm(
            MultiMap.caseInsensitiveMultiMap()
                .add("grant_type", GRANT_TOKEN_EXCHANGE)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("subject_token", userJwt)
                .add("subject_token_type", TOKEN_TYPE_ACCESS)
                .add("audience", targetAudience)
                .add("scope", GRPC_SCOPE)
                .add("requested_token_type", TOKEN_TYPE_ACCESS))
        .compose(
            response -> {
              if (response.statusCode() != 200) {
                LOGGER.error(
                    "Keycloak token exchange failed: HTTP {} body={}",
                    response.statusCode(),
                    response.bodyAsString());
                return Future.failedFuture(
                    "Keycloak token exchange returned HTTP " + response.statusCode());
              }
              var body = response.bodyAsJsonObject();
              String token = body.getString("access_token");
              int expiresIn = body.getInteger("expires_in", 300);
              if (token == null || token.isBlank()) {
                return Future.failedFuture("Keycloak exchange response missing access_token");
              }
              exchangeCache.put(cacheKey, new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
              LOGGER.debug(
                  "Token exchange successful: client_id={} audience={} expiresIn={}s",
                  clientId, targetAudience, expiresIn);
              return Future.succeededFuture(token);
            });
  }

  /** Extracts {@code sub} from a JWT payload by Base64url-decoding the middle segment. */
  private String extractSub(String rawJwt) {
    try {
      String[] parts = rawJwt.split("\\.");
      if (parts.length < 2) return null;
      String payload = parts[1];
      // Base64url may be unpadded; add padding if needed
      int rem = payload.length() % 4;
      if (rem != 0) {
        payload += "=".repeat(4 - rem);
      }
      byte[] decoded = Base64.getUrlDecoder().decode(payload);
      return new JsonObject(new String(decoded, StandardCharsets.UTF_8)).getString("sub");
    } catch (Exception e) {
      LOGGER.error("Failed to extract sub from JWT: {}", e.getMessage());
      return null;
    }
  }

  private static final class CachedToken {
    final String token;
    final Instant expiresAt;

    CachedToken(String token, Instant expiresAt) {
      this.token = token;
      this.expiresAt = expiresAt;
    }
  }
}
