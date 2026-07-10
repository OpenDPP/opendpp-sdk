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
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Standard error body. Authenticated-API errors include &#x60;success: false&#x60;; some endpoints (and all public resolution errors) omit &#x60;success&#x60; and return only &#x60;error&#x60; + &#x60;message&#x60;.
 */
@JsonPropertyOrder({
  Error.JSON_PROPERTY_SUCCESS,
  Error.JSON_PROPERTY_ERROR,
  Error.JSON_PROPERTY_MESSAGE,
  Error.JSON_PROPERTY_REQUEST_ID,
  Error.JSON_PROPERTY_CODE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class Error {
  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nullable
  private Boolean success;

  public static final String JSON_PROPERTY_ERROR = "error";
  @jakarta.annotation.Nonnull
  private String error;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @jakarta.annotation.Nonnull
  private String message;

  public static final String JSON_PROPERTY_REQUEST_ID = "requestId";
  @jakarta.annotation.Nullable
  private String requestId;

  /**
   * Optional MACHINE-STABLE error code for the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) — branch on this instead of parsing &#x60;message&#x60;. Present on the errors it covers (see src/constants/api-error-codes.ts), omitted otherwise.
   */
  public enum CodeEnum {
    OPERATOR_NOT_BOUND(String.valueOf("OPERATOR_NOT_BOUND")),
    
    OPERATOR_AMBIGUOUS(String.valueOf("OPERATOR_AMBIGUOUS")),
    
    OPERATOR_SCOPE_FORBIDDEN(String.valueOf("OPERATOR_SCOPE_FORBIDDEN")),
    
    GTIN_CHECK_DIGIT_INVALID(String.valueOf("GTIN_CHECK_DIGIT_INVALID")),
    
    GLN_CHECK_DIGIT_INVALID(String.valueOf("GLN_CHECK_DIGIT_INVALID")),
    
    COMPRESSED_DIGITAL_LINK(String.valueOf("COMPRESSED_DIGITAL_LINK")),
    
    PASSPORT_DUPLICATE(String.valueOf("PASSPORT_DUPLICATE")),
    
    PASSPORT_SEALED_IMMUTABLE(String.valueOf("PASSPORT_SEALED_IMMUTABLE")),
    
    CATEGORY_IMMUTABLE(String.valueOf("CATEGORY_IMMUTABLE")),
    
    FACILITY_NOT_FOUND(String.valueOf("FACILITY_NOT_FOUND")),
    
    FACILITY_DUPLICATE(String.valueOf("FACILITY_DUPLICATE")),
    
    WEBHOOK_NOT_FOUND(String.valueOf("WEBHOOK_NOT_FOUND")),
    
    WEBHOOK_LIMIT_REACHED(String.valueOf("WEBHOOK_LIMIT_REACHED")),
    
    WEBHOOK_URL_REJECTED(String.valueOf("WEBHOOK_URL_REJECTED"));

    private String value;

    CodeEnum(String value) {
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
    public static CodeEnum fromValue(String value) {
      for (CodeEnum b : CodeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_CODE = "code";
  @jakarta.annotation.Nullable
  private CodeEnum code;

  public Error() { 
  }

  public Error success(@jakarta.annotation.Nullable Boolean success) {
    this.success = success;
    return this;
  }

  /**
   * Always &#x60;false&#x60; when present. Omitted by public endpoints and some self-service endpoints.
   * @return success
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSuccess(@jakarta.annotation.Nullable Boolean success) {
    this.success = success;
  }


  public Error error(@jakarta.annotation.Nonnull String error) {
    this.error = error;
    return this;
  }

  /**
   * Short error title (usually the HTTP reason phrase).
   * @return error
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getError() {
    return error;
  }


  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setError(@jakarta.annotation.Nonnull String error) {
    this.error = error;
  }


  public Error message(@jakarta.annotation.Nonnull String message) {
    this.message = message;
    return this;
  }

  /**
   * Human-readable explanation.
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


  public Error requestId(@jakarta.annotation.Nullable String requestId) {
    this.requestId = requestId;
    return this;
  }

  /**
   * Correlation id for this request, also returned as the &#x60;X-Request-Id&#x60; response header on EVERY response. Present in generic (server-error / framework) bodies; quote it to support to correlate with server logs. Adopts a well-formed inbound &#x60;X-Request-Id&#x60; if you send one.
   * @return requestId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REQUEST_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getRequestId() {
    return requestId;
  }


  @JsonProperty(JSON_PROPERTY_REQUEST_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequestId(@jakarta.annotation.Nullable String requestId) {
    this.requestId = requestId;
  }


  public Error code(@jakarta.annotation.Nullable CodeEnum code) {
    this.code = code;
    return this;
  }

  /**
   * Optional MACHINE-STABLE error code for the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) — branch on this instead of parsing &#x60;message&#x60;. Present on the errors it covers (see src/constants/api-error-codes.ts), omitted otherwise.
   * @return code
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CODE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public CodeEnum getCode() {
    return code;
  }


  @JsonProperty(JSON_PROPERTY_CODE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCode(@jakarta.annotation.Nullable CodeEnum code) {
    this.code = code;
  }


  /**
   * Return true if this Error object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.success, error.success) &&
        Objects.equals(this.error, error.error) &&
        Objects.equals(this.message, error.message) &&
        Objects.equals(this.requestId, error.requestId) &&
        Objects.equals(this.code, error.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, error, message, requestId, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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

    // add `error` to the URL query string
    if (getError() != null) {
      joiner.add(String.format("%serror%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getError()))));
    }

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format("%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    // add `requestId` to the URL query string
    if (getRequestId() != null) {
      joiner.add(String.format("%srequestId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRequestId()))));
    }

    // add `code` to the URL query string
    if (getCode() != null) {
      joiner.add(String.format("%scode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCode()))));
    }

    return joiner.toString();
  }
}

