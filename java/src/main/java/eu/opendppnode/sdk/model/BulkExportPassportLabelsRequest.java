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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * BulkExportPassportLabelsRequest
 */
@JsonPropertyOrder({
  BulkExportPassportLabelsRequest.JSON_PROPERTY_IDS,
  BulkExportPassportLabelsRequest.JSON_PROPERTY_FORMAT,
  BulkExportPassportLabelsRequest.JSON_PROPERTY_SIZE,
  BulkExportPassportLabelsRequest.JSON_PROPERTY_ECL,
  BulkExportPassportLabelsRequest.JSON_PROPERTY_HRI
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BulkExportPassportLabelsRequest {
  public static final String JSON_PROPERTY_IDS = "ids";
  @jakarta.annotation.Nonnull
  private List<String> ids = new ArrayList<>();

  /**
   * Image format for every label in the ZIP.
   */
  public enum FormatEnum {
    PNG(String.valueOf("png")),
    
    SVG(String.valueOf("svg")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    FormatEnum(String value) {
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
    public static FormatEnum fromValue(String value) {
      for (FormatEnum b : FormatEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_FORMAT = "format";
  @jakarta.annotation.Nullable
  private FormatEnum format = FormatEnum.PNG;

  public static final String JSON_PROPERTY_SIZE = "size";
  @jakarta.annotation.Nullable
  private Integer size = 1024;

  /**
   * QR error-correction level (GS1 product-label guidance: &#x60;Q&#x60;).
   */
  public enum EclEnum {
    M(String.valueOf("M")),
    
    Q(String.valueOf("Q")),
    
    H(String.valueOf("H")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    EclEnum(String value) {
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
    public static EclEnum fromValue(String value) {
      for (EclEnum b : EclEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_ECL = "ecl";
  @jakarta.annotation.Nullable
  private EclEnum ecl = EclEnum.Q;

  public static final String JSON_PROPERTY_HRI = "hri";
  @jakarta.annotation.Nullable
  private Boolean hri = false;

  public BulkExportPassportLabelsRequest() { 
  }

  public BulkExportPassportLabelsRequest ids(@jakarta.annotation.Nonnull List<String> ids) {
    this.ids = ids;
    return this;
  }

  public BulkExportPassportLabelsRequest addIdsItem(String idsItem) {
    if (this.ids == null) {
      this.ids = new ArrayList<>();
    }
    this.ids.add(idsItem);
    return this;
  }

  /**
   * Passport UUIDs and/or &#x60;productId&#x60;s (GTIN-14/GRAI/SKU), 1–200 items. Each is resolved tenant-scoped; unresolvable / not-owned ids are skipped and listed in &#x60;manifest.json&#x60;.
   * @return ids
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IDS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getIds() {
    return ids;
  }


  @JsonProperty(JSON_PROPERTY_IDS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIds(@jakarta.annotation.Nonnull List<String> ids) {
    this.ids = ids;
  }


  public BulkExportPassportLabelsRequest format(@jakarta.annotation.Nullable FormatEnum format) {
    this.format = format;
    return this;
  }

  /**
   * Image format for every label in the ZIP.
   * @return format
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FORMAT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public FormatEnum getFormat() {
    return format;
  }


  @JsonProperty(JSON_PROPERTY_FORMAT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFormat(@jakarta.annotation.Nullable FormatEnum format) {
    this.format = format;
  }


  public BulkExportPassportLabelsRequest size(@jakarta.annotation.Nullable Integer size) {
    this.size = size;
    return this;
  }

  /**
   * Rendered width in px; clamped to 128–2048.
   * minimum: 128
   * maximum: 2048
   * @return size
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SIZE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Integer getSize() {
    return size;
  }


  @JsonProperty(JSON_PROPERTY_SIZE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSize(@jakarta.annotation.Nullable Integer size) {
    this.size = size;
  }


  public BulkExportPassportLabelsRequest ecl(@jakarta.annotation.Nullable EclEnum ecl) {
    this.ecl = ecl;
    return this;
  }

  /**
   * QR error-correction level (GS1 product-label guidance: &#x60;Q&#x60;).
   * @return ecl
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ECL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public EclEnum getEcl() {
    return ecl;
  }


  @JsonProperty(JSON_PROPERTY_ECL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEcl(@jakarta.annotation.Nullable EclEnum ecl) {
    this.ecl = ecl;
  }


  public BulkExportPassportLabelsRequest hri(@jakarta.annotation.Nullable Boolean hri) {
    this.hri = hri;
    return this;
  }

  /**
   * Overlay the GS1 Human-Readable Interpretation beneath each symbol. Requires &#x60;format: \&quot;svg\&quot;&#x60;.
   * @return hri
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_HRI)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getHri() {
    return hri;
  }


  @JsonProperty(JSON_PROPERTY_HRI)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHri(@jakarta.annotation.Nullable Boolean hri) {
    this.hri = hri;
  }


  /**
   * Return true if this bulkExportPassportLabels_request object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BulkExportPassportLabelsRequest bulkExportPassportLabelsRequest = (BulkExportPassportLabelsRequest) o;
    return Objects.equals(this.ids, bulkExportPassportLabelsRequest.ids) &&
        Objects.equals(this.format, bulkExportPassportLabelsRequest.format) &&
        Objects.equals(this.size, bulkExportPassportLabelsRequest.size) &&
        Objects.equals(this.ecl, bulkExportPassportLabelsRequest.ecl) &&
        Objects.equals(this.hri, bulkExportPassportLabelsRequest.hri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ids, format, size, ecl, hri);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkExportPassportLabelsRequest {\n");
    sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    ecl: ").append(toIndentedString(ecl)).append("\n");
    sb.append("    hri: ").append(toIndentedString(hri)).append("\n");
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

    // add `ids` to the URL query string
    if (getIds() != null) {
      for (int i = 0; i < getIds().size(); i++) {
        joiner.add(String.format("%sids%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getIds().get(i)))));
      }
    }

    // add `format` to the URL query string
    if (getFormat() != null) {
      joiner.add(String.format("%sformat%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getFormat()))));
    }

    // add `size` to the URL query string
    if (getSize() != null) {
      joiner.add(String.format("%ssize%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSize()))));
    }

    // add `ecl` to the URL query string
    if (getEcl() != null) {
      joiner.add(String.format("%secl%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEcl()))));
    }

    // add `hri` to the URL query string
    if (getHri() != null) {
      joiner.add(String.format("%shri%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getHri()))));
    }

    return joiner.toString();
  }
}

