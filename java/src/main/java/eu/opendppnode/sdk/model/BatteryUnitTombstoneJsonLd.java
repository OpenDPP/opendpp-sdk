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
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Art. 77(8) tombstone (HTTP 410): once a battery is recycled its passport has ceased to exist. This minimal record confirms the unit existed, that it was recycled and when, plus the (still living) model-passport link. Grants and owner credentials do not override the tombstone on the public URL; the underlying data is retained internally for the statutory retention window.
 */
@JsonPropertyOrder({
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_AT_CONTEXT,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_AT_TYPE,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_AT_ID,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_ID,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_SERIAL_NUMBER,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_STATUS,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_CEASED_AT,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_NOTICE,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_OF_MODEL_URL
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitTombstoneJsonLd {
  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  @jakarta.annotation.Nullable
  private Object atContext = null;

  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    BATTERY_UNIT(String.valueOf("BatteryUnit"));

    private String value;

    AtTypeEnum(String value) {
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
    public static AtTypeEnum fromValue(String value) {
      for (AtTypeEnum b : AtTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private AtTypeEnum atType;

  public static final String JSON_PROPERTY_AT_ID = "@id";
  @jakarta.annotation.Nonnull
  private URI atId;

  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_SERIAL_NUMBER = "serialNumber";
  @jakarta.annotation.Nonnull
  private String serialNumber;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    RECYCLED(String.valueOf("RECYCLED"));

    private String value;

    StatusEnum(String value) {
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
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_STATUS = "status";
  @jakarta.annotation.Nonnull
  private StatusEnum status;

  public static final String JSON_PROPERTY_CEASED_AT = "ceasedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime ceasedAt;

  /**
   * Gets or Sets notice
   */
  public enum NoticeEnum {
    THIS_BATTERY_HAS_BEEN_RECYCLED_ITS_BATTERY_PASSPORT_HAS_CEASED_TO_EXIST_REGULATION_EU_2023_1542_ART_77_8_(String.valueOf("This battery has been recycled. Its battery passport has ceased to exist (Regulation (EU) 2023/1542, Art. 77(8))."));

    private String value;

    NoticeEnum(String value) {
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
    public static NoticeEnum fromValue(String value) {
      for (NoticeEnum b : NoticeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_NOTICE = "notice";
  @jakarta.annotation.Nonnull
  private NoticeEnum notice;

  public static final String JSON_PROPERTY_OF_MODEL_URL = "ofModelUrl";
  @jakarta.annotation.Nullable
  private String ofModelUrl;

  public BatteryUnitTombstoneJsonLd() { 
  }

  public BatteryUnitTombstoneJsonLd atContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = atContext;
    return this;
  }

  /**
   * Get atContext
   * @return atContext
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getAtContext() {
    return atContext;
  }


  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = atContext;
  }


  public BatteryUnitTombstoneJsonLd atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
    return this;
  }

  /**
   * Get atType
   * @return atType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public AtTypeEnum getAtType() {
    return atType;
  }


  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
  }


  public BatteryUnitTombstoneJsonLd atId(@jakarta.annotation.Nonnull URI atId) {
    this.atId = atId;
    return this;
  }

  /**
   * Get atId
   * @return atId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getAtId() {
    return atId;
  }


  @JsonProperty(JSON_PROPERTY_AT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtId(@jakarta.annotation.Nonnull URI atId) {
    this.atId = atId;
  }


  public BatteryUnitTombstoneJsonLd id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setId(@jakarta.annotation.Nonnull String id) {
    this.id = id;
  }


  public BatteryUnitTombstoneJsonLd serialNumber(@jakarta.annotation.Nonnull String serialNumber) {
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


  public BatteryUnitTombstoneJsonLd status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public StatusEnum getStatus() {
    return status;
  }


  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStatus(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
  }


  public BatteryUnitTombstoneJsonLd ceasedAt(@jakarta.annotation.Nullable OffsetDateTime ceasedAt) {
    this.ceasedAt = ceasedAt;
    return this;
  }

  /**
   * When the unit&#39;s passport ceased to exist (stamped when the status transitioned to RECYCLED).
   * @return ceasedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CEASED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCeasedAt() {
    return ceasedAt;
  }


  @JsonProperty(JSON_PROPERTY_CEASED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCeasedAt(@jakarta.annotation.Nullable OffsetDateTime ceasedAt) {
    this.ceasedAt = ceasedAt;
  }


  public BatteryUnitTombstoneJsonLd notice(@jakarta.annotation.Nonnull NoticeEnum notice) {
    this.notice = notice;
    return this;
  }

  /**
   * Get notice
   * @return notice
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_NOTICE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public NoticeEnum getNotice() {
    return notice;
  }


  @JsonProperty(JSON_PROPERTY_NOTICE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNotice(@jakarta.annotation.Nonnull NoticeEnum notice) {
    this.notice = notice;
  }


  public BatteryUnitTombstoneJsonLd ofModelUrl(@jakarta.annotation.Nullable String ofModelUrl) {
    this.ofModelUrl = ofModelUrl;
    return this;
  }

  /**
   * Relative URL of the still-living SKU/type passport: &#x60;/passport/{passportId}&#x60;.
   * @return ofModelUrl
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_OF_MODEL_URL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOfModelUrl() {
    return ofModelUrl;
  }


  @JsonProperty(JSON_PROPERTY_OF_MODEL_URL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOfModelUrl(@jakarta.annotation.Nullable String ofModelUrl) {
    this.ofModelUrl = ofModelUrl;
  }


  /**
   * Return true if this BatteryUnitTombstoneJsonLd object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitTombstoneJsonLd batteryUnitTombstoneJsonLd = (BatteryUnitTombstoneJsonLd) o;
    return Objects.equals(this.atContext, batteryUnitTombstoneJsonLd.atContext) &&
        Objects.equals(this.atType, batteryUnitTombstoneJsonLd.atType) &&
        Objects.equals(this.atId, batteryUnitTombstoneJsonLd.atId) &&
        Objects.equals(this.id, batteryUnitTombstoneJsonLd.id) &&
        Objects.equals(this.serialNumber, batteryUnitTombstoneJsonLd.serialNumber) &&
        Objects.equals(this.status, batteryUnitTombstoneJsonLd.status) &&
        Objects.equals(this.ceasedAt, batteryUnitTombstoneJsonLd.ceasedAt) &&
        Objects.equals(this.notice, batteryUnitTombstoneJsonLd.notice) &&
        Objects.equals(this.ofModelUrl, batteryUnitTombstoneJsonLd.ofModelUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atContext, atType, atId, id, serialNumber, status, ceasedAt, notice, ofModelUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitTombstoneJsonLd {\n");
    sb.append("    atContext: ").append(toIndentedString(atContext)).append("\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    atId: ").append(toIndentedString(atId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    ceasedAt: ").append(toIndentedString(ceasedAt)).append("\n");
    sb.append("    notice: ").append(toIndentedString(notice)).append("\n");
    sb.append("    ofModelUrl: ").append(toIndentedString(ofModelUrl)).append("\n");
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

    // add `@context` to the URL query string
    if (getAtContext() != null) {
      joiner.add(String.format("%s@context%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtContext()))));
    }

    // add `@type` to the URL query string
    if (getAtType() != null) {
      joiner.add(String.format("%s@type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtType()))));
    }

    // add `@id` to the URL query string
    if (getAtId() != null) {
      joiner.add(String.format("%s@id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtId()))));
    }

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `serialNumber` to the URL query string
    if (getSerialNumber() != null) {
      joiner.add(String.format("%sserialNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `ceasedAt` to the URL query string
    if (getCeasedAt() != null) {
      joiner.add(String.format("%sceasedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCeasedAt()))));
    }

    // add `notice` to the URL query string
    if (getNotice() != null) {
      joiner.add(String.format("%snotice%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNotice()))));
    }

    // add `ofModelUrl` to the URL query string
    if (getOfModelUrl() != null) {
      joiner.add(String.format("%sofModelUrl%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOfModelUrl()))));
    }

    return joiner.toString();
  }
}

