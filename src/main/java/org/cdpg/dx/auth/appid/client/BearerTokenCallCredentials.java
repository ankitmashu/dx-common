package org.cdpg.dx.auth.appid.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import java.util.concurrent.Executor;

/**
 * Per-call {@link CallCredentials} that injects a pre-obtained Bearer token into gRPC metadata.
 * Instantiated per gRPC call with the token returned by {@link KeycloakTokenExchangeProvider}.
 */
public class BearerTokenCallCredentials extends CallCredentials {

  private static final Metadata.Key<String> AUTHORIZATION_KEY =
      Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

  private final String bearerToken;

  public BearerTokenCallCredentials(String bearerToken) {
    this.bearerToken = bearerToken;
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    Metadata headers = new Metadata();
    headers.put(AUTHORIZATION_KEY, "Bearer " + bearerToken);
    applier.apply(headers);
  }
}
