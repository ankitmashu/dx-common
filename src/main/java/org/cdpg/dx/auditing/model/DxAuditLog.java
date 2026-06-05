package org.cdpg.dx.auditing.model;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete implementation of AuditLog for DX platform audit events.
 *
 * Supports dataplane-specific fields (assetId, organizationId, organizationName,
 * ipAddress, userAgent) for comprehensive audit trails.
 */
public class DxAuditLog implements AuditLog {
  private final UUID id;
  private final UUID assetId;
  private final String logType;
  private final String operation;
  private final String createdAt;
  private final String api;
  private final String method;
  private final Long size;
  private final String role;
  private final UUID userId;
  private final String originServer;
  private final String iss;
  private final String delegateId;
  private final String organizationId;
  private final String organizationName;
  private final String ipAddress;
  private final String userAgent;

  /**
   * Construct a DxAuditLog with comprehensive dataplane audit context.
   */
  public DxAuditLog(
      UUID id,
      UUID assetId,
      String logType,
      String operation,
      String createdAt,
      String api,
      String method,
      Long size,
      String role,
      UUID userId,
      String originServer,
      String organizationId,
      String organizationName,
      String iss,
      String delegateId,
      String ipAddress,
      String userAgent) {
    this.id = id;
    this.assetId = assetId;
    this.logType = logType;
    this.operation = operation;
    this.createdAt = createdAt;
    this.api = api;
    this.method = method;
    this.size = size;
    this.role = role;
    this.userId = userId;
    this.originServer = originServer;
    this.iss = iss;
    this.delegateId = delegateId;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }

  // Accessor methods for audit log fields
  public UUID getId() {
    return id;
  }

  public UUID getAssetId() {
    return assetId;
  }

  public String getLogType() {
    return logType;
  }

  public String getOperation() {
    return operation;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getApi() {
    return api;
  }

  public String getMethod() {
    return method;
  }

  public Long getSize() {
    return size;
  }

  public String getRole() {
    return role;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getOriginServer() {
    return originServer;
  }

  public String getIss() {
    return iss;
  }

  public String getDelegateId() {
    return delegateId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("id", id != null ? id.toString() : null);
    json.put("assetId", assetId != null ? assetId.toString() : null);
    json.put("logType", logType);
    json.put("operation", operation);
    json.put("createdAt", createdAt);
    json.put("api", api);
    json.put("method", method);
    json.put("size", size);
    json.put("role", role);
    json.put("userId", userId != null ? userId.toString() : null);
    json.put("originServer", originServer);
    json.put("iss", iss);
    json.put("delegateId", delegateId);
    json.put("organizationId", organizationId);
    json.put("organizationName", organizationName);
    json.put("ipAddress", ipAddress);
    json.put("userAgent", userAgent);
    return json;
  }
}
