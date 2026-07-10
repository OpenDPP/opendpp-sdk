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
import eu.opendppnode.sdk.model.PassportListItemEconomicOperator;
import eu.opendppnode.sdk.model.PassportListItemProof;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One JSON-LD passport document as it appears in &#x60;GET /api/v1/passports&#x60; list responses. Same shape as &#x60;PublicPassportJsonLd&#x60; with list-specific divergences imposed by the route&#39;s declared response serialization: &#x60;economicOperator&#x60; never carries &#x60;role&#x60;; &#x60;manufacturingFacility&#x60; is always &#x60;null&#x60;; the &#x60;@context&#x60; term map (second array element) is emptied to &#x60;{}&#x60;; and &#x60;proof&#x60; is emptied to &#x60;{}&#x60; on sealed items (&#x60;null&#x60; on unsealed) — fetch the single passport for the verifiable proof block.
 */
@JsonPropertyOrder({
  PassportListItem.JSON_PROPERTY_AT_CONTEXT,
  PassportListItem.JSON_PROPERTY_AT_TYPE,
  PassportListItem.JSON_PROPERTY_AT_ID,
  PassportListItem.JSON_PROPERTY_ID,
  PassportListItem.JSON_PROPERTY_PRODUCT_ID,
  PassportListItem.JSON_PROPERTY_DIGITAL_LINK_URI,
  PassportListItem.JSON_PROPERTY_DIGITAL_SEAL,
  PassportListItem.JSON_PROPERTY_SIGNING_PUBLIC_KEY,
  PassportListItem.JSON_PROPERTY_STATUS,
  PassportListItem.JSON_PROPERTY_ARCHIVED_AT,
  PassportListItem.JSON_PROPERTY_RETENTION_UNTIL,
  PassportListItem.JSON_PROPERTY_PROOF,
  PassportListItem.JSON_PROPERTY_CREATED_AT,
  PassportListItem.JSON_PROPERTY_UPDATED_AT,
  PassportListItem.JSON_PROPERTY_ECONOMIC_OPERATOR,
  PassportListItem.JSON_PROPERTY_MANUFACTURING_FACILITY,
  PassportListItem.JSON_PROPERTY_METADATA
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PassportListItem extends HashMap<String, Object> {
  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  @jakarta.annotation.Nullable
  private Object atContext = null;

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private String atType;

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
  @jakarta.annotation.Nonnull
  private String digitalSeal;

  public static final String JSON_PROPERTY_SIGNING_PUBLIC_KEY = "signingPublicKey";
  @jakarta.annotation.Nonnull
  private String signingPublicKey;

  /**
   * Passport lifecycle status (serialized as &#x60;ACTIVE&#x60; when unset). &#x60;DRAFT&#x60; is only ever visible to owner-tier callers — public/grant resolution of a draft returns 404.
   */
  public enum StatusEnum {
    DRAFT(String.valueOf("DRAFT")),
    
    ACTIVE(String.valueOf("ACTIVE")),
    
    RECALLED(String.valueOf("RECALLED")),
    
    DECOMMISSIONED(String.valueOf("DECOMMISSIONED"));

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

  public static final String JSON_PROPERTY_ARCHIVED_AT = "archivedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime archivedAt;

  public static final String JSON_PROPERTY_RETENTION_UNTIL = "retentionUntil";
  @jakarta.annotation.Nonnull
  private OffsetDateTime retentionUntil;

  public static final String JSON_PROPERTY_PROOF = "proof";
  @jakarta.annotation.Nonnull
  private PassportListItemProof proof;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public static final String JSON_PROPERTY_ECONOMIC_OPERATOR = "economicOperator";
  @jakarta.annotation.Nonnull
  private PassportListItemEconomicOperator economicOperator;

  public static final String JSON_PROPERTY_MANUFACTURING_FACILITY = "manufacturingFacility";
  @jakarta.annotation.Nullable
  private Object manufacturingFacility = null;

  public static final String JSON_PROPERTY_METADATA = "metadata";
  @jakarta.annotation.Nonnull
  private Object metadata;

  public PassportListItem() { 
  }

  public PassportListItem atContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = atContext;
    return this;
  }

  /**
   * Exactly two entries: the context URL &#x60;https://opendpp-node.eu/contexts/dpp/v1&#x60; and an inline term map covering the 9 fixed DPP terms (&#x60;DigitalProductPassport&#x60;, &#x60;economicOperator&#x60;, &#x60;manufacturingFacility&#x60;, &#x60;metadata&#x60;, &#x60;digitalSeal&#x60;, &#x60;signingPublicKey&#x60;, &#x60;status&#x60;, &#x60;archivedAt&#x60;, &#x60;retentionUntil&#x60;) plus one generated term per metadata key (&#x60;https://opendpp-node.eu/contexts/dpp/v1#&lt;key&gt;&#x60;).
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


  public PassportListItem atType(@jakarta.annotation.Nonnull String atType) {
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
  public String getAtType() {
    return atType;
  }


  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtType(@jakarta.annotation.Nonnull String atType) {
    this.atType = atType;
  }


  public PassportListItem atId(@jakarta.annotation.Nonnull URI atId) {
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


  public PassportListItem id(@jakarta.annotation.Nonnull String id) {
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


  public PassportListItem productId(@jakarta.annotation.Nonnull String productId) {
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


  public PassportListItem digitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
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


  public PassportListItem digitalSeal(@jakarta.annotation.Nonnull String digitalSeal) {
    this.digitalSeal = digitalSeal;
    return this;
  }

  /**
   * eIDAS ADVANCED electronic seal: base64 ECDSA prime256v1 (P-256) signature over the Merkle root of the key-sorted metadata. &#x60;null&#x60; when the passport has not been sealed.
   * @return digitalSeal
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DIGITAL_SEAL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDigitalSeal() {
    return digitalSeal;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_SEAL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalSeal(@jakarta.annotation.Nonnull String digitalSeal) {
    this.digitalSeal = digitalSeal;
  }


  public PassportListItem signingPublicKey(@jakarta.annotation.Nonnull String signingPublicKey) {
    this.signingPublicKey = signingPublicKey;
    return this;
  }

  /**
   * PEM public key that verifies &#x60;digitalSeal&#x60;. &#x60;null&#x60; when unsealed.
   * @return signingPublicKey
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SIGNING_PUBLIC_KEY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSigningPublicKey() {
    return signingPublicKey;
  }


  @JsonProperty(JSON_PROPERTY_SIGNING_PUBLIC_KEY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSigningPublicKey(@jakarta.annotation.Nonnull String signingPublicKey) {
    this.signingPublicKey = signingPublicKey;
  }


  public PassportListItem status(@jakarta.annotation.Nonnull StatusEnum status) {
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


  public PassportListItem archivedAt(@jakarta.annotation.Nonnull OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
    return this;
  }

  /**
   * Soft-delete marker (owner off-boarded / decommissioned). Archived passports remain publicly resolvable (ESPR persistence duty).
   * @return archivedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getArchivedAt() {
    return archivedAt;
  }


  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setArchivedAt(@jakarta.annotation.Nonnull OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
  }


  public PassportListItem retentionUntil(@jakarta.annotation.Nonnull OffsetDateTime retentionUntil) {
    this.retentionUntil = retentionUntil;
    return this;
  }

  /**
   * Minimum-availability deadline; the passport is never purged before this instant.
   * @return retentionUntil
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_RETENTION_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRetentionUntil() {
    return retentionUntil;
  }


  @JsonProperty(JSON_PROPERTY_RETENTION_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRetentionUntil(@jakarta.annotation.Nonnull OffsetDateTime retentionUntil) {
    this.retentionUntil = retentionUntil;
  }


  public PassportListItem proof(@jakarta.annotation.Nonnull PassportListItemProof proof) {
    this.proof = proof;
    return this;
  }

  /**
   * Get proof
   * @return proof
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PassportListItemProof getProof() {
    return proof;
  }


  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProof(@jakarta.annotation.Nonnull PassportListItemProof proof) {
    this.proof = proof;
  }


  public PassportListItem createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  public PassportListItem updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
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


  public PassportListItem economicOperator(@jakarta.annotation.Nonnull PassportListItemEconomicOperator economicOperator) {
    this.economicOperator = economicOperator;
    return this;
  }

  /**
   * Get economicOperator
   * @return economicOperator
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PassportListItemEconomicOperator getEconomicOperator() {
    return economicOperator;
  }


  @JsonProperty(JSON_PROPERTY_ECONOMIC_OPERATOR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEconomicOperator(@jakarta.annotation.Nonnull PassportListItemEconomicOperator economicOperator) {
    this.economicOperator = economicOperator;
  }


  public PassportListItem manufacturingFacility(@jakarta.annotation.Nullable Object manufacturingFacility) {
    this.manufacturingFacility = manufacturingFacility;
    return this;
  }

  /**
   * Always &#x60;null&#x60; in list responses (facility nodes are only embedded on single-passport reads).
   * @return manufacturingFacility
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MANUFACTURING_FACILITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getManufacturingFacility() {
    return manufacturingFacility;
  }


  @JsonProperty(JSON_PROPERTY_MANUFACTURING_FACILITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setManufacturingFacility(@jakarta.annotation.Nullable Object manufacturingFacility) {
    this.manufacturingFacility = manufacturingFacility;
  }


  public PassportListItem metadata(@jakarta.annotation.Nonnull Object metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * The ESPR category metadata, tier-masked: keys above the caller&#39;s tier hold the literal string &#x60;[REDACTED - Privileged Access Required]&#x60; instead of their value.
   * @return metadata
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_METADATA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getMetadata() {
    return metadata;
  }


  @JsonProperty(JSON_PROPERTY_METADATA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMetadata(@jakarta.annotation.Nonnull Object metadata) {
    this.metadata = metadata;
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
  public PassportListItem putAdditionalProperty(String key, Object value) {
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
   * Return true if this PassportListItem object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportListItem passportListItem = (PassportListItem) o;
    return Objects.equals(this.atContext, passportListItem.atContext) &&
        Objects.equals(this.atType, passportListItem.atType) &&
        Objects.equals(this.atId, passportListItem.atId) &&
        Objects.equals(this.id, passportListItem.id) &&
        Objects.equals(this.productId, passportListItem.productId) &&
        Objects.equals(this.digitalLinkUri, passportListItem.digitalLinkUri) &&
        Objects.equals(this.digitalSeal, passportListItem.digitalSeal) &&
        Objects.equals(this.signingPublicKey, passportListItem.signingPublicKey) &&
        Objects.equals(this.status, passportListItem.status) &&
        Objects.equals(this.archivedAt, passportListItem.archivedAt) &&
        Objects.equals(this.retentionUntil, passportListItem.retentionUntil) &&
        Objects.equals(this.proof, passportListItem.proof) &&
        Objects.equals(this.createdAt, passportListItem.createdAt) &&
        Objects.equals(this.updatedAt, passportListItem.updatedAt) &&
        Objects.equals(this.economicOperator, passportListItem.economicOperator) &&
        Objects.equals(this.manufacturingFacility, passportListItem.manufacturingFacility) &&
        Objects.equals(this.metadata, passportListItem.metadata)&&
        Objects.equals(this.additionalProperties, passportListItem.additionalProperties) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atContext, atType, atId, id, productId, digitalLinkUri, digitalSeal, signingPublicKey, status, archivedAt, retentionUntil, proof, createdAt, updatedAt, economicOperator, manufacturingFacility, metadata, super.hashCode(), additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PassportListItem {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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
      joiner.add(String.format("%smanufacturingFacility%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getManufacturingFacility()))));
    }

    // add `metadata` to the URL query string
    if (getMetadata() != null) {
      joiner.add(String.format("%smetadata%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMetadata()))));
    }

    return joiner.toString();
  }
}

