package org.cdpg.dx.catalogue.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import java.util.List;

/**
 * Canonical Asset model shared across all DX dataplanes and control plane.
 *
 * Represents a catalogue asset with provider ownership, organization context,
 * and optional OGC-specific metadata for audit logging.
 *
 * This is a Vert.x @DataObject enabling JSON serialization and code generation.
 */
@DataObject
@JsonGen
public class Asset {
  private String providerId;
  private String itemId;
  private String assetName;
  private String assetType;
  private String organizationId;
  private String organizationName;
  private String shortDescription;
  private String accessPolicy;
  private String createdAt;
  private List<String> tags;

  public Asset() {
  }

  public Asset(JsonObject json) {
    AssetConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AssetConverter.toJson(this, json);
    return json;
  }

  public Asset(Asset other) {
    this.providerId = other.providerId;
    this.itemId = other.itemId;
    this.assetName = other.assetName;
    this.assetType = other.assetType;
    this.organizationId = other.organizationId;
    this.organizationName = other.organizationName;
    this.shortDescription = other.shortDescription;
    this.accessPolicy = other.accessPolicy;
    this.createdAt = other.createdAt;
    this.tags = other.tags;
  }

  public String getProviderId() {
    return providerId;
  }

  public Asset setProviderId(String providerId) {
    this.providerId = providerId;
    return this;
  }

  public String getItemId() {
    return itemId;
  }

  public Asset setItemId(String itemId) {
    this.itemId = itemId;
    return this;
  }

  public String getAssetName() {
    return assetName;
  }

  public Asset setAssetName(String assetName) {
    this.assetName = assetName;
    return this;
  }

  public String getAssetType() {
    return assetType;
  }

  public Asset setAssetType(String assetType) {
    this.assetType = assetType;
    return this;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public Asset setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public Asset setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
    return this;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public Asset setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
    return this;
  }

  public String getAccessPolicy() {
    return accessPolicy;
  }

  public Asset setAccessPolicy(String accessPolicy) {
    this.accessPolicy = accessPolicy;
    return this;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public Asset setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public List<String> getTags() {
    return tags;
  }

  public Asset setTags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  @Override
  public String toString() {
    return "Asset{" +
        "providerId='" + providerId + '\'' +
        ", itemId='" + itemId + '\'' +
        ", assetName='" + assetName + '\'' +
        ", assetType='" + assetType + '\'' +
        ", organizationId='" + organizationId + '\'' +
        ", organizationName='" + organizationName + '\'' +
        ", shortDescription='" + shortDescription + '\'' +
        ", accessPolicy='" + accessPolicy + '\'' +
        ", createdAt='" + createdAt + '\'' +
        ", tags=" + tags +
        '}';
  }
}
