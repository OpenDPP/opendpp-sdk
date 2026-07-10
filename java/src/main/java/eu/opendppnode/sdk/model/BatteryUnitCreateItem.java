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
import eu.opendppnode.sdk.model.BatteryUnitCreateItemManufacturedAt;
import eu.opendppnode.sdk.model.BatteryUnitStatus;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One unit to serialise. Validation is per-item: an invalid item is skipped (its error string collected) without failing the rest of the batch.
 */
@JsonPropertyOrder({
  BatteryUnitCreateItem.JSON_PROPERTY_SERIAL_NUMBER,
  BatteryUnitCreateItem.JSON_PROPERTY_MANUFACTURED_AT,
  BatteryUnitCreateItem.JSON_PROPERTY_STATUS,
  BatteryUnitCreateItem.JSON_PROPERTY_PREDECESSOR_UNIT_ID,
  BatteryUnitCreateItem.JSON_PROPERTY_PREDECESSOR_STATUS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitCreateItem {
  public static final String JSON_PROPERTY_SERIAL_NUMBER = "serialNumber";
  @jakarta.annotation.Nonnull
  private String serialNumber;

  public static final String JSON_PROPERTY_MANUFACTURED_AT = "manufacturedAt";
  @jakarta.annotation.Nullable
  private BatteryUnitCreateItemManufacturedAt manufacturedAt;

  public static final String JSON_PROPERTY_STATUS = "status";
  @jakarta.annotation.Nullable
  private BatteryUnitStatus status;

  public static final String JSON_PROPERTY_PREDECESSOR_UNIT_ID = "predecessorUnitId";
  @jakarta.annotation.Nullable
  private String predecessorUnitId;

  /**
   * Optional; only meaningful with &#x60;predecessorUnitId&#x60;. The status the predecessor transitions to. Defaults to &#x60;REPURPOSED&#x60;.
   */
  public enum PredecessorStatusEnum {
    REPURPOSED(String.valueOf("REPURPOSED")),
    
    REMANUFACTURED(String.valueOf("REMANUFACTURED")),
    
    REUSED(String.valueOf("REUSED"));

    private String value;

    PredecessorStatusEnum(String value) {
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
    public static PredecessorStatusEnum fromValue(String value) {
      for (PredecessorStatusEnum b : PredecessorStatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PREDECESSOR_STATUS = "predecessorStatus";
  @jakarta.annotation.Nullable
  private PredecessorStatusEnum predecessorStatus = PredecessorStatusEnum.REPURPOSED;

  public BatteryUnitCreateItem() { 
  }

  public BatteryUnitCreateItem serialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * Required. The battery&#39;s real physical serial (GS1 AI-21). Trimmed, then must match &#x60;^[A-Za-z0-9._-]{1,20}$&#x60; (GS1 AI-21&#39;s 20-character maximum) — a longer serial is rejected at ingest with a per-item error. Must be unique within the passport (duplicates are skipped with an item error).
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


  public BatteryUnitCreateItem manufacturedAt(@jakarta.annotation.Nullable BatteryUnitCreateItemManufacturedAt manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
    return this;
  }

  /**
   * Get manufacturedAt
   * @return manufacturedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public BatteryUnitCreateItemManufacturedAt getManufacturedAt() {
    return manufacturedAt;
  }


  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setManufacturedAt(@jakarta.annotation.Nullable BatteryUnitCreateItemManufacturedAt manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
  }


  public BatteryUnitCreateItem status(@jakarta.annotation.Nullable BatteryUnitStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Optional initial status. Defaults to &#x60;IN_SERVICE&#x60;. Note: creating a unit directly with &#x60;RECYCLED&#x60; makes the public view a 410 tombstone but does NOT stamp &#x60;ceasedAt&#x60; (only the events-route transition does), so such a unit can still be referenced as a predecessor.
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


  public BatteryUnitCreateItem predecessorUnitId(@jakarta.annotation.Nullable String predecessorUnitId) {
    this.predecessorUnitId = predecessorUnitId;
    return this;
  }

  /**
   * Optional Art. 77(7) linkage: id of an existing unit **in your tenant** (any passport) that this battery was repurposed/remanufactured from. A recycled predecessor (&#x60;ceasedAt&#x60; set) is refused — the check keys on &#x60;ceasedAt&#x60;, which only the events-route &#x60;RECYCLED&#x60; transition stamps. Atomically with creation, a &#x60;STATUS_CHANGE&#x60; event (&#x60;{status, successorUnitId, successorSerial}&#x60;) is appended to the predecessor and its status set to &#x60;predecessorStatus&#x60;.
   * @return predecessorUnitId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PREDECESSOR_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPredecessorUnitId() {
    return predecessorUnitId;
  }


  @JsonProperty(JSON_PROPERTY_PREDECESSOR_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPredecessorUnitId(@jakarta.annotation.Nullable String predecessorUnitId) {
    this.predecessorUnitId = predecessorUnitId;
  }


  public BatteryUnitCreateItem predecessorStatus(@jakarta.annotation.Nullable PredecessorStatusEnum predecessorStatus) {
    this.predecessorStatus = predecessorStatus;
    return this;
  }

  /**
   * Optional; only meaningful with &#x60;predecessorUnitId&#x60;. The status the predecessor transitions to. Defaults to &#x60;REPURPOSED&#x60;.
   * @return predecessorStatus
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PREDECESSOR_STATUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public PredecessorStatusEnum getPredecessorStatus() {
    return predecessorStatus;
  }


  @JsonProperty(JSON_PROPERTY_PREDECESSOR_STATUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPredecessorStatus(@jakarta.annotation.Nullable PredecessorStatusEnum predecessorStatus) {
    this.predecessorStatus = predecessorStatus;
  }


  /**
   * Return true if this BatteryUnitCreateItem object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitCreateItem batteryUnitCreateItem = (BatteryUnitCreateItem) o;
    return Objects.equals(this.serialNumber, batteryUnitCreateItem.serialNumber) &&
        Objects.equals(this.manufacturedAt, batteryUnitCreateItem.manufacturedAt) &&
        Objects.equals(this.status, batteryUnitCreateItem.status) &&
        Objects.equals(this.predecessorUnitId, batteryUnitCreateItem.predecessorUnitId) &&
        Objects.equals(this.predecessorStatus, batteryUnitCreateItem.predecessorStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serialNumber, manufacturedAt, status, predecessorUnitId, predecessorStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitCreateItem {\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    manufacturedAt: ").append(toIndentedString(manufacturedAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    predecessorUnitId: ").append(toIndentedString(predecessorUnitId)).append("\n");
    sb.append("    predecessorStatus: ").append(toIndentedString(predecessorStatus)).append("\n");
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

    // add `serialNumber` to the URL query string
    if (getSerialNumber() != null) {
      joiner.add(String.format("%sserialNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
    }

    // add `manufacturedAt` to the URL query string
    if (getManufacturedAt() != null) {
      joiner.add(getManufacturedAt().toUrlQueryString(prefix + "manufacturedAt" + suffix));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `predecessorUnitId` to the URL query string
    if (getPredecessorUnitId() != null) {
      joiner.add(String.format("%spredecessorUnitId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPredecessorUnitId()))));
    }

    // add `predecessorStatus` to the URL query string
    if (getPredecessorStatus() != null) {
      joiner.add(String.format("%spredecessorStatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPredecessorStatus()))));
    }

    return joiner.toString();
  }
}

