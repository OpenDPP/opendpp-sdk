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
import eu.opendppnode.sdk.model.AdvisoryItem;
import eu.opendppnode.sdk.model.BatteryUnitRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Returned (201) when at least one unit was created. Partial success is possible: skipped items are reported in &#x60;errors&#x60; while &#x60;units&#x60; holds the created rows.
 */
@JsonPropertyOrder({
  SerializeBatteryUnitsResponse.JSON_PROPERTY_SUCCESS,
  SerializeBatteryUnitsResponse.JSON_PROPERTY_MESSAGE,
  SerializeBatteryUnitsResponse.JSON_PROPERTY_COUNT,
  SerializeBatteryUnitsResponse.JSON_PROPERTY_UNITS,
  SerializeBatteryUnitsResponse.JSON_PROPERTY_ERRORS,
  SerializeBatteryUnitsResponse.JSON_PROPERTY_WARNINGS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SerializeBatteryUnitsResponse {
  /**
   * Gets or Sets success
   */
  public enum SuccessEnum {
    TRUE(Boolean.valueOf("true")),
    
    UNKNOWN_DEFAULT_OPEN_API(Boolean.valueOf("11184809"));

    private Boolean value;

    SuccessEnum(Boolean value) {
      this.value = value;
    }

    @JsonValue
    public Boolean getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SuccessEnum fromValue(Boolean value) {
      for (SuccessEnum b : SuccessEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private SuccessEnum success;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @jakarta.annotation.Nonnull
  private String message;

  public static final String JSON_PROPERTY_COUNT = "count";
  @jakarta.annotation.Nonnull
  private Integer count;

  public static final String JSON_PROPERTY_UNITS = "units";
  @jakarta.annotation.Nonnull
  private List<BatteryUnitRow> units = new ArrayList<>();

  public static final String JSON_PROPERTY_ERRORS = "errors";
  @jakarta.annotation.Nullable
  private List<String> errors = new ArrayList<>();

  public static final String JSON_PROPERTY_WARNINGS = "warnings";
  @jakarta.annotation.Nonnull
  private List<AdvisoryItem> warnings = new ArrayList<>();

  public SerializeBatteryUnitsResponse() { 
  }

  public SerializeBatteryUnitsResponse success(@jakarta.annotation.Nonnull SuccessEnum success) {
    this.success = success;
    return this;
  }

  /**
   * Get success
   * @return success
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public SuccessEnum getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccess(@jakarta.annotation.Nonnull SuccessEnum success) {
    this.success = success;
  }


  public SerializeBatteryUnitsResponse message(@jakarta.annotation.Nonnull String message) {
    this.message = message;
    return this;
  }

  /**
   * E.g. &#x60;Serialised 2 individual unit(s)&#x60; or &#x60;Serialised 1 individual unit(s), skipped 1&#x60;.
   * @return message
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMessage() {
    return message;
  }


  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMessage(@jakarta.annotation.Nonnull String message) {
    this.message = message;
  }


  public SerializeBatteryUnitsResponse count(@jakarta.annotation.Nonnull Integer count) {
    this.count = count;
    return this;
  }

  /**
   * Number of units actually created (equals &#x60;units.length&#x60;).
   * minimum: 1
   * @return count
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getCount() {
    return count;
  }


  @JsonProperty(JSON_PROPERTY_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCount(@jakarta.annotation.Nonnull Integer count) {
    this.count = count;
  }


  public SerializeBatteryUnitsResponse units(@jakarta.annotation.Nonnull List<BatteryUnitRow> units) {
    this.units = units;
    return this;
  }

  public SerializeBatteryUnitsResponse addUnitsItem(BatteryUnitRow unitsItem) {
    if (this.units == null) {
      this.units = new ArrayList<>();
    }
    this.units.add(unitsItem);
    return this;
  }

  /**
   * Get units
   * @return units
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UNITS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<BatteryUnitRow> getUnits() {
    return units;
  }


  @JsonProperty(JSON_PROPERTY_UNITS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUnits(@jakarta.annotation.Nonnull List<BatteryUnitRow> units) {
    this.units = units;
  }


  public SerializeBatteryUnitsResponse errors(@jakarta.annotation.Nullable List<String> errors) {
    this.errors = errors;
    return this;
  }

  public SerializeBatteryUnitsResponse addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Present only when some items were skipped — one plain-English string per skipped item, generally prefixed &#x60;[&lt;serialNumber&gt;]&#x60;.
   * @return errors
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getErrors() {
    return errors;
  }


  @JsonProperty(JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setErrors(@jakarta.annotation.Nullable List<String> errors) {
    this.errors = errors;
  }


  public SerializeBatteryUnitsResponse warnings(@jakarta.annotation.Nonnull List<AdvisoryItem> warnings) {
    this.warnings = warnings;
    return this;
  }

  public SerializeBatteryUnitsResponse addWarningsItem(AdvisoryItem warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * Non-blocking advisories. Carries a single note when the passport&#39;s &#x60;productId&#x60; is NOT a GS1 GTIN — the created units then have no scannable GS1 unit Digital Link (&#x60;/01/{gtin}/21/{serial}&#x60;) and resolve only via &#x60;/unit/{id}&#x60;. Empty &#x60;[]&#x60; for a GTIN-keyed passport.
   * @return warnings
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<AdvisoryItem> getWarnings() {
    return warnings;
  }


  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWarnings(@jakarta.annotation.Nonnull List<AdvisoryItem> warnings) {
    this.warnings = warnings;
  }


  /**
   * Return true if this SerializeBatteryUnitsResponse object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SerializeBatteryUnitsResponse serializeBatteryUnitsResponse = (SerializeBatteryUnitsResponse) o;
    return Objects.equals(this.success, serializeBatteryUnitsResponse.success) &&
        Objects.equals(this.message, serializeBatteryUnitsResponse.message) &&
        Objects.equals(this.count, serializeBatteryUnitsResponse.count) &&
        Objects.equals(this.units, serializeBatteryUnitsResponse.units) &&
        Objects.equals(this.errors, serializeBatteryUnitsResponse.errors) &&
        Objects.equals(this.warnings, serializeBatteryUnitsResponse.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message, count, units, errors, warnings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SerializeBatteryUnitsResponse {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    units: ").append(toIndentedString(units)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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

    // add `success` to the URL query string
    if (getSuccess() != null) {
      joiner.add(String.format("%ssuccess%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSuccess()))));
    }

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format("%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    // add `count` to the URL query string
    if (getCount() != null) {
      joiner.add(String.format("%scount%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCount()))));
    }

    // add `units` to the URL query string
    if (getUnits() != null) {
      for (int i = 0; i < getUnits().size(); i++) {
        if (getUnits().get(i) != null) {
          joiner.add(getUnits().get(i).toUrlQueryString(String.format("%sunits%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `errors` to the URL query string
    if (getErrors() != null) {
      for (int i = 0; i < getErrors().size(); i++) {
        joiner.add(String.format("%serrors%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getErrors().get(i)))));
      }
    }

    // add `warnings` to the URL query string
    if (getWarnings() != null) {
      for (int i = 0; i < getWarnings().size(); i++) {
        if (getWarnings().get(i) != null) {
          joiner.add(getWarnings().get(i).toUrlQueryString(String.format("%swarnings%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }
}

