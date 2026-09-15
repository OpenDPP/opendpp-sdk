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
import eu.opendppnode.sdk.model.EconomicOperatorNode;
import eu.opendppnode.sdk.model.MerkleTreeAttestationProof;
import eu.opendppnode.sdk.model.PublicFacilityNode;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * The public, redacted JSON-LD Digital Product Passport document (&#x60;application/ld+json&#x60;), serialised as EN 18223:2026 clause 5.2 prescribes: the nine Table 1 header attributes and the server-owned document keys listed here, then EVERY data element of the passport body at the document root, once, under its elementId as the JSON key (hence &#x60;additionalProperties: true&#x60;). There is no wrapping object and no second copy — a consumer reads &#x60;category&#x60;, &#x60;chemistry&#x60;, &#x60;carbonFootprint&#x60; and the rest directly, and each such key expands under the one vocabulary through the inline &#x60;@context&#x60; term map. A key spelled like a reserved document key (&#x60;@context&#x60;, &#x60;@type&#x60;, &#x60;@id&#x60;, &#x60;id&#x60;, &#x60;productId&#x60;, &#x60;digitalLinkUri&#x60;, &#x60;productIdScheme&#x60;, the header attributes, &#x60;digitalSeal&#x60;, &#x60;signingPublicKey&#x60;, &#x60;status&#x60;, &#x60;archivedAt&#x60;, &#x60;retentionUntil&#x60;, &#x60;proof&#x60;, &#x60;createdAt&#x60;, &#x60;updatedAt&#x60;, &#x60;economicOperator&#x60;, &#x60;manufacturingFacility&#x60;) is never published as an element; its leaf still travels in &#x60;proof.redactedLeaves&#x60;. Tier-masked elements hold the literal string &#x60;[REDACTED - Privileged Access Required]&#x60;. Masking by tier: anonymous public callers lose the per-category restricted keys (category &#x60;batteries&#x60;: &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60;, &#x60;circularityAndDisassembly&#x60; — masked only when actually present) AND the owner-only key &#x60;facilityDetails&#x60;; legitimate-interest/authority grant holders lose only &#x60;facilityDetails&#x60;; owner-tier responses are unmasked and additionally include the facility street address fields. Note: &#x60;facilityDetails&#x60; is placeholder-masked in EVERY non-owner response, even when the underlying metadata never contained the key — in that case it has no entry in &#x60;proof.redactedLeaves&#x60;. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in &#x60;proof.redactedLeaves&#x60;, so the seal stays verifiable offline after redaction (see &#x60;MerkleTreeAttestationProof&#x60; for the reconstruction rule).
 */
@JsonPropertyOrder({
  PublicPassportJsonLd.JSON_PROPERTY_AT_CONTEXT,
  PublicPassportJsonLd.JSON_PROPERTY_AT_TYPE,
  PublicPassportJsonLd.JSON_PROPERTY_AT_ID,
  PublicPassportJsonLd.JSON_PROPERTY_ID,
  PublicPassportJsonLd.JSON_PROPERTY_PRODUCT_ID,
  PublicPassportJsonLd.JSON_PROPERTY_DIGITAL_LINK_URI,
  PublicPassportJsonLd.JSON_PROPERTY_PRODUCT_ID_SCHEME,
  PublicPassportJsonLd.JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID,
  PublicPassportJsonLd.JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER,
  PublicPassportJsonLd.JSON_PROPERTY_GRANULARITY,
  PublicPassportJsonLd.JSON_PROPERTY_DPP_SCHEMA_VERSION,
  PublicPassportJsonLd.JSON_PROPERTY_DPP_STATUS,
  PublicPassportJsonLd.JSON_PROPERTY_LAST_UPDATED,
  PublicPassportJsonLd.JSON_PROPERTY_ECONOMIC_OPERATOR_ID,
  PublicPassportJsonLd.JSON_PROPERTY_FACILITY_ID,
  PublicPassportJsonLd.JSON_PROPERTY_CONTENT_SPECIFICATION_IDS,
  PublicPassportJsonLd.JSON_PROPERTY_DIGITAL_SEAL,
  PublicPassportJsonLd.JSON_PROPERTY_SIGNING_PUBLIC_KEY,
  PublicPassportJsonLd.JSON_PROPERTY_STATUS,
  PublicPassportJsonLd.JSON_PROPERTY_ARCHIVED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_RETENTION_UNTIL,
  PublicPassportJsonLd.JSON_PROPERTY_PROOF,
  PublicPassportJsonLd.JSON_PROPERTY_CREATED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_UPDATED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_ECONOMIC_OPERATOR,
  PublicPassportJsonLd.JSON_PROPERTY_MANUFACTURING_FACILITY
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PublicPassportJsonLd {
  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  @jakarta.annotation.Nullable
  private Object atContext = null;

  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    DIGITAL_PRODUCT_PASSPORT(String.valueOf("DigitalProductPassport")),
    
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

  public static final String JSON_PROPERTY_PRODUCT_ID = "productId";
  @jakarta.annotation.Nonnull
  private String productId;

  public static final String JSON_PROPERTY_DIGITAL_LINK_URI = "digitalLinkUri";
  @jakarta.annotation.Nonnull
  private URI digitalLinkUri;

  /**
   * The EN 18219 clause 5 scheme &#x60;productId&#x60; was declared under at issue: &#x60;GS1_DIGITAL_LINK&#x60; (scheme 1 — a GS1 key carried as a GS1 Digital Link) or &#x60;IDENTIFICATION_LINK&#x60; (scheme 2 — an EN IEC 61406-1 Identification Link under this node&#39;s domain for a non-GS1 identifier). Stated beside the identifier so a registry can keep it unique across identifier domains (EN 18219 4.1.2). Immutable once issued.
   */
  public enum ProductIdSchemeEnum {
    GS1_DIGITAL_LINK(String.valueOf("GS1_DIGITAL_LINK")),
    
    IDENTIFICATION_LINK(String.valueOf("IDENTIFICATION_LINK")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ProductIdSchemeEnum(String value) {
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
    public static ProductIdSchemeEnum fromValue(String value) {
      for (ProductIdSchemeEnum b : ProductIdSchemeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_PRODUCT_ID_SCHEME = "productIdScheme";
  @jakarta.annotation.Nonnull
  private ProductIdSchemeEnum productIdScheme;

  public static final String JSON_PROPERTY_DIGITAL_PRODUCT_PASSPORT_ID = "digitalProductPassportId";
  @jakarta.annotation.Nonnull
  private URI digitalProductPassportId;

  public static final String JSON_PROPERTY_UNIQUE_PRODUCT_IDENTIFIER = "uniqueProductIdentifier";
  @jakarta.annotation.Nonnull
  private URI uniqueProductIdentifier;

  /**
   * Granularity level of the unique product identifier — EN 18223 4.1.2.2&#39;s closed enumeration — as the passport declared it at issue: &#x60;model&#x60; for a GTIN-keyed or Identification-Link passport, &#x60;item&#x60; for a serialised GRAI. &#x60;batch&#x60; is in the enumeration and never issued by this node.
   */
  public enum GranularityEnum {
    MODEL(String.valueOf("model")),
    
    BATCH(String.valueOf("batch")),
    
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

  public static final String JSON_PROPERTY_DIGITAL_SEAL = "digitalSeal";
  @jakarta.annotation.Nullable
  private String digitalSeal;

  public static final String JSON_PROPERTY_SIGNING_PUBLIC_KEY = "signingPublicKey";
  @jakarta.annotation.Nullable
  private String signingPublicKey;

  /**
   * Passport lifecycle status (serialized as &#x60;ACTIVE&#x60; when unset). &#x60;DRAFT&#x60; is only ever visible to owner-tier callers — public/grant resolution of a draft returns 404.
   */
  public enum StatusEnum {
    DRAFT(String.valueOf("DRAFT")),
    
    ACTIVE(String.valueOf("ACTIVE")),
    
    RECALLED(String.valueOf("RECALLED")),
    
    DECOMMISSIONED(String.valueOf("DECOMMISSIONED")),
    
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

  public static final String JSON_PROPERTY_ARCHIVED_AT = "archivedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime archivedAt;

  public static final String JSON_PROPERTY_RETENTION_UNTIL = "retentionUntil";
  @jakarta.annotation.Nullable
  private OffsetDateTime retentionUntil;

  public static final String JSON_PROPERTY_PROOF = "proof";
  @jakarta.annotation.Nullable
  private MerkleTreeAttestationProof proof;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public static final String JSON_PROPERTY_ECONOMIC_OPERATOR = "economicOperator";
  @jakarta.annotation.Nullable
  private EconomicOperatorNode economicOperator;

  public static final String JSON_PROPERTY_MANUFACTURING_FACILITY = "manufacturingFacility";
  @jakarta.annotation.Nullable
  private PublicFacilityNode manufacturingFacility;

  public PublicPassportJsonLd() { 
  }

  public PublicPassportJsonLd atContext(@jakarta.annotation.Nullable Object atContext) {
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


  public PublicPassportJsonLd atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
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


  public PublicPassportJsonLd atId(@jakarta.annotation.Nonnull URI atId) {
    this.atId = atId;
    return this;
  }

  /**
   * The passport&#39;s canonical GS1 Digital Link URI (same value as &#x60;digitalLinkUri&#x60;).
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


  public PublicPassportJsonLd id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Server-assigned passport UUID.
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


  public PublicPassportJsonLd productId(@jakarta.annotation.Nonnull String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Caller-supplied product identifier: a GTIN-14 (&#x60;^[0-9]{14}$&#x60; with valid GS1 modulo-10 check digit), a GRAI (&#x60;^[0-9]{14}[A-Za-z0-9]{0,16}$&#x60;), or a free-form SKU.
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


  public PublicPassportJsonLd digitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
    return this;
  }

  /**
   * SKU/type-level GS1 Digital Link URI: &#x60;{origin}/{01|8003}/{productId}&#x60; — the bare primary key. Individual units carry their physical serial under AI 21 (&#x60;…/21/{serialNumber}&#x60;).
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


  public PublicPassportJsonLd productIdScheme(@jakarta.annotation.Nonnull ProductIdSchemeEnum productIdScheme) {
    this.productIdScheme = productIdScheme;
    return this;
  }

  /**
   * The EN 18219 clause 5 scheme &#x60;productId&#x60; was declared under at issue: &#x60;GS1_DIGITAL_LINK&#x60; (scheme 1 — a GS1 key carried as a GS1 Digital Link) or &#x60;IDENTIFICATION_LINK&#x60; (scheme 2 — an EN IEC 61406-1 Identification Link under this node&#39;s domain for a non-GS1 identifier). Stated beside the identifier so a registry can keep it unique across identifier domains (EN 18219 4.1.2). Immutable once issued.
   * @return productIdScheme
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PRODUCT_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ProductIdSchemeEnum getProductIdScheme() {
    return productIdScheme;
  }


  @JsonProperty(JSON_PROPERTY_PRODUCT_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProductIdScheme(@jakarta.annotation.Nonnull ProductIdSchemeEnum productIdScheme) {
    this.productIdScheme = productIdScheme;
  }


  public PublicPassportJsonLd digitalProductPassportId(@jakarta.annotation.Nonnull URI digitalProductPassportId) {
    this.digitalProductPassportId = digitalProductPassportId;
    return this;
  }

  /**
   * The identifier of this PASSPORT instance (EN 18223 Table 1 &#x60;digitalProductPassportId&#x60;): this node&#39;s own &#x60;/passport/{id}&#x60; URL for the SKU/type passport. It identifies the passport, **not** the product — the product&#39;s identifier is &#x60;uniqueProductIdentifier&#x60; below, and the two are never the same value. &#x60;@id&#x60; and &#x60;digitalLinkUri&#x60; carry the product&#39;s link, so this attribute differs from both.
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


  public PublicPassportJsonLd uniqueProductIdentifier(@jakarta.annotation.Nonnull URI uniqueProductIdentifier) {
    this.uniqueProductIdentifier = uniqueProductIdentifier;
    return this;
  }

  /**
   * The identifier of the PRODUCT in its web-linkable form (EN 18219, EN 18223 Table 1 &#x60;uniqueProductIdentifier&#x60;): the GS1 Digital Link of the SKU/type passport, or an EN IEC 61406 Identification Link where the product carries no GS1 key. Distinct from &#x60;digitalProductPassportId&#x60; above, which identifies the passport.
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


  public PublicPassportJsonLd granularity(@jakarta.annotation.Nonnull GranularityEnum granularity) {
    this.granularity = granularity;
    return this;
  }

  /**
   * Granularity level of the unique product identifier — EN 18223 4.1.2.2&#39;s closed enumeration — as the passport declared it at issue: &#x60;model&#x60; for a GTIN-keyed or Identification-Link passport, &#x60;item&#x60; for a serialised GRAI. &#x60;batch&#x60; is in the enumeration and never issued by this node.
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


  public PublicPassportJsonLd dppSchemaVersion(@jakarta.annotation.Nonnull DppSchemaVersionEnum dppSchemaVersion) {
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


  public PublicPassportJsonLd dppStatus(@jakarta.annotation.Nonnull String dppStatus) {
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


  public PublicPassportJsonLd lastUpdated(@jakarta.annotation.Nonnull OffsetDateTime lastUpdated) {
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


  public PublicPassportJsonLd economicOperatorId(@jakarta.annotation.Nonnull String economicOperatorId) {
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


  public PublicPassportJsonLd facilityId(@jakarta.annotation.Nullable URI facilityId) {
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


  public PublicPassportJsonLd contentSpecificationIds(@jakarta.annotation.Nullable List<URI> contentSpecificationIds) {
    this.contentSpecificationIds = contentSpecificationIds;
    return this;
  }

  public PublicPassportJsonLd addContentSpecificationIdsItem(URI contentSpecificationIdsItem) {
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


  public PublicPassportJsonLd digitalSeal(@jakarta.annotation.Nullable String digitalSeal) {
    this.digitalSeal = digitalSeal;
    return this;
  }

  /**
   * ADVANCED electronic seal: base64 ECDSA prime256v1 (P-256) signature over the Merkle root of the key-sorted metadata. &#x60;null&#x60; when the passport has not been sealed.
   * @return digitalSeal
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DIGITAL_SEAL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDigitalSeal() {
    return digitalSeal;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_SEAL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalSeal(@jakarta.annotation.Nullable String digitalSeal) {
    this.digitalSeal = digitalSeal;
  }


  public PublicPassportJsonLd signingPublicKey(@jakarta.annotation.Nullable String signingPublicKey) {
    this.signingPublicKey = signingPublicKey;
    return this;
  }

  /**
   * PEM public key that verifies &#x60;digitalSeal&#x60;. &#x60;null&#x60; when unsealed.
   * @return signingPublicKey
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SIGNING_PUBLIC_KEY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSigningPublicKey() {
    return signingPublicKey;
  }


  @JsonProperty(JSON_PROPERTY_SIGNING_PUBLIC_KEY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSigningPublicKey(@jakarta.annotation.Nullable String signingPublicKey) {
    this.signingPublicKey = signingPublicKey;
  }


  public PublicPassportJsonLd status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Passport lifecycle status (serialized as &#x60;ACTIVE&#x60; when unset). &#x60;DRAFT&#x60; is only ever visible to owner-tier callers — public/grant resolution of a draft returns 404.
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


  public PublicPassportJsonLd archivedAt(@jakarta.annotation.Nullable OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
    return this;
  }

  /**
   * Soft-delete marker (owner off-boarded / decommissioned). Archived passports remain publicly resolvable (ESPR persistence duty).
   * @return archivedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getArchivedAt() {
    return archivedAt;
  }


  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setArchivedAt(@jakarta.annotation.Nullable OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
  }


  public PublicPassportJsonLd retentionUntil(@jakarta.annotation.Nullable OffsetDateTime retentionUntil) {
    this.retentionUntil = retentionUntil;
    return this;
  }

  /**
   * Minimum-availability deadline; the passport is never purged before this instant.
   * @return retentionUntil
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RETENTION_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRetentionUntil() {
    return retentionUntil;
  }


  @JsonProperty(JSON_PROPERTY_RETENTION_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRetentionUntil(@jakarta.annotation.Nullable OffsetDateTime retentionUntil) {
    this.retentionUntil = retentionUntil;
  }


  public PublicPassportJsonLd proof(@jakarta.annotation.Nullable MerkleTreeAttestationProof proof) {
    this.proof = proof;
    return this;
  }

  /**
   * Get proof
   * @return proof
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public MerkleTreeAttestationProof getProof() {
    return proof;
  }


  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProof(@jakarta.annotation.Nullable MerkleTreeAttestationProof proof) {
    this.proof = proof;
  }


  public PublicPassportJsonLd createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }


  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCreatedAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  public PublicPassportJsonLd updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }


  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUpdatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  public PublicPassportJsonLd economicOperator(@jakarta.annotation.Nullable EconomicOperatorNode economicOperator) {
    this.economicOperator = economicOperator;
    return this;
  }

  /**
   * Get economicOperator
   * @return economicOperator
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public EconomicOperatorNode getEconomicOperator() {
    return economicOperator;
  }


  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEconomicOperator(@jakarta.annotation.Nullable EconomicOperatorNode economicOperator) {
    this.economicOperator = economicOperator;
  }


  public PublicPassportJsonLd manufacturingFacility(@jakarta.annotation.Nullable PublicFacilityNode manufacturingFacility) {
    this.manufacturingFacility = manufacturingFacility;
    return this;
  }

  /**
   * Get manufacturingFacility
   * @return manufacturingFacility
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MANUFACTURING_FACILITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PublicFacilityNode getManufacturingFacility() {
    return manufacturingFacility;
  }


  @JsonProperty(JSON_PROPERTY_MANUFACTURING_FACILITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setManufacturingFacility(@jakarta.annotation.Nullable PublicFacilityNode manufacturingFacility) {
    this.manufacturingFacility = manufacturingFacility;
  }


  /**
   * Return true if this PublicPassportJsonLd object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicPassportJsonLd publicPassportJsonLd = (PublicPassportJsonLd) o;
    return Objects.equals(this.atContext, publicPassportJsonLd.atContext) &&
        Objects.equals(this.atType, publicPassportJsonLd.atType) &&
        Objects.equals(this.atId, publicPassportJsonLd.atId) &&
        Objects.equals(this.id, publicPassportJsonLd.id) &&
        Objects.equals(this.productId, publicPassportJsonLd.productId) &&
        Objects.equals(this.digitalLinkUri, publicPassportJsonLd.digitalLinkUri) &&
        Objects.equals(this.productIdScheme, publicPassportJsonLd.productIdScheme) &&
        Objects.equals(this.digitalProductPassportId, publicPassportJsonLd.digitalProductPassportId) &&
        Objects.equals(this.uniqueProductIdentifier, publicPassportJsonLd.uniqueProductIdentifier) &&
        Objects.equals(this.granularity, publicPassportJsonLd.granularity) &&
        Objects.equals(this.dppSchemaVersion, publicPassportJsonLd.dppSchemaVersion) &&
        Objects.equals(this.dppStatus, publicPassportJsonLd.dppStatus) &&
        Objects.equals(this.lastUpdated, publicPassportJsonLd.lastUpdated) &&
        Objects.equals(this.economicOperatorId, publicPassportJsonLd.economicOperatorId) &&
        Objects.equals(this.facilityId, publicPassportJsonLd.facilityId) &&
        Objects.equals(this.contentSpecificationIds, publicPassportJsonLd.contentSpecificationIds) &&
        Objects.equals(this.digitalSeal, publicPassportJsonLd.digitalSeal) &&
        Objects.equals(this.signingPublicKey, publicPassportJsonLd.signingPublicKey) &&
        Objects.equals(this.status, publicPassportJsonLd.status) &&
        Objects.equals(this.archivedAt, publicPassportJsonLd.archivedAt) &&
        Objects.equals(this.retentionUntil, publicPassportJsonLd.retentionUntil) &&
        Objects.equals(this.proof, publicPassportJsonLd.proof) &&
        Objects.equals(this.createdAt, publicPassportJsonLd.createdAt) &&
        Objects.equals(this.updatedAt, publicPassportJsonLd.updatedAt) &&
        Objects.equals(this.economicOperator, publicPassportJsonLd.economicOperator) &&
        Objects.equals(this.manufacturingFacility, publicPassportJsonLd.manufacturingFacility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atContext, atType, atId, id, productId, digitalLinkUri, productIdScheme, digitalProductPassportId, uniqueProductIdentifier, granularity, dppSchemaVersion, dppStatus, lastUpdated, economicOperatorId, facilityId, contentSpecificationIds, digitalSeal, signingPublicKey, status, archivedAt, retentionUntil, proof, createdAt, updatedAt, economicOperator, manufacturingFacility);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicPassportJsonLd {\n");
    sb.append("    atContext: ").append(toIndentedString(atContext)).append("\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    atId: ").append(toIndentedString(atId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    digitalLinkUri: ").append(toIndentedString(digitalLinkUri)).append("\n");
    sb.append("    productIdScheme: ").append(toIndentedString(productIdScheme)).append("\n");
    sb.append("    digitalProductPassportId: ").append(toIndentedString(digitalProductPassportId)).append("\n");
    sb.append("    uniqueProductIdentifier: ").append(toIndentedString(uniqueProductIdentifier)).append("\n");
    sb.append("    granularity: ").append(toIndentedString(granularity)).append("\n");
    sb.append("    dppSchemaVersion: ").append(toIndentedString(dppSchemaVersion)).append("\n");
    sb.append("    dppStatus: ").append(toIndentedString(dppStatus)).append("\n");
    sb.append("    lastUpdated: ").append(toIndentedString(lastUpdated)).append("\n");
    sb.append("    economicOperatorId: ").append(toIndentedString(economicOperatorId)).append("\n");
    sb.append("    facilityId: ").append(toIndentedString(facilityId)).append("\n");
    sb.append("    contentSpecificationIds: ").append(toIndentedString(contentSpecificationIds)).append("\n");
    sb.append("    digitalSeal: ").append(toIndentedString(digitalSeal)).append("\n");
    sb.append("    signingPublicKey: ").append(toIndentedString(signingPublicKey)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    archivedAt: ").append(toIndentedString(archivedAt)).append("\n");
    sb.append("    retentionUntil: ").append(toIndentedString(retentionUntil)).append("\n");
    sb.append("    proof: ").append(toIndentedString(proof)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    economicOperator: ").append(toIndentedString(economicOperator)).append("\n");
    sb.append("    manufacturingFacility: ").append(toIndentedString(manufacturingFacility)).append("\n");
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

    // add `productId` to the URL query string
    if (getProductId() != null) {
      joiner.add(String.format("%sproductId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProductId()))));
    }

    // add `digitalLinkUri` to the URL query string
    if (getDigitalLinkUri() != null) {
      joiner.add(String.format("%sdigitalLinkUri%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLinkUri()))));
    }

    // add `productIdScheme` to the URL query string
    if (getProductIdScheme() != null) {
      joiner.add(String.format("%sproductIdScheme%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProductIdScheme()))));
    }

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

    // add `digitalSeal` to the URL query string
    if (getDigitalSeal() != null) {
      joiner.add(String.format("%sdigitalSeal%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalSeal()))));
    }

    // add `signingPublicKey` to the URL query string
    if (getSigningPublicKey() != null) {
      joiner.add(String.format("%ssigningPublicKey%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSigningPublicKey()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `archivedAt` to the URL query string
    if (getArchivedAt() != null) {
      joiner.add(String.format("%sarchivedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getArchivedAt()))));
    }

    // add `retentionUntil` to the URL query string
    if (getRetentionUntil() != null) {
      joiner.add(String.format("%sretentionUntil%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRetentionUntil()))));
    }

    // add `proof` to the URL query string
    if (getProof() != null) {
      joiner.add(getProof().toUrlQueryString(prefix + "proof" + suffix));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `updatedAt` to the URL query string
    if (getUpdatedAt() != null) {
      joiner.add(String.format("%supdatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUpdatedAt()))));
    }

    // add `economicOperator` to the URL query string
    if (getEconomicOperator() != null) {
      joiner.add(getEconomicOperator().toUrlQueryString(prefix + "economicOperator" + suffix));
    }

    // add `manufacturingFacility` to the URL query string
    if (getManufacturingFacility() != null) {
      joiner.add(getManufacturingFacility().toUrlQueryString(prefix + "manufacturingFacility" + suffix));
    }

    return joiner.toString();
  }
}

