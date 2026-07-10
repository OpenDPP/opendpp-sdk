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

import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * ValidateBatteryUnits200ResponseResultsInner
 */
@JsonPropertyOrder({
  ValidateBatteryUnits200ResponseResultsInner.JSON_PROPERTY_SERIAL_NUMBER,
  ValidateBatteryUnits200ResponseResultsInner.JSON_PROPERTY_OK,
  ValidateBatteryUnits200ResponseResultsInner.JSON_PROPERTY_ERROR
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class ValidateBatteryUnits200ResponseResultsInner extends HashMap<String, Object> {
  public static final String JSON_PROPERTY_SERIAL_NUMBER = "serialNumber";
  @jakarta.annotation.Nonnull
  private String serialNumber;

  public static final String JSON_PROPERTY_OK = "ok";
  @jakarta.annotation.Nonnull
  private Boolean ok;

  public static final String JSON_PROPERTY_ERROR = "error";
  @jakarta.annotation.Nullable
  private String error;

  public ValidateBatteryUnits200ResponseResultsInner() { 
  }

  public ValidateBatteryUnits200ResponseResultsInner serialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * Get serialNumber
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


  public ValidateBatteryUnits200ResponseResultsInner ok(@jakarta.annotation.Nonnull Boolean ok) {
    this.ok = ok;
    return this;
  }

  /**
   * Get ok
   * @return ok
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_OK)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getOk() {
    return ok;
  }


  @JsonProperty(JSON_PROPERTY_OK)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOk(@jakarta.annotation.Nonnull Boolean ok) {
    this.ok = ok;
  }


  public ValidateBatteryUnits200ResponseResultsInner error(@jakarta.annotation.Nullable String error) {
    this.error = error;
    return this;
  }

  /**
   * Present only when ok&#x3D;false.
   * @return error
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getError() {
    return error;
  }


  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setError(@jakarta.annotation.Nullable String error) {
    this.error = error;
  }

  /**
   * A container for additional, undeclared properties.
   * This is a holder for any undeclared properties as specified with
   * the 'additionalProperties' keyword in the OAS document.
   */
  private Map<String, Object> additionalProperties;

  /**
   * Set the additional (undeclared) property with the specified name and value.
   * If the property does not already exist, create it otherwise replace it.
   * @param key the name of the property
   * @param value the value of the property
   * @return self reference
   */
  @JsonAnySetter
  public ValidateBatteryUnits200ResponseResultsInner putAdditionalProperty(String key, Object value) {
    if (this.additionalProperties == null) {
        this.additionalProperties = new HashMap<String, Object>();
    }
    this.additionalProperties.put(key, value);
    return this;
  }

  /**
   * Return the additional (undeclared) properties.
   * @return the additional (undeclared) properties
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  /**
   * Return the additional (undeclared) property with the specified name.
   * @param key the name of the property
   * @return the additional (undeclared) property with the specified name
   */
  public Object getAdditionalProperty(String key) {
    if (this.additionalProperties == null) {
        return null;
    }
    return this.additionalProperties.get(key);
  }

  /**
   * Return true if this validateBatteryUnits_200_response_results_inner object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidateBatteryUnits200ResponseResultsInner validateBatteryUnits200ResponseResultsInner = (ValidateBatteryUnits200ResponseResultsInner) o;
    return Objects.equals(this.serialNumber, validateBatteryUnits200ResponseResultsInner.serialNumber) &&
        Objects.equals(this.ok, validateBatteryUnits200ResponseResultsInner.ok) &&
        Objects.equals(this.error, validateBatteryUnits200ResponseResultsInner.error)&&
        Objects.equals(this.additionalProperties, validateBatteryUnits200ResponseResultsInner.additionalProperties) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serialNumber, ok, error, super.hashCode(), additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidateBatteryUnits200ResponseResultsInner {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    ok: ").append(toIndentedString(ok)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

    // add `ok` to the URL query string
    if (getOk() != null) {
      joiner.add(String.format("%sok%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOk()))));
    }

    // add `error` to the URL query string
    if (getError() != null) {
      joiner.add(String.format("%serror%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getError()))));
    }

    return joiner.toString();
  }
}

