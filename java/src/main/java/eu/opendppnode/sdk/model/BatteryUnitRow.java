/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR (Regulation (EU) 2024/1781) data requirements and the EU Battery Regulation (Regulation (EU) 2023/1542). This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations return Fastify's default `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Global limit: **100 requests/min per IP** (higher for verified crawlers), with `x-ratelimit-*` response headers. Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**. Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window.  ## Sealing & verification Passport seals are **eIDAS advanced electronic seals** (ECDSA P-256 over a Merkle root of the passport content, optional RFC 3161 timestamp). Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.11.0
 * Contact: support@opendpp-node.eu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package eu.opendppnode.sdk.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.opendppnode.sdk.model.BatteryUnitStatus;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One physical serialised battery (raw persisted row — these routes declare no Fastify response schema, so all model fields are returned as-is). A &#x60;BatteryUnit&#x60; is an individual instance of a SKU/type-level passport, carrying its real serial in GS1 AI-21.
 */
@JsonPropertyOrder({
  BatteryUnitRow.JSON_PROPERTY_ID,
  BatteryUnitRow.JSON_PROPERTY_SERIAL_NUMBER,
  BatteryUnitRow.JSON_PROPERTY_DIGITAL_LINK_URI,
  BatteryUnitRow.JSON_PROPERTY_PASSPORT_ID,
  BatteryUnitRow.JSON_PROPERTY_TENANT_ID,
  BatteryUnitRow.JSON_PROPERTY_MANUFACTURED_AT,
  BatteryUnitRow.JSON_PROPERTY_STATUS,
  BatteryUnitRow.JSON_PROPERTY_CEASED_AT,
  BatteryUnitRow.JSON_PROPERTY_PREDECESSOR_UNIT_ID,
  BatteryUnitRow.JSON_PROPERTY_CREATED_AT,
  BatteryUnitRow.JSON_PROPERTY_UPDATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_SERIAL_NUMBER = "serialNumber";
  @jakarta.annotation.Nonnull
  private String serialNumber;

  public static final String JSON_PROPERTY_DIGITAL_LINK_URI = "digitalLinkUri";
  @jakarta.annotation.Nonnull
  private URI digitalLinkUri;

  public static final String JSON_PROPERTY_PASSPORT_ID = "passportId";
  @jakarta.annotation.Nonnull
  private String passportId;

  public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
  @jakarta.annotation.Nonnull
  private String tenantId;

  public static final String JSON_PROPERTY_MANUFACTURED_AT = "manufacturedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime manufacturedAt;

  public static final String JSON_PROPERTY_STATUS = "status";
  @jakarta.annotation.Nonnull
  private BatteryUnitStatus status;

  public static final String JSON_PROPERTY_CEASED_AT = "ceasedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime ceasedAt;

  public static final String JSON_PROPERTY_PREDECESSOR_UNIT_ID = "predecessorUnitId";
  @jakarta.annotation.Nullable
  private String predecessorUnitId;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public BatteryUnitRow() { 
  }

  public BatteryUnitRow id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setId(@jakarta.annotation.Nonnull String id) {
    this.id = id;
  }


  public BatteryUnitRow serialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * The battery&#39;s real physical serial number (GS1 AI-21 value). 1–20 URL-safe characters; GS1 recommends ≤ 20.
   * @return serialNumber
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSerialNumber() {
    return serialNumber;
  }


  @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSerialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
  }


  public BatteryUnitRow digitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
    return this;
  }

  /**
   * Per-unit GS1 Digital Link: &#x60;{BASE_URL}/{01|8003}/{productId}/21/{serialNumber}&#x60; — AI &#x60;01&#x60; for GTIN (and non-GS1 SKUs), &#x60;8003&#x60; for GRAI. Unique platform-wide.
   * @return digitalLinkUri
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK_URI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getDigitalLinkUri() {
    return digitalLinkUri;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK_URI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
  }


  public BatteryUnitRow passportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
    return this;
  }

  /**
   * The SKU/type-level passport this unit is an instance of.
   * @return passportId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPassportId() {
    return passportId;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPassportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
  }


  public BatteryUnitRow tenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Owning tenant id (the demo tenant uses the fixed id &#x60;tenant-demo-opendpp&#x60;; regular tenants use UUIDs).
   * @return tenantId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTenantId() {
    return tenantId;
  }


  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
  }


  public BatteryUnitRow manufacturedAt(@jakarta.annotation.Nullable OffsetDateTime manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
    return this;
  }

  /**
   * Get manufacturedAt
   * @return manufacturedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getManufacturedAt() {
    return manufacturedAt;
  }


  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setManufacturedAt(@jakarta.annotation.Nullable OffsetDateTime manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
  }


  public BatteryUnitRow status(@jakarta.annotation.Nonnull BatteryUnitStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BatteryUnitStatus getStatus() {
    return status;
  }


  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStatus(@jakarta.annotation.Nonnull BatteryUnitStatus status) {
    this.status = status;
  }


  public BatteryUnitRow ceasedAt(@jakarta.annotation.Nullable OffsetDateTime ceasedAt) {
    this.ceasedAt = ceasedAt;
    return this;
  }

  /**
   * Stamped when the events endpoint transitions &#x60;status&#x60; to &#x60;RECYCLED&#x60; (Art. 77(8) cease-to-exist); never cleared afterwards, even if a later event changes &#x60;status&#x60; again. Non-null means the public unit view is a 410 tombstone and the unit is refused as a &#x60;predecessorUnitId&#x60;. Note: a unit *created* with initial status &#x60;RECYCLED&#x60; does NOT get &#x60;ceasedAt&#x60; stamped (the public view still tombstones on the status alone, but the predecessor refusal does not apply).
   * @return ceasedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CEASED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCeasedAt() {
    return ceasedAt;
  }


  @JsonProperty(JSON_PROPERTY_CEASED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCeasedAt(@jakarta.annotation.Nullable OffsetDateTime ceasedAt) {
    this.ceasedAt = ceasedAt;
  }


  public BatteryUnitRow predecessorUnitId(@jakarta.annotation.Nullable String predecessorUnitId) {
    this.predecessorUnitId = predecessorUnitId;
    return this;
  }

  /**
   * Art. 77(7) lineage: the original unit this battery was repurposed/remanufactured from (&#x60;null&#x60; for first-life units).
   * @return predecessorUnitId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PREDECESSOR_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPredecessorUnitId() {
    return predecessorUnitId;
  }


  @JsonProperty(JSON_PROPERTY_PREDECESSOR_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPredecessorUnitId(@jakarta.annotation.Nullable String predecessorUnitId) {
    this.predecessorUnitId = predecessorUnitId;
  }


  public BatteryUnitRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }


  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCreatedAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  public BatteryUnitRow updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }


  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUpdatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  /**
   * Return true if this BatteryUnitRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitRow batteryUnitRow = (BatteryUnitRow) o;
    return Objects.equals(this.id, batteryUnitRow.id) &&
        Objects.equals(this.serialNumber, batteryUnitRow.serialNumber) &&
        Objects.equals(this.digitalLinkUri, batteryUnitRow.digitalLinkUri) &&
        Objects.equals(this.passportId, batteryUnitRow.passportId) &&
        Objects.equals(this.tenantId, batteryUnitRow.tenantId) &&
        Objects.equals(this.manufacturedAt, batteryUnitRow.manufacturedAt) &&
        Objects.equals(this.status, batteryUnitRow.status) &&
        Objects.equals(this.ceasedAt, batteryUnitRow.ceasedAt) &&
        Objects.equals(this.predecessorUnitId, batteryUnitRow.predecessorUnitId) &&
        Objects.equals(this.createdAt, batteryUnitRow.createdAt) &&
        Objects.equals(this.updatedAt, batteryUnitRow.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, serialNumber, digitalLinkUri, passportId, tenantId, manufacturedAt, status, ceasedAt, predecessorUnitId, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    digitalLinkUri: ").append(toIndentedString(digitalLinkUri)).append("\n");
    sb.append("    passportId: ").append(toIndentedString(passportId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    manufacturedAt: ").append(toIndentedString(manufacturedAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    ceasedAt: ").append(toIndentedString(ceasedAt)).append("\n");
    sb.append("    predecessorUnitId: ").append(toIndentedString(predecessorUnitId)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `serialNumber` to the URL query string
    if (getSerialNumber() != null) {
      joiner.add(String.format("%sserialNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
    }

    // add `digitalLinkUri` to the URL query string
    if (getDigitalLinkUri() != null) {
      joiner.add(String.format("%sdigitalLinkUri%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLinkUri()))));
    }

    // add `passportId` to the URL query string
    if (getPassportId() != null) {
      joiner.add(String.format("%spassportId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPassportId()))));
    }

    // add `tenantId` to the URL query string
    if (getTenantId() != null) {
      joiner.add(String.format("%stenantId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTenantId()))));
    }

    // add `manufacturedAt` to the URL query string
    if (getManufacturedAt() != null) {
      joiner.add(String.format("%smanufacturedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getManufacturedAt()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `ceasedAt` to the URL query string
    if (getCeasedAt() != null) {
      joiner.add(String.format("%sceasedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCeasedAt()))));
    }

    // add `predecessorUnitId` to the URL query string
    if (getPredecessorUnitId() != null) {
      joiner.add(String.format("%spredecessorUnitId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPredecessorUnitId()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `updatedAt` to the URL query string
    if (getUpdatedAt() != null) {
      joiner.add(String.format("%supdatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUpdatedAt()))));
    }

    return joiner.toString();
  }
}

