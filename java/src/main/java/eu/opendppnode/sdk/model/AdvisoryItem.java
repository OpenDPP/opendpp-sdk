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
 * One non-blocking advisory on a response&#39;s &#x60;warnings[]&#x60; (a heads-up — the request still succeeded) or &#x60;notices[]&#x60; (informational — something helpful the API did). The &#x60;code&#x60; is a MACHINE-STABLE handle an interface can switch on, map to its own localized string, or link to docs; the human &#x60;message&#x60; (developer-facing) and &#x60;friendlyMessage&#x60; (end-user, localizable) wording may change, but the code will not.
 */
@JsonPropertyOrder({
  AdvisoryItem.JSON_PROPERTY_CODE,
  AdvisoryItem.JSON_PROPERTY_PATH,
  AdvisoryItem.JSON_PROPERTY_MESSAGE,
  AdvisoryItem.JSON_PROPERTY_FRIENDLY_MESSAGE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class AdvisoryItem {
  /**
   * Stable advisory code. WARNINGS: &#x60;NON_GS1_PRODUCT_ID&#x60; (the productId is not a GS1 GTIN/GRAI → no scannable GS1 link), &#x60;PII_SHAPE_DETECTED&#x60; (metadata looks like personal data), &#x60;UNIT_NO_SCANNABLE_LINK&#x60; (units under a non-GTIN passport have no scannable unit link), &#x60;DRAFT_DEMOTED&#x60; (draft:true took an already-published passport offline), &#x60;EORI_NOT_FOUND&#x60; (a declared EORI was not in the EU EOS register). NOTICES: &#x60;OPERATOR_AUTO_ATTRIBUTED&#x60; (operatorId omitted → the workspace&#39;s first bound operator was used), &#x60;GTIN_AUTO_COPIED&#x60; (a valid GTIN-14/GRAI productId was copied into metadata.gtin/metadata.grai).
   */
  public enum CodeEnum {
    NON_GS1_PRODUCT_ID(String.valueOf("NON_GS1_PRODUCT_ID")),
    
    PII_SHAPE_DETECTED(String.valueOf("PII_SHAPE_DETECTED")),
    
    UNIT_NO_SCANNABLE_LINK(String.valueOf("UNIT_NO_SCANNABLE_LINK")),
    
    DRAFT_DEMOTED(String.valueOf("DRAFT_DEMOTED")),
    
    EORI_NOT_FOUND(String.valueOf("EORI_NOT_FOUND")),
    
    OPERATOR_AUTO_ATTRIBUTED(String.valueOf("OPERATOR_AUTO_ATTRIBUTED")),
    
    GTIN_AUTO_COPIED(String.valueOf("GTIN_AUTO_COPIED"));

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
  @jakarta.annotation.Nonnull
  private CodeEnum code;

  public static final String JSON_PROPERTY_PATH = "path";
  @jakarta.annotation.Nullable
  private String path;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @jakarta.annotation.Nonnull
  private String message;

  public static final String JSON_PROPERTY_FRIENDLY_MESSAGE = "friendlyMessage";
  @jakarta.annotation.Nonnull
  private String friendlyMessage;

  public AdvisoryItem() { 
  }

  public AdvisoryItem code(@jakarta.annotation.Nonnull CodeEnum code) {
    this.code = code;
    return this;
  }

  /**
   * Stable advisory code. WARNINGS: &#x60;NON_GS1_PRODUCT_ID&#x60; (the productId is not a GS1 GTIN/GRAI → no scannable GS1 link), &#x60;PII_SHAPE_DETECTED&#x60; (metadata looks like personal data), &#x60;UNIT_NO_SCANNABLE_LINK&#x60; (units under a non-GTIN passport have no scannable unit link), &#x60;DRAFT_DEMOTED&#x60; (draft:true took an already-published passport offline), &#x60;EORI_NOT_FOUND&#x60; (a declared EORI was not in the EU EOS register). NOTICES: &#x60;OPERATOR_AUTO_ATTRIBUTED&#x60; (operatorId omitted → the workspace&#39;s first bound operator was used), &#x60;GTIN_AUTO_COPIED&#x60; (a valid GTIN-14/GRAI productId was copied into metadata.gtin/metadata.grai).
   * @return code
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public CodeEnum getCode() {
    return code;
  }


  @JsonProperty(JSON_PROPERTY_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCode(@jakarta.annotation.Nonnull CodeEnum code) {
    this.code = code;
  }


  public AdvisoryItem path(@jakarta.annotation.Nullable String path) {
    this.path = path;
    return this;
  }

  /**
   * The field the advisory is about (e.g. &#x60;productId&#x60;, &#x60;draft&#x60;, &#x60;regId&#x60;), when applicable.
   * @return path
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PATH)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPath() {
    return path;
  }


  @JsonProperty(JSON_PROPERTY_PATH)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPath(@jakarta.annotation.Nullable String path) {
    this.path = path;
  }


  public AdvisoryItem message(@jakarta.annotation.Nonnull String message) {
    this.message = message;
    return this;
  }

  /**
   * Developer-facing detail (English).
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


  public AdvisoryItem friendlyMessage(@jakarta.annotation.Nonnull String friendlyMessage) {
    this.friendlyMessage = friendlyMessage;
    return this;
  }

  /**
   * End-user-facing, localizable summary.
   * @return friendlyMessage
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_FRIENDLY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getFriendlyMessage() {
    return friendlyMessage;
  }


  @JsonProperty(JSON_PROPERTY_FRIENDLY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFriendlyMessage(@jakarta.annotation.Nonnull String friendlyMessage) {
    this.friendlyMessage = friendlyMessage;
  }


  /**
   * Return true if this AdvisoryItem object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdvisoryItem advisoryItem = (AdvisoryItem) o;
    return Objects.equals(this.code, advisoryItem.code) &&
        Objects.equals(this.path, advisoryItem.path) &&
        Objects.equals(this.message, advisoryItem.message) &&
        Objects.equals(this.friendlyMessage, advisoryItem.friendlyMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, path, message, friendlyMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvisoryItem {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    friendlyMessage: ").append(toIndentedString(friendlyMessage)).append("\n");
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

    // add `code` to the URL query string
    if (getCode() != null) {
      joiner.add(String.format("%scode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCode()))));
    }

    // add `path` to the URL query string
    if (getPath() != null) {
      joiner.add(String.format("%spath%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
    }

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format("%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    // add `friendlyMessage` to the URL query string
    if (getFriendlyMessage() != null) {
      joiner.add(String.format("%sfriendlyMessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getFriendlyMessage()))));
    }

    return joiner.toString();
  }
}

