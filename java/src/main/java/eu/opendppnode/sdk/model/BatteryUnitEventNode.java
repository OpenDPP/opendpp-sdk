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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One append-only telemetry event (owner/grant tiers only).
 */
@JsonPropertyOrder({
  BatteryUnitEventNode.JSON_PROPERTY_AT_TYPE,
  BatteryUnitEventNode.JSON_PROPERTY_EVENT_TYPE,
  BatteryUnitEventNode.JSON_PROPERTY_STATE_OF_HEALTH,
  BatteryUnitEventNode.JSON_PROPERTY_CYCLE_COUNT,
  BatteryUnitEventNode.JSON_PROPERTY_REMAINING_CAPACITY_AH,
  BatteryUnitEventNode.JSON_PROPERTY_TEMPERATURE_C,
  BatteryUnitEventNode.JSON_PROPERTY_PAYLOAD,
  BatteryUnitEventNode.JSON_PROPERTY_RECORDED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitEventNode {
  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    BATTERY_UNIT_EVENT(String.valueOf("BatteryUnitEvent"));

    private String value;

    AtTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AtTypeEnum fromValue(String value) {
      for (AtTypeEnum b : AtTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private AtTypeEnum atType;

  /**
   * Gets or Sets eventType
   */
  public enum EventTypeEnum {
    SOH_MEASUREMENT(String.valueOf("SOH_MEASUREMENT")),
    
    CHARGE_CYCLE(String.valueOf("CHARGE_CYCLE")),
    
    STATUS_CHANGE(String.valueOf("STATUS_CHANGE")),
    
    NEGATIVE_EVENT(String.valueOf("NEGATIVE_EVENT")),
    
    OTHER(String.valueOf("OTHER"));

    private String value;

    EventTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static EventTypeEnum fromValue(String value) {
      for (EventTypeEnum b : EventTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
  @jakarta.annotation.Nonnull
  private EventTypeEnum eventType;

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

  public BatteryUnitEventNode() { 
  }

  public BatteryUnitEventNode atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
    return this;
  }

  /**
   * Get atType
   * @return atType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public AtTypeEnum getAtType() {
    return atType;
  }


  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
  }


  public BatteryUnitEventNode eventType(@jakarta.annotation.Nonnull EventTypeEnum eventType) {
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
  public EventTypeEnum getEventType() {
    return eventType;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEventType(@jakarta.annotation.Nonnull EventTypeEnum eventType) {
    this.eventType = eventType;
  }


  public BatteryUnitEventNode stateOfHealth(@jakarta.annotation.Nullable BigDecimal stateOfHealth) {
    this.stateOfHealth = stateOfHealth;
    return this;
  }

  /**
   * Percent, 0-100.
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


  public BatteryUnitEventNode cycleCount(@jakarta.annotation.Nullable Integer cycleCount) {
    this.cycleCount = cycleCount;
    return this;
  }

  /**
   * Get cycleCount
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


  public BatteryUnitEventNode remainingCapacityAh(@jakarta.annotation.Nullable BigDecimal remainingCapacityAh) {
    this.remainingCapacityAh = remainingCapacityAh;
    return this;
  }

  /**
   * Get remainingCapacityAh
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


  public BatteryUnitEventNode temperatureC(@jakarta.annotation.Nullable BigDecimal temperatureC) {
    this.temperatureC = temperatureC;
    return this;
  }

  /**
   * Get temperatureC
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


  public BatteryUnitEventNode payload(@jakarta.annotation.Nullable Object payload) {
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


  public BatteryUnitEventNode recordedAt(@jakarta.annotation.Nonnull OffsetDateTime recordedAt) {
    this.recordedAt = recordedAt;
    return this;
  }

  /**
   * When the measurement was taken (client-supplied at ingestion).
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


  /**
   * Return true if this BatteryUnitEventNode object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitEventNode batteryUnitEventNode = (BatteryUnitEventNode) o;
    return Objects.equals(this.atType, batteryUnitEventNode.atType) &&
        Objects.equals(this.eventType, batteryUnitEventNode.eventType) &&
        Objects.equals(this.stateOfHealth, batteryUnitEventNode.stateOfHealth) &&
        Objects.equals(this.cycleCount, batteryUnitEventNode.cycleCount) &&
        Objects.equals(this.remainingCapacityAh, batteryUnitEventNode.remainingCapacityAh) &&
        Objects.equals(this.temperatureC, batteryUnitEventNode.temperatureC) &&
        Objects.equals(this.payload, batteryUnitEventNode.payload) &&
        Objects.equals(this.recordedAt, batteryUnitEventNode.recordedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atType, eventType, stateOfHealth, cycleCount, remainingCapacityAh, temperatureC, payload, recordedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitEventNode {\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    stateOfHealth: ").append(toIndentedString(stateOfHealth)).append("\n");
    sb.append("    cycleCount: ").append(toIndentedString(cycleCount)).append("\n");
    sb.append("    remainingCapacityAh: ").append(toIndentedString(remainingCapacityAh)).append("\n");
    sb.append("    temperatureC: ").append(toIndentedString(temperatureC)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    recordedAt: ").append(toIndentedString(recordedAt)).append("\n");
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

    // add `@type` to the URL query string
    if (getAtType() != null) {
      joiner.add(String.format("%s@type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtType()))));
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

    return joiner.toString();
  }
}

