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
import java.net.URI;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * MintGtinCheckDigit200Response
 */
@JsonPropertyOrder({
  MintGtinCheckDigit200Response.JSON_PROPERTY_SUCCESS,
  MintGtinCheckDigit200Response.JSON_PROPERTY_GTIN,
  MintGtinCheckDigit200Response.JSON_PROPERTY_GS1_COMPANY_PREFIX,
  MintGtinCheckDigit200Response.JSON_PROPERTY_ITEM_REF,
  MintGtinCheckDigit200Response.JSON_PROPERTY_CHECK_DIGIT,
  MintGtinCheckDigit200Response.JSON_PROPERTY_DIGITAL_LINK,
  MintGtinCheckDigit200Response.JSON_PROPERTY_NOTE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class MintGtinCheckDigit200Response {
  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private Boolean success;

  public static final String JSON_PROPERTY_GTIN = "gtin";
  @jakarta.annotation.Nonnull
  private String gtin;

  public static final String JSON_PROPERTY_GS1_COMPANY_PREFIX = "gs1CompanyPrefix";
  @jakarta.annotation.Nonnull
  private String gs1CompanyPrefix;

  public static final String JSON_PROPERTY_ITEM_REF = "itemRef";
  @jakarta.annotation.Nonnull
  private String itemRef;

  public static final String JSON_PROPERTY_CHECK_DIGIT = "checkDigit";
  @jakarta.annotation.Nonnull
  private String checkDigit;

  public static final String JSON_PROPERTY_DIGITAL_LINK = "digitalLink";
  @jakarta.annotation.Nonnull
  private URI digitalLink;

  public static final String JSON_PROPERTY_NOTE = "note";
  @jakarta.annotation.Nonnull
  private String note;

  public MintGtinCheckDigit200Response() { 
  }

  public MintGtinCheckDigit200Response success(@jakarta.annotation.Nonnull Boolean success) {
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
  public Boolean getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccess(@jakarta.annotation.Nonnull Boolean success) {
    this.success = success;
  }


  public MintGtinCheckDigit200Response gtin(@jakarta.annotation.Nonnull String gtin) {
    this.gtin = gtin;
    return this;
  }

  /**
   * The 14-digit GTIN (&#x60;prefix + itemRef + check digit&#x60;).
   * @return gtin
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_GTIN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getGtin() {
    return gtin;
  }


  @JsonProperty(JSON_PROPERTY_GTIN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGtin(@jakarta.annotation.Nonnull String gtin) {
    this.gtin = gtin;
  }


  public MintGtinCheckDigit200Response gs1CompanyPrefix(@jakarta.annotation.Nonnull String gs1CompanyPrefix) {
    this.gs1CompanyPrefix = gs1CompanyPrefix;
    return this;
  }

  /**
   * Get gs1CompanyPrefix
   * @return gs1CompanyPrefix
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_GS1_COMPANY_PREFIX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getGs1CompanyPrefix() {
    return gs1CompanyPrefix;
  }


  @JsonProperty(JSON_PROPERTY_GS1_COMPANY_PREFIX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGs1CompanyPrefix(@jakarta.annotation.Nonnull String gs1CompanyPrefix) {
    this.gs1CompanyPrefix = gs1CompanyPrefix;
  }


  public MintGtinCheckDigit200Response itemRef(@jakarta.annotation.Nonnull String itemRef) {
    this.itemRef = itemRef;
    return this;
  }

  /**
   * Get itemRef
   * @return itemRef
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ITEM_REF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getItemRef() {
    return itemRef;
  }


  @JsonProperty(JSON_PROPERTY_ITEM_REF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setItemRef(@jakarta.annotation.Nonnull String itemRef) {
    this.itemRef = itemRef;
  }


  public MintGtinCheckDigit200Response checkDigit(@jakarta.annotation.Nonnull String checkDigit) {
    this.checkDigit = checkDigit;
    return this;
  }

  /**
   * The computed GS1 mod-10 check digit (the 14th digit).
   * @return checkDigit
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CHECK_DIGIT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCheckDigit() {
    return checkDigit;
  }


  @JsonProperty(JSON_PROPERTY_CHECK_DIGIT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCheckDigit(@jakarta.annotation.Nonnull String checkDigit) {
    this.checkDigit = checkDigit;
  }


  public MintGtinCheckDigit200Response digitalLink(@jakarta.annotation.Nonnull URI digitalLink) {
    this.digitalLink = digitalLink;
    return this;
  }

  /**
   * A Digital Link preview &#x60;https://opendpp-node.eu/01/{gtin}&#x60; — resolvable once a passport uses this GTIN as its &#x60;productId&#x60;.
   * @return digitalLink
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getDigitalLink() {
    return digitalLink;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalLink(@jakarta.annotation.Nonnull URI digitalLink) {
    this.digitalLink = digitalLink;
  }


  public MintGtinCheckDigit200Response note(@jakarta.annotation.Nonnull String note) {
    this.note = note;
    return this;
  }

  /**
   * The ownership caveat: OpenDPP computes only the check digit and never allocates a prefix or asserts ownership.
   * @return note
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_NOTE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNote() {
    return note;
  }


  @JsonProperty(JSON_PROPERTY_NOTE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNote(@jakarta.annotation.Nonnull String note) {
    this.note = note;
  }


  /**
   * Return true if this mintGtinCheckDigit_200_response object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MintGtinCheckDigit200Response mintGtinCheckDigit200Response = (MintGtinCheckDigit200Response) o;
    return Objects.equals(this.success, mintGtinCheckDigit200Response.success) &&
        Objects.equals(this.gtin, mintGtinCheckDigit200Response.gtin) &&
        Objects.equals(this.gs1CompanyPrefix, mintGtinCheckDigit200Response.gs1CompanyPrefix) &&
        Objects.equals(this.itemRef, mintGtinCheckDigit200Response.itemRef) &&
        Objects.equals(this.checkDigit, mintGtinCheckDigit200Response.checkDigit) &&
        Objects.equals(this.digitalLink, mintGtinCheckDigit200Response.digitalLink) &&
        Objects.equals(this.note, mintGtinCheckDigit200Response.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, gtin, gs1CompanyPrefix, itemRef, checkDigit, digitalLink, note);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MintGtinCheckDigit200Response {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    gtin: ").append(toIndentedString(gtin)).append("\n");
    sb.append("    gs1CompanyPrefix: ").append(toIndentedString(gs1CompanyPrefix)).append("\n");
    sb.append("    itemRef: ").append(toIndentedString(itemRef)).append("\n");
    sb.append("    checkDigit: ").append(toIndentedString(checkDigit)).append("\n");
    sb.append("    digitalLink: ").append(toIndentedString(digitalLink)).append("\n");
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
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

    // add `gtin` to the URL query string
    if (getGtin() != null) {
      joiner.add(String.format("%sgtin%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGtin()))));
    }

    // add `gs1CompanyPrefix` to the URL query string
    if (getGs1CompanyPrefix() != null) {
      joiner.add(String.format("%sgs1CompanyPrefix%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGs1CompanyPrefix()))));
    }

    // add `itemRef` to the URL query string
    if (getItemRef() != null) {
      joiner.add(String.format("%sitemRef%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getItemRef()))));
    }

    // add `checkDigit` to the URL query string
    if (getCheckDigit() != null) {
      joiner.add(String.format("%scheckDigit%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCheckDigit()))));
    }

    // add `digitalLink` to the URL query string
    if (getDigitalLink() != null) {
      joiner.add(String.format("%sdigitalLink%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLink()))));
    }

    // add `note` to the URL query string
    if (getNote() != null) {
      joiner.add(String.format("%snote%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNote()))));
    }

    return joiner.toString();
  }
}

