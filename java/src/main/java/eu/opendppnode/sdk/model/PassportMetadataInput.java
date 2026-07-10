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
 * The ESPR product metadata payload. For non-draft ingestion and for the validate-only endpoints, &#x60;category&#x60; is mandatory and must be one of the 9 ESPR categories; each category then mandates its own field set (e.g. textiles require &#x60;fiberComposition&#x60;, &#x60;careInstructions&#x60;, &#x60;size&#x60;; batteries require &#x60;batteryCategory&#x60;, &#x60;chemistry&#x60;, &#x60;electrochemicalCapacity&#x60;, &#x60;durability&#x60;, &#x60;recycledContentShare&#x60;, &#x60;carbonFootprint&#x60;). For five categories — textiles, batteries, electronics, chemicals, construction — the authoritative per-category JSON Schema (required fields, value constraints, field help) is served live at &#x60;GET /api/v1/schemas/{category}&#x60;; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and &#x60;GET /api/v1/schemas/{category}&#x60; returns 404 for them. Cross-field rules are enforced on top: &#x60;materialComposition&#x60; (and textile &#x60;fiberComposition&#x60;) percentages must sum to 100 ±0.1, &#x60;originCountry&#x60; must be a real ISO 3166-1 alpha-2 code, textile hazardous-substance concentrations are checked against REACH ppm limits. A documented set of supplementary objects (e.g. &#x60;technicalProperties&#x60;, &#x60;environmentalFootprint&#x60;, &#x60;circularityAttributes&#x60;, &#x60;esgDueDiligence&#x60;, &#x60;detailedPerformance&#x60;) produce non-blocking &#x60;warnings&#x60; instead of &#x60;errors&#x60; when malformed. With &#x60;draft: true&#x60; (single ingestion only) validation is skipped entirely and any object is accepted.
 */
@JsonPropertyOrder({
  PassportMetadataInput.JSON_PROPERTY_CATEGORY,
  PassportMetadataInput.JSON_PROPERTY_ORIGIN_COUNTRY,
  PassportMetadataInput.JSON_PROPERTY_COMMODITY_CODE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PassportMetadataInput {
  /**
   * ESPR product category; selects the validation rules. Required whenever validation runs (i.e. always, except &#x60;draft: true&#x60; single ingestion).
   */
  public enum CategoryEnum {
    TEXTILES(String.valueOf("textiles")),
    
    BATTERIES(String.valueOf("batteries")),
    
    ELECTRONICS(String.valueOf("electronics")),
    
    COSMETICS(String.valueOf("cosmetics")),
    
    TOYS(String.valueOf("toys")),
    
    IRON_STEEL(String.valueOf("iron-steel")),
    
    ALUMINIUM(String.valueOf("aluminium")),
    
    CHEMICALS(String.valueOf("chemicals")),
    
    CONSTRUCTION(String.valueOf("construction"));

    private String value;

    CategoryEnum(String value) {
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
    public static CategoryEnum fromValue(String value) {
      for (CategoryEnum b : CategoryEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_CATEGORY = "category";
  @jakarta.annotation.Nullable
  private CategoryEnum category;

  public static final String JSON_PROPERTY_ORIGIN_COUNTRY = "originCountry";
  @jakarta.annotation.Nullable
  private String originCountry;

  public static final String JSON_PROPERTY_COMMODITY_CODE = "commodityCode";
  @jakarta.annotation.Nullable
  private String commodityCode;

  public PassportMetadataInput() { 
  }

  public PassportMetadataInput category(@jakarta.annotation.Nullable CategoryEnum category) {
    this.category = category;
    return this;
  }

  /**
   * ESPR product category; selects the validation rules. Required whenever validation runs (i.e. always, except &#x60;draft: true&#x60; single ingestion).
   * @return category
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CATEGORY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public CategoryEnum getCategory() {
    return category;
  }


  @JsonProperty(JSON_PROPERTY_CATEGORY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCategory(@jakarta.annotation.Nullable CategoryEnum category) {
    this.category = category;
  }


  public PassportMetadataInput originCountry(@jakarta.annotation.Nullable String originCountry) {
    this.originCountry = originCountry;
    return this;
  }

  /**
   * ISO 3166-1 alpha-2 country code (validated against the full 249-code set).
   * @return originCountry
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ORIGIN_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getOriginCountry() {
    return originCountry;
  }


  @JsonProperty(JSON_PROPERTY_ORIGIN_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOriginCountry(@jakarta.annotation.Nullable String originCountry) {
    this.originCountry = originCountry;
  }


  public PassportMetadataInput commodityCode(@jakarta.annotation.Nullable String commodityCode) {
    this.commodityCode = commodityCode;
    return this;
  }

  /**
   * Optional HS / TARIC commodity code (4–10 digits). Validated for every category when present (a malformed code is a 400). Not category-mandated, but required to project the passport into the EU DPP registry pointer (ESPR Art. 13).
   * @return commodityCode
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_COMMODITY_CODE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getCommodityCode() {
    return commodityCode;
  }


  @JsonProperty(JSON_PROPERTY_COMMODITY_CODE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCommodityCode(@jakarta.annotation.Nullable String commodityCode) {
    this.commodityCode = commodityCode;
  }


  /**
   * Return true if this PassportMetadataInput object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportMetadataInput passportMetadataInput = (PassportMetadataInput) o;
    return Objects.equals(this.category, passportMetadataInput.category) &&
        Objects.equals(this.originCountry, passportMetadataInput.originCountry) &&
        Objects.equals(this.commodityCode, passportMetadataInput.commodityCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, originCountry, commodityCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PassportMetadataInput {\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    originCountry: ").append(toIndentedString(originCountry)).append("\n");
    sb.append("    commodityCode: ").append(toIndentedString(commodityCode)).append("\n");
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

    // add `category` to the URL query string
    if (getCategory() != null) {
      joiner.add(String.format("%scategory%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCategory()))));
    }

    // add `originCountry` to the URL query string
    if (getOriginCountry() != null) {
      joiner.add(String.format("%soriginCountry%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOriginCountry()))));
    }

    // add `commodityCode` to the URL query string
    if (getCommodityCode() != null) {
      joiner.add(String.format("%scommodityCode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCommodityCode()))));
    }

    return joiner.toString();
  }
}

