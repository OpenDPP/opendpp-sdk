/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.16.0
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Tombstone (HTTP 410): once a battery is recycled its passport has ceased to exist. This minimal record confirms the unit existed, that it was recycled and when, plus the (still living) model-passport link. Grants and owner credentials do not override the tombstone on the public URL; the underlying data is retained internally for the statutory retention window.
 */
@JsonPropertyOrder({
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_GRANULARITY,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_DPP_SCHEMA_VERSION,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_DPP_STATUS,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_LAST_UPDATED,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_ECONOMIC_OPERATOR_ID,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_FACILITY_ID,
  BatteryUnitTombstoneJsonLd.JSON_PROPERTY_CONTENT_SPECIFICATION_IDS,
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
  public static final String JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID = "digitalProductPassportId";
  @jakarta.annotation.Nonnull
  private URI digitalProductPassportId;

  public static final String JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER = "uniqueProductIdentifier";
  @jakarta.annotation.Nonnull
  private URI uniqueProductIdentifier;

  /**
   * Granularity level of the unique product identifier (EN 18223 4.1.2.2): an individually serialised unit is always &#x60;item&#x60;.
   */
  public enum GranularityEnum {
    ITEM(String.valueOf("item")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    GranularityEnum(String value) {
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
    public static GranularityEnum fromValue(String value) {
      for (GranularityEnum b : GranularityEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_GRANULARITY = "granularity";
  @jakarta.annotation.Nonnull
  private GranularityEnum granularity;

  /**
   * The reference standard whose schema the instance follows (EN 18223 Table 1): the dated designation of the standard the body&#39;s model and serialisation come from, so a consumer picks its parser by it. Not this node&#39;s API contract version — that is &#x60;GET /api/v1/version&#x60;, and it says nothing about the body&#39;s model.
   */
  public enum DppSchemaVersionEnum {
    EN_18223_2026(String.valueOf("EN 18223:2026")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    DppSchemaVersionEnum(String value) {
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
    public static DppSchemaVersionEnum fromValue(String value) {
      for (DppSchemaVersionEnum b : DppSchemaVersionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_DPP_SCHEMA_VERSION = "dppSchemaVersion";
  @jakarta.annotation.Nonnull
  private DppSchemaVersionEnum dppSchemaVersion;

  public static final String JSON_PROPERTY_DPP_STATUS = "dppStatus";
  @jakarta.annotation.Nonnull
  private String dppStatus;

  public static final String JSON_PROPERTY_LAST_UPDATED = "lastUpdated";
  @jakarta.annotation.Nonnull
  private OffsetDateTime lastUpdated;

  public static final String JSON_PROPERTY_ECONOMIC_OPERATOR_ID = "economicOperatorId";
  @jakarta.annotation.Nonnull
  private String economicOperatorId;

  public static final String JSON_PROPERTY_FACILITY_ID = "facilityId";
  @jakarta.annotation.Nullable
  private URI facilityId;

  public static final String JSON_PROPERTY_CONTENT_SPECIFICATION_IDS = "contentSpecificationIds";
  @jakarta.annotation.Nullable
  private List<URI> contentSpecificationIds = new ArrayList<>();

  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  @jakarta.annotation.Nullable
  private Object atContext = null;

  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    BATTERY_UNIT(String.valueOf("BatteryUnit")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

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
      return UNKNOWN_DEFAULT_OPEN_API;
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
    RECYCLED(String.valueOf("RECYCLED")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

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
      return UNKNOWN_DEFAULT_OPEN_API;
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
    THIS_BATTERY_HAS_BEEN_RECYCLED_ITS_BATTERY_PASSPORT_HAS_CEASED_TO_EXIST_REGULATION_EU_2023_1542_ART_77_8_(String.valueOf("This battery has been recycled. Its battery passport has ceased to exist (Regulation (EU) 2023/1542, Art. 77(8)).")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

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
      return UNKNOWN_DEFAULT_OPEN_API;
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

  public BatteryUnitTombstoneJsonLd digitalProductPassportId(@jakarta.annotation.Nonnull URI digitalProductPassportId) {
    this.digitalProductPassportId = digitalProductPassportId;
    return this;
  }

  /**
   * The identifier of this PASSPORT instance (EN 18223 Table 1 &#x60;digitalProductPassportId&#x60;): this node&#39;s own &#x60;/unit/{id}&#x60; URL for the individual serialised unit. It identifies the passport, **not** the product — the product&#39;s identifier is &#x60;uniqueProductIdentifier&#x60; below, and the two are never the same value. &#x60;@id&#x60; and &#x60;digitalLinkUri&#x60; carry the product&#39;s link, so this attribute differs from both.
   * @return digitalProductPassportId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getDigitalProductPassportId() {
    return digitalProductPassportId;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalProductPassportId(@jakarta.annotation.Nonnull URI digitalProductPassportId) {
    this.digitalProductPassportId = digitalProductPassportId;
  }


  public BatteryUnitTombstoneJsonLd uniqueProductIdentifier(@jakarta.annotation.Nonnull URI uniqueProductIdentifier) {
    this.uniqueProductIdentifier = uniqueProductIdentifier;
    return this;
  }

  /**
   * The identifier of the PRODUCT in its web-linkable form (EN 18219, EN 18223 Table 1 &#x60;uniqueProductIdentifier&#x60;): the GS1 Digital Link of the individual serialised unit, or an EN IEC 61406 Identification Link where the product carries no GS1 key. Distinct from &#x60;digitalProductPassportId&#x60; above, which identifies the passport.
   * @return uniqueProductIdentifier
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getUniqueProductIdentifier() {
    return uniqueProductIdentifier;
  }


  @JsonProperty(JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUniqueProductIdentifier(@jakarta.annotation.Nonnull URI uniqueProductIdentifier) {
    this.uniqueProductIdentifier = uniqueProductIdentifier;
  }


  public BatteryUnitTombstoneJsonLd granularity(@jakarta.annotation.Nonnull GranularityEnum granularity) {
    this.granularity = granularity;
    return this;
  }

  /**
   * Granularity level of the unique product identifier (EN 18223 4.1.2.2): an individually serialised unit is always &#x60;item&#x60;.
   * @return granularity
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_GRANULARITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public GranularityEnum getGranularity() {
    return granularity;
  }


  @JsonProperty(JSON_PROPERTY_GRANULARITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGranularity(@jakarta.annotation.Nonnull GranularityEnum granularity) {
    this.granularity = granularity;
  }


  public BatteryUnitTombstoneJsonLd dppSchemaVersion(@jakarta.annotation.Nonnull DppSchemaVersionEnum dppSchemaVersion) {
    this.dppSchemaVersion = dppSchemaVersion;
    return this;
  }

  /**
   * The reference standard whose schema the instance follows (EN 18223 Table 1): the dated designation of the standard the body&#39;s model and serialisation come from, so a consumer picks its parser by it. Not this node&#39;s API contract version — that is &#x60;GET /api/v1/version&#x60;, and it says nothing about the body&#39;s model.
   * @return dppSchemaVersion
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DPP_SCHEMA_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public DppSchemaVersionEnum getDppSchemaVersion() {
    return dppSchemaVersion;
  }


  @JsonProperty(JSON_PROPERTY_DPP_SCHEMA_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDppSchemaVersion(@jakarta.annotation.Nonnull DppSchemaVersionEnum dppSchemaVersion) {
    this.dppSchemaVersion = dppSchemaVersion;
  }


  public BatteryUnitTombstoneJsonLd dppStatus(@jakarta.annotation.Nonnull String dppStatus) {
    this.dppStatus = dppStatus;
    return this;
  }

  /**
   * Status of the DPP instance AS A DIGITAL RESOURCE (EN 18223 Table 1), not of the product. An OPEN vocabulary — Table 1&#39;s values are examples and a legal act may add more — so it is deliberately not an enum here. This node emits &#x60;active&#x60; for a published passport (&#x60;status&#x60; ACTIVE or RECALLED — a recalled product&#39;s passport is still maintained), &#x60;inactive&#x60; for a DRAFT, &#x60;archived&#x60; once DECOMMISSIONED or when the owner was off-boarded (&#x60;archivedAt&#x60;). A unit is &#x60;archived&#x60; once tombstoned (RECYCLED).
   * @return dppStatus
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DPP_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDppStatus() {
    return dppStatus;
  }


  @JsonProperty(JSON_PROPERTY_DPP_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDppStatus(@jakarta.annotation.Nonnull String dppStatus) {
    this.dppStatus = dppStatus;
  }


  public BatteryUnitTombstoneJsonLd lastUpdated(@jakarta.annotation.Nonnull OffsetDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

  /**
   * Date and time of the latest update to the instance (ISO 8601, UTC) — the same instant as &#x60;updatedAt&#x60;. Never null: EN 18223 gives it cardinality 1, and the database maintains the column on every write.
   * @return lastUpdated
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_LAST_UPDATED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getLastUpdated() {
    return lastUpdated;
  }


  @JsonProperty(JSON_PROPERTY_LAST_UPDATED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLastUpdated(@jakarta.annotation.Nonnull OffsetDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }


  public BatteryUnitTombstoneJsonLd economicOperatorId(@jakarta.annotation.Nonnull String economicOperatorId) {
    this.economicOperatorId = economicOperatorId;
    return this;
  }

  /**
   * The responsible economic operator&#39;s identifier in EN 18219 form (EN 18223 Table 1): ISO/IEC 6523 &#x60;ICD:identifier&#x60; for the operator&#39;s clause 6 scheme — VAT &#x60;0223&#x60;, DUNS &#x60;0060&#x60;, LEI &#x60;0199&#x60;, GLN &#x60;0088&#x60; — e.g. &#x60;0223:LT000000000001&#x60;. The scheme is &#x60;economicOperator.regIdScheme&#x60;; only an operator that predates the scheme declaration (&#x60;UNDECLARED&#x60;) is carried as registered. Never null: EN 18223 gives it cardinality 1, the operator is a non-null foreign key, and every serialisation venue loads that relation.
   * @return economicOperatorId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEconomicOperatorId() {
    return economicOperatorId;
  }


  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEconomicOperatorId(@jakarta.annotation.Nonnull String economicOperatorId) {
    this.economicOperatorId = economicOperatorId;
  }


  public BatteryUnitTombstoneJsonLd facilityId(@jakarta.annotation.Nullable URI facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  /**
   * The Unique Facility Identifier of the linked manufacturing facility as a GS1 Digital Link carrying the location GLN under AI 414 (&#x60;https://id.gs1.org/414/{gln}&#x60;). Optional in EN 18223 (cardinality 0..1): when no facility is linked the attribute is OMITTED — never &#x60;null&#x60;, never a placeholder — so it is not a &#x60;required&#x60; key and a reader tests for its presence.
   * @return facilityId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FACILITY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public URI getFacilityId() {
    return facilityId;
  }


  @JsonProperty(JSON_PROPERTY_FACILITY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFacilityId(@jakarta.annotation.Nullable URI facilityId) {
    this.facilityId = facilityId;
  }


  public BatteryUnitTombstoneJsonLd contentSpecificationIds(@jakarta.annotation.Nullable List<URI> contentSpecificationIds) {
    this.contentSpecificationIds = contentSpecificationIds;
    return this;
  }

  public BatteryUnitTombstoneJsonLd addContentSpecificationIdsItem(URI contentSpecificationIdsItem) {
    if (this.contentSpecificationIds == null) {
      this.contentSpecificationIds = new ArrayList<>();
    }
    this.contentSpecificationIds.add(contentSpecificationIdsItem);
    return this;
  }

  /**
   * The content specification(s) the instance follows: the URL of the ESPR category schema this node validated the metadata against (&#x60;GET /api/v1/schemas/{category}&#x60;). Empty when the metadata names no category. Optional in EN 18223 (cardinality 0..*), so it is not a &#x60;required&#x60; key — this node always sends it.
   * @return contentSpecificationIds
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CONTENT_SPECIFICATION_IDS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<URI> getContentSpecificationIds() {
    return contentSpecificationIds;
  }


  @JsonProperty(JSON_PROPERTY_CONTENT_SPECIFICATION_IDS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setContentSpecificationIds(@jakarta.annotation.Nullable List<URI> contentSpecificationIds) {
    this.contentSpecificationIds = contentSpecificationIds;
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
    return Objects.equals(this.digitalProductPassportId, batteryUnitTombstoneJsonLd.digitalProductPassportId) &&
        Objects.equals(this.uniqueProductIdentifier, batteryUnitTombstoneJsonLd.uniqueProductIdentifier) &&
        Objects.equals(this.granularity, batteryUnitTombstoneJsonLd.granularity) &&
        Objects.equals(this.dppSchemaVersion, batteryUnitTombstoneJsonLd.dppSchemaVersion) &&
        Objects.equals(this.dppStatus, batteryUnitTombstoneJsonLd.dppStatus) &&
        Objects.equals(this.lastUpdated, batteryUnitTombstoneJsonLd.lastUpdated) &&
        Objects.equals(this.economicOperatorId, batteryUnitTombstoneJsonLd.economicOperatorId) &&
        Objects.equals(this.facilityId, batteryUnitTombstoneJsonLd.facilityId) &&
        Objects.equals(this.contentSpecificationIds, batteryUnitTombstoneJsonLd.contentSpecificationIds) &&
        Objects.equals(this.atContext, batteryUnitTombstoneJsonLd.atContext) &&
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
    return Objects.hash(digitalProductPassportId, uniqueProductIdentifier, granularity, dppSchemaVersion, dppStatus, lastUpdated, economicOperatorId, facilityId, contentSpecificationIds, atContext, atType, atId, id, serialNumber, status, ceasedAt, notice, ofModelUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitTombstoneJsonLd {\n");
    sb.append("    digitalProductPassportId: ").append(toIndentedString(digitalProductPassportId)).append("\n");
    sb.append("    uniqueProductIdentifier: ").append(toIndentedString(uniqueProductIdentifier)).append("\n");
    sb.append("    granularity: ").append(toIndentedString(granularity)).append("\n");
    sb.append("    dppSchemaVersion: ").append(toIndentedString(dppSchemaVersion)).append("\n");
    sb.append("    dppStatus: ").append(toIndentedString(dppStatus)).append("\n");
    sb.append("    lastUpdated: ").append(toIndentedString(lastUpdated)).append("\n");
    sb.append("    economicOperatorId: ").append(toIndentedString(economicOperatorId)).append("\n");
    sb.append("    facilityId: ").append(toIndentedString(facilityId)).append("\n");
    sb.append("    contentSpecificationIds: ").append(toIndentedString(contentSpecificationIds)).append("\n");
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

    // add `digitalProductPassportId` to the URL query string
    if (getDigitalProductPassportId() != null) {
      joiner.add(String.format("%sdigitalProductPassportId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalProductPassportId()))));
    }

    // add `uniqueProductIdentifier` to the URL query string
    if (getUniqueProductIdentifier() != null) {
      joiner.add(String.format("%suniqueProductIdentifier%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUniqueProductIdentifier()))));
    }

    // add `granularity` to the URL query string
    if (getGranularity() != null) {
      joiner.add(String.format("%sgranularity%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGranularity()))));
    }

    // add `dppSchemaVersion` to the URL query string
    if (getDppSchemaVersion() != null) {
      joiner.add(String.format("%sdppSchemaVersion%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDppSchemaVersion()))));
    }

    // add `dppStatus` to the URL query string
    if (getDppStatus() != null) {
      joiner.add(String.format("%sdppStatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDppStatus()))));
    }

    // add `lastUpdated` to the URL query string
    if (getLastUpdated() != null) {
      joiner.add(String.format("%slastUpdated%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLastUpdated()))));
    }

    // add `economicOperatorId` to the URL query string
    if (getEconomicOperatorId() != null) {
      joiner.add(String.format("%seconomicOperatorId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEconomicOperatorId()))));
    }

    // add `facilityId` to the URL query string
    if (getFacilityId() != null) {
      joiner.add(String.format("%sfacilityId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getFacilityId()))));
    }

    // add `contentSpecificationIds` to the URL query string
    if (getContentSpecificationIds() != null) {
      for (int i = 0; i < getContentSpecificationIds().size(); i++) {
        if (getContentSpecificationIds().get(i) != null) {
          joiner.add(String.format("%scontentSpecificationIds%s%s=%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
              ApiClient.urlEncode(ApiClient.valueToString(getContentSpecificationIds().get(i)))));
        }
      }
    }

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

