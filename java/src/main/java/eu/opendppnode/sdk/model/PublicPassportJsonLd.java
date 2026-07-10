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
import eu.opendppnode.sdk.model.EconomicOperatorNode;
import eu.opendppnode.sdk.model.MerkleTreeAttestationProof;
import eu.opendppnode.sdk.model.PublicFacilityNode;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * The public, redacted JSON-LD Digital Product Passport document (&#x60;application/ld+json&#x60;). All listed top-level keys are ALWAYS present (&#x60;null&#x60; where not applicable). Additionally, every key of the (masked) &#x60;metadata&#x60; object — except the reserved document keys (&#x60;@context&#x60;, &#x60;@type&#x60;, &#x60;@id&#x60;, &#x60;id&#x60;, &#x60;productId&#x60;, &#x60;digitalLinkUri&#x60;, &#x60;digitalSeal&#x60;, &#x60;signingPublicKey&#x60;, &#x60;status&#x60;, &#x60;archivedAt&#x60;, &#x60;retentionUntil&#x60;, &#x60;proof&#x60;, &#x60;createdAt&#x60;, &#x60;updatedAt&#x60;, &#x60;economicOperator&#x60;, &#x60;manufacturingFacility&#x60;, &#x60;metadata&#x60;) — is ALSO flattened onto the document root for direct semantic-graph querying (hence &#x60;additionalProperties: true&#x60;); flattened values are identical to the corresponding &#x60;metadata&#x60; values, including redaction placeholders. Tier-masked metadata keys are replaced (in both places) with the literal string &#x60;[REDACTED - Privileged Access Required]&#x60;. Masking by tier: anonymous public callers lose the per-category restricted keys (category &#x60;batteries&#x60;: &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60;, &#x60;circularityAndDisassembly&#x60; — masked only when actually present) AND the owner-only key &#x60;facilityDetails&#x60;; legitimate-interest/authority grant holders lose only &#x60;facilityDetails&#x60;; owner-tier responses are unmasked and additionally include the facility street address fields. Note: &#x60;facilityDetails&#x60; is placeholder-masked in EVERY non-owner response, even when the underlying metadata never contained the key — in that case it has no entry in &#x60;proof.redactedLeaves&#x60;. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in &#x60;proof.redactedLeaves&#x60;, so the eIDAS seal stays verifiable offline after redaction (see &#x60;MerkleTreeAttestationProof&#x60; for the reconstruction rule).
 */
@JsonPropertyOrder({
  PublicPassportJsonLd.JSON_PROPERTY_AT_CONTEXT,
  PublicPassportJsonLd.JSON_PROPERTY_AT_TYPE,
  PublicPassportJsonLd.JSON_PROPERTY_AT_ID,
  PublicPassportJsonLd.JSON_PROPERTY_ID,
  PublicPassportJsonLd.JSON_PROPERTY_PRODUCT_ID,
  PublicPassportJsonLd.JSON_PROPERTY_DIGITAL_LINK_URI,
  PublicPassportJsonLd.JSON_PROPERTY_DIGITAL_SEAL,
  PublicPassportJsonLd.JSON_PROPERTY_SIGNING_PUBLIC_KEY,
  PublicPassportJsonLd.JSON_PROPERTY_STATUS,
  PublicPassportJsonLd.JSON_PROPERTY_ARCHIVED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_RETENTION_UNTIL,
  PublicPassportJsonLd.JSON_PROPERTY_PROOF,
  PublicPassportJsonLd.JSON_PROPERTY_CREATED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_UPDATED_AT,
  PublicPassportJsonLd.JSON_PROPERTY_ECONOMIC_OPERATOR,
  PublicPassportJsonLd.JSON_PROPERTY_MANUFACTURING_FACILITY,
  PublicPassportJsonLd.JSON_PROPERTY_METADATA
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

  public static final String JSON_PROPERTY_METADATA = "metadata";
  @jakarta.annotation.Nonnull
  private Map<String, Object> metadata = new HashMap<>();

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
   * SKU/type-level GS1 Digital Link URI: &#x60;{BASE_URL}/{01|8003}/{productId}&#x60; (AI-21 carries the passport UUID at SKU level; individual units carry their physical serial instead).
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


  public PublicPassportJsonLd digitalSeal(@jakarta.annotation.Nullable String digitalSeal) {
    this.digitalSeal = digitalSeal;
    return this;
  }

  /**
   * eIDAS ADVANCED electronic seal: base64 ECDSA prime256v1 (P-256) signature over the Merkle root of the key-sorted metadata. &#x60;null&#x60; when the passport has not been sealed.
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


  public PublicPassportJsonLd metadata(@jakarta.annotation.Nonnull Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  public PublicPassportJsonLd putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put(key, metadataItem);
    return this;
  }

  /**
   * The ESPR category metadata, tier-masked: keys above the caller&#39;s tier hold the literal string &#x60;[REDACTED - Privileged Access Required]&#x60; instead of their value.
   * @return metadata
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_METADATA)
  @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.ALWAYS)
  public Map<String, Object> getMetadata() {
    return metadata;
  }


  @JsonProperty(JSON_PROPERTY_METADATA)
  @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.ALWAYS)
  public void setMetadata(@jakarta.annotation.Nonnull Map<String, Object> metadata) {
    this.metadata = metadata;
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
        Objects.equals(this.digitalSeal, publicPassportJsonLd.digitalSeal) &&
        Objects.equals(this.signingPublicKey, publicPassportJsonLd.signingPublicKey) &&
        Objects.equals(this.status, publicPassportJsonLd.status) &&
        Objects.equals(this.archivedAt, publicPassportJsonLd.archivedAt) &&
        Objects.equals(this.retentionUntil, publicPassportJsonLd.retentionUntil) &&
        Objects.equals(this.proof, publicPassportJsonLd.proof) &&
        Objects.equals(this.createdAt, publicPassportJsonLd.createdAt) &&
        Objects.equals(this.updatedAt, publicPassportJsonLd.updatedAt) &&
        Objects.equals(this.economicOperator, publicPassportJsonLd.economicOperator) &&
        Objects.equals(this.manufacturingFacility, publicPassportJsonLd.manufacturingFacility) &&
        Objects.equals(this.metadata, publicPassportJsonLd.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atContext, atType, atId, id, productId, digitalLinkUri, digitalSeal, signingPublicKey, status, archivedAt, retentionUntil, proof, createdAt, updatedAt, economicOperator, manufacturingFacility, metadata);
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
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

    // add `metadata` to the URL query string
    if (getMetadata() != null) {
      for (String _key : getMetadata().keySet()) {
        joiner.add(String.format("%smetadata%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, _key, containerSuffix),
            getMetadata().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getMetadata().get(_key)))));
      }
    }

    return joiner.toString();
  }
}

