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
import eu.opendppnode.sdk.model.BatteryUnitEventType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One immutable per-unit telemetry record (raw persisted row). Append-only: no update or delete path exists.
 */
@JsonPropertyOrder({
  BatteryUnitEventRow.JSON_PROPERTY_ID,
  BatteryUnitEventRow.JSON_PROPERTY_BATTERY_UNIT_ID,
  BatteryUnitEventRow.JSON_PROPERTY_TENANT_ID,
  BatteryUnitEventRow.JSON_PROPERTY_EVENT_TYPE,
  BatteryUnitEventRow.JSON_PROPERTY_STATE_OF_HEALTH,
  BatteryUnitEventRow.JSON_PROPERTY_CYCLE_COUNT,
  BatteryUnitEventRow.JSON_PROPERTY_REMAINING_CAPACITY_AH,
  BatteryUnitEventRow.JSON_PROPERTY_TEMPERATURE_C,
  BatteryUnitEventRow.JSON_PROPERTY_PAYLOAD,
  BatteryUnitEventRow.JSON_PROPERTY_RECORDED_AT,
  BatteryUnitEventRow.JSON_PROPERTY_CREATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitEventRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_BATTERY_UNIT_ID = "batteryUnitId";
  @jakarta.annotation.Nonnull
  private String batteryUnitId;

  public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
  @jakarta.annotation.Nonnull
  private String tenantId;

  public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
  @jakarta.annotation.Nonnull
  private BatteryUnitEventType eventType;

  public static final String JSON_PROPERTY_STATE_OF_HEALTH = "stateOfHealth";
  @jakarta.annotation.Nullable
  private BigDecimal stateOfHealth;

  public static final String JSON_PROPERTY_CYCLE_COUNT = "cycleCount";
  @jakarta.annotation.Nullable
  private Integer cycleCount;

  public static final String JSON_PROPERTY_REMAINING_CAPACITY_AH = "remainingCapacityAh";
  @jakarta.annotation.Nullable
  private BigDecimal remainingCapacityAh;

  public static final String JSON_PROPERTY_TEMPERATURE_C = "temperatureC";
  @jakarta.annotation.Nullable
  private BigDecimal temperatureC;

  public static final String JSON_PROPERTY_PAYLOAD = "payload";
  @jakarta.annotation.Nullable
  private Object payload = null;

  public static final String JSON_PROPERTY_RECORDED_AT = "recordedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime recordedAt;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public BatteryUnitEventRow() { 
  }

  public BatteryUnitEventRow id(@jakarta.annotation.Nonnull String id) {
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


  public BatteryUnitEventRow batteryUnitId(@jakarta.annotation.Nonnull String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
    return this;
  }

  /**
   * Get batteryUnitId
   * @return batteryUnitId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getBatteryUnitId() {
    return batteryUnitId;
  }


  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setBatteryUnitId(@jakarta.annotation.Nonnull String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
  }


  public BatteryUnitEventRow tenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
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


  public BatteryUnitEventRow eventType(@jakarta.annotation.Nonnull BatteryUnitEventType eventType) {
    this.eventType = eventType;
    return this;
  }

  /**
   * Get eventType
   * @return eventType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BatteryUnitEventType getEventType() {
    return eventType;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEventType(@jakarta.annotation.Nonnull BatteryUnitEventType eventType) {
    this.eventType = eventType;
  }


  public BatteryUnitEventRow stateOfHealth(@jakarta.annotation.Nullable BigDecimal stateOfHealth) {
    this.stateOfHealth = stateOfHealth;
    return this;
  }

  /**
   * State of health, percent.
   * minimum: 0
   * maximum: 100
   * @return stateOfHealth
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_STATE_OF_HEALTH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getStateOfHealth() {
    return stateOfHealth;
  }


  @JsonProperty(JSON_PROPERTY_STATE_OF_HEALTH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStateOfHealth(@jakarta.annotation.Nullable BigDecimal stateOfHealth) {
    this.stateOfHealth = stateOfHealth;
  }


  public BatteryUnitEventRow cycleCount(@jakarta.annotation.Nullable Integer cycleCount) {
    this.cycleCount = cycleCount;
    return this;
  }

  /**
   * Cumulative full-equivalent cycles (truncated to an integer on write).
   * minimum: 0
   * @return cycleCount
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CYCLE_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getCycleCount() {
    return cycleCount;
  }


  @JsonProperty(JSON_PROPERTY_CYCLE_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCycleCount(@jakarta.annotation.Nullable Integer cycleCount) {
    this.cycleCount = cycleCount;
  }


  public BatteryUnitEventRow remainingCapacityAh(@jakarta.annotation.Nullable BigDecimal remainingCapacityAh) {
    this.remainingCapacityAh = remainingCapacityAh;
    return this;
  }

  /**
   * Measured remaining capacity, ampere-hours.
   * minimum: 0
   * @return remainingCapacityAh
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REMAINING_CAPACITY_AH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getRemainingCapacityAh() {
    return remainingCapacityAh;
  }


  @JsonProperty(JSON_PROPERTY_REMAINING_CAPACITY_AH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRemainingCapacityAh(@jakarta.annotation.Nullable BigDecimal remainingCapacityAh) {
    this.remainingCapacityAh = remainingCapacityAh;
  }


  public BatteryUnitEventRow temperatureC(@jakarta.annotation.Nullable BigDecimal temperatureC) {
    this.temperatureC = temperatureC;
    return this;
  }

  /**
   * Observed temperature, °C.
   * minimum: -273.15
   * maximum: 10000
   * @return temperatureC
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TEMPERATURE_C)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getTemperatureC() {
    return temperatureC;
  }


  @JsonProperty(JSON_PROPERTY_TEMPERATURE_C)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTemperatureC(@jakarta.annotation.Nullable BigDecimal temperatureC) {
    this.temperatureC = temperatureC;
  }


  public BatteryUnitEventRow payload(@jakarta.annotation.Nullable Object payload) {
    this.payload = payload;
    return this;
  }

  /**
   * Get payload
   * @return payload
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PAYLOAD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getPayload() {
    return payload;
  }


  @JsonProperty(JSON_PROPERTY_PAYLOAD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPayload(@jakarta.annotation.Nullable Object payload) {
    this.payload = payload;
  }


  public BatteryUnitEventRow recordedAt(@jakarta.annotation.Nonnull OffsetDateTime recordedAt) {
    this.recordedAt = recordedAt;
    return this;
  }

  /**
   * When the measurement was taken (client-supplied; server time when omitted).
   * @return recordedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRecordedAt() {
    return recordedAt;
  }


  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRecordedAt(@jakarta.annotation.Nonnull OffsetDateTime recordedAt) {
    this.recordedAt = recordedAt;
  }


  public BatteryUnitEventRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Immutable append timestamp (server-assigned).
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


  /**
   * Return true if this BatteryUnitEventRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitEventRow batteryUnitEventRow = (BatteryUnitEventRow) o;
    return Objects.equals(this.id, batteryUnitEventRow.id) &&
        Objects.equals(this.batteryUnitId, batteryUnitEventRow.batteryUnitId) &&
        Objects.equals(this.tenantId, batteryUnitEventRow.tenantId) &&
        Objects.equals(this.eventType, batteryUnitEventRow.eventType) &&
        Objects.equals(this.stateOfHealth, batteryUnitEventRow.stateOfHealth) &&
        Objects.equals(this.cycleCount, batteryUnitEventRow.cycleCount) &&
        Objects.equals(this.remainingCapacityAh, batteryUnitEventRow.remainingCapacityAh) &&
        Objects.equals(this.temperatureC, batteryUnitEventRow.temperatureC) &&
        Objects.equals(this.payload, batteryUnitEventRow.payload) &&
        Objects.equals(this.recordedAt, batteryUnitEventRow.recordedAt) &&
        Objects.equals(this.createdAt, batteryUnitEventRow.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, batteryUnitId, tenantId, eventType, stateOfHealth, cycleCount, remainingCapacityAh, temperatureC, payload, recordedAt, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitEventRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    batteryUnitId: ").append(toIndentedString(batteryUnitId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    stateOfHealth: ").append(toIndentedString(stateOfHealth)).append("\n");
    sb.append("    cycleCount: ").append(toIndentedString(cycleCount)).append("\n");
    sb.append("    remainingCapacityAh: ").append(toIndentedString(remainingCapacityAh)).append("\n");
    sb.append("    temperatureC: ").append(toIndentedString(temperatureC)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    recordedAt: ").append(toIndentedString(recordedAt)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

    // add `batteryUnitId` to the URL query string
    if (getBatteryUnitId() != null) {
      joiner.add(String.format("%sbatteryUnitId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBatteryUnitId()))));
    }

    // add `tenantId` to the URL query string
    if (getTenantId() != null) {
      joiner.add(String.format("%stenantId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTenantId()))));
    }

    // add `eventType` to the URL query string
    if (getEventType() != null) {
      joiner.add(String.format("%seventType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventType()))));
    }

    // add `stateOfHealth` to the URL query string
    if (getStateOfHealth() != null) {
      joiner.add(String.format("%sstateOfHealth%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStateOfHealth()))));
    }

    // add `cycleCount` to the URL query string
    if (getCycleCount() != null) {
      joiner.add(String.format("%scycleCount%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCycleCount()))));
    }

    // add `remainingCapacityAh` to the URL query string
    if (getRemainingCapacityAh() != null) {
      joiner.add(String.format("%sremainingCapacityAh%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRemainingCapacityAh()))));
    }

    // add `temperatureC` to the URL query string
    if (getTemperatureC() != null) {
      joiner.add(String.format("%stemperatureC%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTemperatureC()))));
    }

    // add `payload` to the URL query string
    if (getPayload() != null) {
      joiner.add(String.format("%spayload%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPayload()))));
    }

    // add `recordedAt` to the URL query string
    if (getRecordedAt() != null) {
      joiner.add(String.format("%srecordedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRecordedAt()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    return joiner.toString();
  }
}

