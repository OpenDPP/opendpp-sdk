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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * PassportBulkResultResultsInner
 */
@JsonPropertyOrder({
  PassportBulkResultResultsInner.JSON_PROPERTY_PRODUCT_ID,
  PassportBulkResultResultsInner.JSON_PROPERTY_DIGITAL_LINK_URI,
  PassportBulkResultResultsInner.JSON_PROPERTY_VC_READY,
  PassportBulkResultResultsInner.JSON_PROPERTY_VC_READY_REASON,
  PassportBulkResultResultsInner.JSON_PROPERTY_WARNINGS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PassportBulkResultResultsInner {
  public static final String JSON_PROPERTY_PRODUCT_ID = "productId";
  @jakarta.annotation.Nonnull
  private String productId;

  public static final String JSON_PROPERTY_DIGITAL_LINK_URI = "digitalLinkUri";
  @jakarta.annotation.Nonnull
  private URI digitalLinkUri;

  public static final String JSON_PROPERTY_VC_READY = "vcReady";
  @jakarta.annotation.Nonnull
  private Boolean vcReady;

  public static final String JSON_PROPERTY_VC_READY_REASON = "vcReadyReason";
  private JsonNullable<String> vcReadyReason = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_WARNINGS = "warnings";
  @jakarta.annotation.Nullable
  private List<AdvisoryItem> warnings = new ArrayList<>();

  public PassportBulkResultResultsInner() { 
  }

  public PassportBulkResultResultsInner productId(@jakarta.annotation.Nonnull String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Get productId
   * @return productId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PRODUCT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProductId() {
    return productId;
  }


  @JsonProperty(JSON_PROPERTY_PRODUCT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProductId(@jakarta.annotation.Nonnull String productId) {
    this.productId = productId;
  }


  public PassportBulkResultResultsInner digitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
    return this;
  }

  /**
   * Generated GS1 Digital Link URI &#x60;https://opendpp-node.eu/{01|8003}/{productId}&#x60;.
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


  public PassportBulkResultResultsInner vcReady(@jakarta.annotation.Nonnull Boolean vcReady) {
    this.vcReady = vcReady;
    return this;
  }

  /**
   * #247: whether this row&#39;s passport can emit a UNTP Verifiable Credential — true only when a manufacturing facility with a country of production is linked. On a &#x60;dryRun&#x60; preview this reflects the EFFECTIVE facility after import (the row&#39;s facility, else the existing passport&#39;s preserved one).
   * @return vcReady
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getVcReady() {
    return vcReady;
  }


  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVcReady(@jakarta.annotation.Nonnull Boolean vcReady) {
    this.vcReady = vcReady;
  }


  public PassportBulkResultResultsInner vcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
    return this;
  }

  /**
   * Null when &#x60;vcReady&#x60; is true; otherwise a short, actionable reason (link a facility with a country of production).
   * @return vcReadyReason
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getVcReadyReason() {
        return vcReadyReason.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getVcReadyReason_JsonNullable() {
    return vcReadyReason;
  }
  
  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  public void setVcReadyReason_JsonNullable(JsonNullable<String> vcReadyReason) {
    this.vcReadyReason = vcReadyReason;
  }

  public void setVcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
  }


  public PassportBulkResultResultsInner warnings(@jakarta.annotation.Nullable List<AdvisoryItem> warnings) {
    this.warnings = warnings;
    return this;
  }

  public PassportBulkResultResultsInner addWarningsItem(AdvisoryItem warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * Per-row non-blocking advisories — the #249 non-GS1 \&quot;no scannable QR\&quot; note and the #400 PII-shape privacy advisory. Empty &#x60;[]&#x60; when the row is clean; never blocks the row.
   * @return warnings
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<AdvisoryItem> getWarnings() {
    return warnings;
  }


  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setWarnings(@jakarta.annotation.Nullable List<AdvisoryItem> warnings) {
    this.warnings = warnings;
  }


  /**
   * Return true if this PassportBulkResult_results_inner object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportBulkResultResultsInner passportBulkResultResultsInner = (PassportBulkResultResultsInner) o;
    return Objects.equals(this.productId, passportBulkResultResultsInner.productId) &&
        Objects.equals(this.digitalLinkUri, passportBulkResultResultsInner.digitalLinkUri) &&
        Objects.equals(this.vcReady, passportBulkResultResultsInner.vcReady) &&
        equalsNullable(this.vcReadyReason, passportBulkResultResultsInner.vcReadyReason) &&
        Objects.equals(this.warnings, passportBulkResultResultsInner.warnings);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, digitalLinkUri, vcReady, hashCodeNullable(vcReadyReason), warnings);
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
    sb.append("class PassportBulkResultResultsInner {\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    digitalLinkUri: ").append(toIndentedString(digitalLinkUri)).append("\n");
    sb.append("    vcReady: ").append(toIndentedString(vcReady)).append("\n");
    sb.append("    vcReadyReason: ").append(toIndentedString(vcReadyReason)).append("\n");
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

    // add `productId` to the URL query string
    if (getProductId() != null) {
      joiner.add(String.format("%sproductId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProductId()))));
    }

    // add `digitalLinkUri` to the URL query string
    if (getDigitalLinkUri() != null) {
      joiner.add(String.format("%sdigitalLinkUri%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLinkUri()))));
    }

    // add `vcReady` to the URL query string
    if (getVcReady() != null) {
      joiner.add(String.format("%svcReady%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReady()))));
    }

    // add `vcReadyReason` to the URL query string
    if (getVcReadyReason() != null) {
      joiner.add(String.format("%svcReadyReason%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReadyReason()))));
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

