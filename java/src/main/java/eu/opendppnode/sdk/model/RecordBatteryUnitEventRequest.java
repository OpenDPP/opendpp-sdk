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
import eu.opendppnode.sdk.model.BatteryUnitStatus;
import eu.opendppnode.sdk.model.RecordBatteryUnitEventRequestRecordedAt;
import java.math.BigDecimal;
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One telemetry record. All measurements are optional and independently nullable; numeric ranges are enforced with 400 on violation.
 */
@JsonPropertyOrder({
  RecordBatteryUnitEventRequest.JSON_PROPERTY_EVENT_TYPE,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_STATE_OF_HEALTH,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_CYCLE_COUNT,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_REMAINING_CAPACITY_AH,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_TEMPERATURE_C,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_PAYLOAD,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_RECORDED_AT,
  RecordBatteryUnitEventRequest.JSON_PROPERTY_STATUS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class RecordBatteryUnitEventRequest {
  public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
  @jakarta.annotation.Nonnull
  private BatteryUnitEventType eventType;

  public static final String JSON_PROPERTY_STATE_OF_HEALTH = "stateOfHealth";
  private JsonNullable<BigDecimal> stateOfHealth = JsonNullable.<BigDecimal>undefined();

  public static final String JSON_PROPERTY_CYCLE_COUNT = "cycleCount";
  private JsonNullable<BigDecimal> cycleCount = JsonNullable.<BigDecimal>undefined();

  public static final String JSON_PROPERTY_REMAINING_CAPACITY_AH = "remainingCapacityAh";
  private JsonNullable<BigDecimal> remainingCapacityAh = JsonNullable.<BigDecimal>undefined();

  public static final String JSON_PROPERTY_TEMPERATURE_C = "temperatureC";
  private JsonNullable<BigDecimal> temperatureC = JsonNullable.<BigDecimal>undefined();

  public static final String JSON_PROPERTY_PAYLOAD = "payload";
  private JsonNullable<Object> payload = JsonNullable.<Object>of(null);

  public static final String JSON_PROPERTY_RECORDED_AT = "recordedAt";
  @jakarta.annotation.Nullable
  private RecordBatteryUnitEventRequestRecordedAt recordedAt;

  public static final String JSON_PROPERTY_STATUS = "status";
  @jakarta.annotation.Nullable
  private BatteryUnitStatus status;

  public RecordBatteryUnitEventRequest() { 
  }

  public RecordBatteryUnitEventRequest eventType(@jakarta.annotation.Nonnull BatteryUnitEventType eventType) {
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


  public RecordBatteryUnitEventRequest stateOfHealth(@jakarta.annotation.Nullable BigDecimal stateOfHealth) {
    this.stateOfHealth = JsonNullable.<BigDecimal>of(stateOfHealth);
    return this;
  }

  /**
   * State of health, percent (0–100).
   * minimum: 0
   * maximum: 100
   * @return stateOfHealth
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public BigDecimal getStateOfHealth() {
        return stateOfHealth.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_STATE_OF_HEALTH)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<BigDecimal> getStateOfHealth_JsonNullable() {
    return stateOfHealth;
  }
  
  @JsonProperty(JSON_PROPERTY_STATE_OF_HEALTH)
  public void setStateOfHealth_JsonNullable(JsonNullable<BigDecimal> stateOfHealth) {
    this.stateOfHealth = stateOfHealth;
  }

  public void setStateOfHealth(@jakarta.annotation.Nullable BigDecimal stateOfHealth) {
    this.stateOfHealth = JsonNullable.<BigDecimal>of(stateOfHealth);
  }


  public RecordBatteryUnitEventRequest cycleCount(@jakarta.annotation.Nullable BigDecimal cycleCount) {
    this.cycleCount = JsonNullable.<BigDecimal>of(cycleCount);
    return this;
  }

  /**
   * Cumulative full-equivalent cycles. Fractional values are accepted but truncated to an integer before persisting.
   * minimum: 0
   * maximum: 9007199254740991
   * @return cycleCount
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public BigDecimal getCycleCount() {
        return cycleCount.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_CYCLE_COUNT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<BigDecimal> getCycleCount_JsonNullable() {
    return cycleCount;
  }
  
  @JsonProperty(JSON_PROPERTY_CYCLE_COUNT)
  public void setCycleCount_JsonNullable(JsonNullable<BigDecimal> cycleCount) {
    this.cycleCount = cycleCount;
  }

  public void setCycleCount(@jakarta.annotation.Nullable BigDecimal cycleCount) {
    this.cycleCount = JsonNullable.<BigDecimal>of(cycleCount);
  }


  public RecordBatteryUnitEventRequest remainingCapacityAh(@jakarta.annotation.Nullable BigDecimal remainingCapacityAh) {
    this.remainingCapacityAh = JsonNullable.<BigDecimal>of(remainingCapacityAh);
    return this;
  }

  /**
   * Remaining capacity, ampere-hours.
   * minimum: 0
   * maximum: 9007199254740991
   * @return remainingCapacityAh
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public BigDecimal getRemainingCapacityAh() {
        return remainingCapacityAh.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_REMAINING_CAPACITY_AH)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<BigDecimal> getRemainingCapacityAh_JsonNullable() {
    return remainingCapacityAh;
  }
  
  @JsonProperty(JSON_PROPERTY_REMAINING_CAPACITY_AH)
  public void setRemainingCapacityAh_JsonNullable(JsonNullable<BigDecimal> remainingCapacityAh) {
    this.remainingCapacityAh = remainingCapacityAh;
  }

  public void setRemainingCapacityAh(@jakarta.annotation.Nullable BigDecimal remainingCapacityAh) {
    this.remainingCapacityAh = JsonNullable.<BigDecimal>of(remainingCapacityAh);
  }


  public RecordBatteryUnitEventRequest temperatureC(@jakarta.annotation.Nullable BigDecimal temperatureC) {
    this.temperatureC = JsonNullable.<BigDecimal>of(temperatureC);
    return this;
  }

  /**
   * Observed temperature, °C.
   * minimum: -273.15
   * maximum: 10000
   * @return temperatureC
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public BigDecimal getTemperatureC() {
        return temperatureC.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_TEMPERATURE_C)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<BigDecimal> getTemperatureC_JsonNullable() {
    return temperatureC;
  }
  
  @JsonProperty(JSON_PROPERTY_TEMPERATURE_C)
  public void setTemperatureC_JsonNullable(JsonNullable<BigDecimal> temperatureC) {
    this.temperatureC = temperatureC;
  }

  public void setTemperatureC(@jakarta.annotation.Nullable BigDecimal temperatureC) {
    this.temperatureC = JsonNullable.<BigDecimal>of(temperatureC);
  }


  public RecordBatteryUnitEventRequest payload(@jakarta.annotation.Nullable Object payload) {
    this.payload = JsonNullable.<Object>of(payload);
    return this;
  }

  /**
   * Get payload
   * @return payload
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public Object getPayload() {
        return payload.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_PAYLOAD)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getPayload_JsonNullable() {
    return payload;
  }
  
  @JsonProperty(JSON_PROPERTY_PAYLOAD)
  public void setPayload_JsonNullable(JsonNullable<Object> payload) {
    this.payload = payload;
  }

  public void setPayload(@jakarta.annotation.Nullable Object payload) {
    this.payload = JsonNullable.<Object>of(payload);
  }


  public RecordBatteryUnitEventRequest recordedAt(@jakarta.annotation.Nullable RecordBatteryUnitEventRequestRecordedAt recordedAt) {
    this.recordedAt = recordedAt;
    return this;
  }

  /**
   * Get recordedAt
   * @return recordedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public RecordBatteryUnitEventRequestRecordedAt getRecordedAt() {
    return recordedAt;
  }


  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRecordedAt(@jakarta.annotation.Nullable RecordBatteryUnitEventRequestRecordedAt recordedAt) {
    this.recordedAt = recordedAt;
  }


  public RecordBatteryUnitEventRequest status(@jakarta.annotation.Nullable BatteryUnitStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Optional status transition, applied to the unit in the same transaction when it differs from the current status (works with any &#x60;eventType&#x60;; conventionally paired with &#x60;STATUS_CHANGE&#x60;). Transitioning to &#x60;RECYCLED&#x60; stamps &#x60;ceasedAt&#x60; (if not already set; never cleared) and turns the public unit view into a 410 tombstone (Art. 77(8)); &#x60;status&#x60; itself is not locked afterwards — a later event may still set a different value — but &#x60;ceasedAt&#x60; persists, so the 410 and the predecessor refusal are permanent.
   * @return status
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public BatteryUnitStatus getStatus() {
    return status;
  }


  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setStatus(@jakarta.annotation.Nullable BatteryUnitStatus status) {
    this.status = status;
  }


  /**
   * Return true if this RecordBatteryUnitEventRequest object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordBatteryUnitEventRequest recordBatteryUnitEventRequest = (RecordBatteryUnitEventRequest) o;
    return Objects.equals(this.eventType, recordBatteryUnitEventRequest.eventType) &&
        equalsNullable(this.stateOfHealth, recordBatteryUnitEventRequest.stateOfHealth) &&
        equalsNullable(this.cycleCount, recordBatteryUnitEventRequest.cycleCount) &&
        equalsNullable(this.remainingCapacityAh, recordBatteryUnitEventRequest.remainingCapacityAh) &&
        equalsNullable(this.temperatureC, recordBatteryUnitEventRequest.temperatureC) &&
        equalsNullable(this.payload, recordBatteryUnitEventRequest.payload) &&
        Objects.equals(this.recordedAt, recordBatteryUnitEventRequest.recordedAt) &&
        Objects.equals(this.status, recordBatteryUnitEventRequest.status);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventType, hashCodeNullable(stateOfHealth), hashCodeNullable(cycleCount), hashCodeNullable(remainingCapacityAh), hashCodeNullable(temperatureC), hashCodeNullable(payload), recordedAt, status);
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RecordBatteryUnitEventRequest {\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    stateOfHealth: ").append(toIndentedString(stateOfHealth)).append("\n");
    sb.append("    cycleCount: ").append(toIndentedString(cycleCount)).append("\n");
    sb.append("    remainingCapacityAh: ").append(toIndentedString(remainingCapacityAh)).append("\n");
    sb.append("    temperatureC: ").append(toIndentedString(temperatureC)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    recordedAt: ").append(toIndentedString(recordedAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
      joiner.add(getRecordedAt().toUrlQueryString(prefix + "recordedAt" + suffix));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    return joiner.toString();
  }
}

