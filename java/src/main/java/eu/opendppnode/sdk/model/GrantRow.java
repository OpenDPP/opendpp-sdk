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
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Tenant-facing projection of an access grant. The token hash, issuer user id, revoking actor and request IP are never exposed; raw capability tokens appear only in the one-time issuance/approval responses. All fields are always present (nullable ones serialize as &#x60;null&#x60;).
 */
@JsonPropertyOrder({
  GrantRow.JSON_PROPERTY_ID,
  GrantRow.JSON_PROPERTY_STATUS,
  GrantRow.JSON_PROPERTY_KIND,
  GrantRow.JSON_PROPERTY_GRANTEE_NAME,
  GrantRow.JSON_PROPERTY_GRANTEE_EMAIL,
  GrantRow.JSON_PROPERTY_ORGANIZATION,
  GrantRow.JSON_PROPERTY_PURPOSE,
  GrantRow.JSON_PROPERTY_SCOPE_TYPE,
  GrantRow.JSON_PROPERTY_PASSPORT_ID,
  GrantRow.JSON_PROPERTY_BATTERY_UNIT_ID,
  GrantRow.JSON_PROPERTY_ISSUER_TYPE,
  GrantRow.JSON_PROPERTY_ISSUER_EMAIL,
  GrantRow.JSON_PROPERTY_DECIDED_AT,
  GrantRow.JSON_PROPERTY_DECIDED_BY,
  GrantRow.JSON_PROPERTY_EXPIRES_AT,
  GrantRow.JSON_PROPERTY_REVOKED_AT,
  GrantRow.JSON_PROPERTY_LAST_USED_AT,
  GrantRow.JSON_PROPERTY_USE_COUNT,
  GrantRow.JSON_PROPERTY_CREATED_AT,
  GrantRow.JSON_PROPERTY_REVOCABLE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class GrantRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  /**
   * &#x60;PENDING&#x60; &#x3D; undecided third-party request (no token exists yet); &#x60;ACTIVE&#x60; &#x3D; usable token; &#x60;DENIED&#x60; &#x3D; rejected request; &#x60;REVOKED&#x60; &#x3D; soft-revoked.
   */
  public enum StatusEnum {
    PENDING(String.valueOf("PENDING")),
    
    ACTIVE(String.valueOf("ACTIVE")),
    
    DENIED(String.valueOf("DENIED")),
    
    REVOKED(String.valueOf("REVOKED")),
    
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

  /**
   * &#x60;LEGITIMATE_INTEREST&#x60; (&#x60;dpp_li_…&#x60; tokens, tenant-issued or approved from a request) or &#x60;AUTHORITY&#x60; (&#x60;dpp_auth_…&#x60; tokens, platform-issued for market surveillance; not tenant-revocable).
   */
  public enum KindEnum {
    LEGITIMATE_INTEREST(String.valueOf("LEGITIMATE_INTEREST")),
    
    AUTHORITY(String.valueOf("AUTHORITY")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    KindEnum(String value) {
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
    public static KindEnum fromValue(String value) {
      for (KindEnum b : KindEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_KIND = "kind";
  @jakarta.annotation.Nonnull
  private KindEnum kind;

  public static final String JSON_PROPERTY_GRANTEE_NAME = "granteeName";
  @jakarta.annotation.Nonnull
  private String granteeName;

  public static final String JSON_PROPERTY_GRANTEE_EMAIL = "granteeEmail";
  @jakarta.annotation.Nullable
  private String granteeEmail;

  public static final String JSON_PROPERTY_ORGANIZATION = "organization";
  @jakarta.annotation.Nullable
  private String organization;

  public static final String JSON_PROPERTY_PURPOSE = "purpose";
  @jakarta.annotation.Nullable
  private String purpose;

  /**
   * What the token unlocks on the public resolvers: a single battery unit, a single passport, or the whole workspace.
   */
  public enum ScopeTypeEnum {
    UNIT(String.valueOf("UNIT")),
    
    PASSPORT(String.valueOf("PASSPORT")),
    
    TENANT(String.valueOf("TENANT")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ScopeTypeEnum(String value) {
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
    public static ScopeTypeEnum fromValue(String value) {
      for (ScopeTypeEnum b : ScopeTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_SCOPE_TYPE = "scopeType";
  @jakarta.annotation.Nonnull
  private ScopeTypeEnum scopeType;

  public static final String JSON_PROPERTY_PASSPORT_ID = "passportId";
  @jakarta.annotation.Nullable
  private String passportId;

  public static final String JSON_PROPERTY_BATTERY_UNIT_ID = "batteryUnitId";
  @jakarta.annotation.Nullable
  private String batteryUnitId;

  /**
   * &#x60;TENANT&#x60; &#x3D; issued directly via this API; &#x60;REQUEST&#x60; &#x3D; submitted by a third party through the hosted request-access page; &#x60;PLATFORM&#x60; &#x3D; platform-admin-issued (AUTHORITY grants).
   */
  public enum IssuerTypeEnum {
    TENANT(String.valueOf("TENANT")),
    
    PLATFORM(String.valueOf("PLATFORM")),
    
    REQUEST(String.valueOf("REQUEST")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    IssuerTypeEnum(String value) {
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
    public static IssuerTypeEnum fromValue(String value) {
      for (IssuerTypeEnum b : IssuerTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_ISSUER_TYPE = "issuerType";
  @jakarta.annotation.Nonnull
  private IssuerTypeEnum issuerType;

  public static final String JSON_PROPERTY_ISSUER_EMAIL = "issuerEmail";
  @jakarta.annotation.Nullable
  private String issuerEmail;

  public static final String JSON_PROPERTY_DECIDED_AT = "decidedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime decidedAt;

  public static final String JSON_PROPERTY_DECIDED_BY = "decidedBy";
  @jakarta.annotation.Nullable
  private String decidedBy;

  public static final String JSON_PROPERTY_EXPIRES_AT = "expiresAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime expiresAt;

  public static final String JSON_PROPERTY_REVOKED_AT = "revokedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime revokedAt;

  public static final String JSON_PROPERTY_LAST_USED_AT = "lastUsedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime lastUsedAt;

  public static final String JSON_PROPERTY_USE_COUNT = "useCount";
  @jakarta.annotation.Nonnull
  private Integer useCount;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_REVOCABLE = "revocable";
  @jakarta.annotation.Nonnull
  private Boolean revocable;

  public GrantRow() { 
  }

  public GrantRow id(@jakarta.annotation.Nonnull String id) {
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


  public GrantRow status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * &#x60;PENDING&#x60; &#x3D; undecided third-party request (no token exists yet); &#x60;ACTIVE&#x60; &#x3D; usable token; &#x60;DENIED&#x60; &#x3D; rejected request; &#x60;REVOKED&#x60; &#x3D; soft-revoked.
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


  public GrantRow kind(@jakarta.annotation.Nonnull KindEnum kind) {
    this.kind = kind;
    return this;
  }

  /**
   * &#x60;LEGITIMATE_INTEREST&#x60; (&#x60;dpp_li_…&#x60; tokens, tenant-issued or approved from a request) or &#x60;AUTHORITY&#x60; (&#x60;dpp_auth_…&#x60; tokens, platform-issued for market surveillance; not tenant-revocable).
   * @return kind
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_KIND)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public KindEnum getKind() {
    return kind;
  }


  @JsonProperty(JSON_PROPERTY_KIND)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setKind(@jakarta.annotation.Nonnull KindEnum kind) {
    this.kind = kind;
  }


  public GrantRow granteeName(@jakarta.annotation.Nonnull String granteeName) {
    this.granteeName = granteeName;
    return this;
  }

  /**
   * Get granteeName
   * @return granteeName
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_GRANTEE_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getGranteeName() {
    return granteeName;
  }


  @JsonProperty(JSON_PROPERTY_GRANTEE_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGranteeName(@jakarta.annotation.Nonnull String granteeName) {
    this.granteeName = granteeName;
  }


  public GrantRow granteeEmail(@jakarta.annotation.Nullable String granteeEmail) {
    this.granteeEmail = granteeEmail;
    return this;
  }

  /**
   * Get granteeEmail
   * @return granteeEmail
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_GRANTEE_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getGranteeEmail() {
    return granteeEmail;
  }


  @JsonProperty(JSON_PROPERTY_GRANTEE_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGranteeEmail(@jakarta.annotation.Nullable String granteeEmail) {
    this.granteeEmail = granteeEmail;
  }


  public GrantRow organization(@jakarta.annotation.Nullable String organization) {
    this.organization = organization;
    return this;
  }

  /**
   * Get organization
   * @return organization
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ORGANIZATION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOrganization() {
    return organization;
  }


  @JsonProperty(JSON_PROPERTY_ORGANIZATION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOrganization(@jakarta.annotation.Nullable String organization) {
    this.organization = organization;
  }


  public GrantRow purpose(@jakarta.annotation.Nullable String purpose) {
    this.purpose = purpose;
    return this;
  }

  /**
   * The stated legitimate interest.
   * @return purpose
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPurpose() {
    return purpose;
  }


  @JsonProperty(JSON_PROPERTY_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPurpose(@jakarta.annotation.Nullable String purpose) {
    this.purpose = purpose;
  }


  public GrantRow scopeType(@jakarta.annotation.Nonnull ScopeTypeEnum scopeType) {
    this.scopeType = scopeType;
    return this;
  }

  /**
   * What the token unlocks on the public resolvers: a single battery unit, a single passport, or the whole workspace.
   * @return scopeType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SCOPE_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ScopeTypeEnum getScopeType() {
    return scopeType;
  }


  @JsonProperty(JSON_PROPERTY_SCOPE_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScopeType(@jakarta.annotation.Nonnull ScopeTypeEnum scopeType) {
    this.scopeType = scopeType;
  }


  public GrantRow passportId(@jakarta.annotation.Nullable String passportId) {
    this.passportId = passportId;
    return this;
  }

  /**
   * Set for &#x60;PASSPORT&#x60; scope, and also for &#x60;UNIT&#x60; scope (the unit&#39;s parent passport). &#x60;null&#x60; for &#x60;TENANT&#x60; scope.
   * @return passportId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPassportId() {
    return passportId;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPassportId(@jakarta.annotation.Nullable String passportId) {
    this.passportId = passportId;
  }


  public GrantRow batteryUnitId(@jakarta.annotation.Nullable String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
    return this;
  }

  /**
   * Set only for &#x60;UNIT&#x60; scope.
   * @return batteryUnitId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getBatteryUnitId() {
    return batteryUnitId;
  }


  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setBatteryUnitId(@jakarta.annotation.Nullable String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
  }


  public GrantRow issuerType(@jakarta.annotation.Nonnull IssuerTypeEnum issuerType) {
    this.issuerType = issuerType;
    return this;
  }

  /**
   * &#x60;TENANT&#x60; &#x3D; issued directly via this API; &#x60;REQUEST&#x60; &#x3D; submitted by a third party through the hosted request-access page; &#x60;PLATFORM&#x60; &#x3D; platform-admin-issued (AUTHORITY grants).
   * @return issuerType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ISSUER_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public IssuerTypeEnum getIssuerType() {
    return issuerType;
  }


  @JsonProperty(JSON_PROPERTY_ISSUER_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIssuerType(@jakarta.annotation.Nonnull IssuerTypeEnum issuerType) {
    this.issuerType = issuerType;
  }


  public GrantRow issuerEmail(@jakarta.annotation.Nullable String issuerEmail) {
    this.issuerEmail = issuerEmail;
    return this;
  }

  /**
   * E-mail of the issuing user; &#x60;null&#x60; when issued by an API key or created from a public request.
   * @return issuerEmail
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ISSUER_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getIssuerEmail() {
    return issuerEmail;
  }


  @JsonProperty(JSON_PROPERTY_ISSUER_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIssuerEmail(@jakarta.annotation.Nullable String issuerEmail) {
    this.issuerEmail = issuerEmail;
  }


  public GrantRow decidedAt(@jakarta.annotation.Nullable OffsetDateTime decidedAt) {
    this.decidedAt = decidedAt;
    return this;
  }

  /**
   * When a PENDING request was approved/denied; &#x60;null&#x60; for direct issuances.
   * @return decidedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DECIDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getDecidedAt() {
    return decidedAt;
  }


  @JsonProperty(JSON_PROPERTY_DECIDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDecidedAt(@jakarta.annotation.Nullable OffsetDateTime decidedAt) {
    this.decidedAt = decidedAt;
  }


  public GrantRow decidedBy(@jakarta.annotation.Nullable String decidedBy) {
    this.decidedBy = decidedBy;
    return this;
  }

  /**
   * The deciding actor: a user e-mail, &#x60;API_KEY_&lt;keyId&gt;&#x60; when decided via an API key, or the literal &#x60;unknown&#x60; in degenerate authentication states.
   * @return decidedBy
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DECIDED_BY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDecidedBy() {
    return decidedBy;
  }


  @JsonProperty(JSON_PROPERTY_DECIDED_BY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDecidedBy(@jakarta.annotation.Nullable String decidedBy) {
    this.decidedBy = decidedBy;
  }


  public GrantRow expiresAt(@jakarta.annotation.Nonnull OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  /**
   * Hard expiry; the public resolvers reject the token after this instant. PENDING requests carry a provisional 90-day expiry that is replaced on approval.
   * @return expiresAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EXPIRES_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }


  @JsonProperty(JSON_PROPERTY_EXPIRES_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setExpiresAt(@jakarta.annotation.Nonnull OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }


  public GrantRow revokedAt(@jakarta.annotation.Nullable OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
    return this;
  }

  /**
   * Get revokedAt
   * @return revokedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REVOKED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRevokedAt() {
    return revokedAt;
  }


  @JsonProperty(JSON_PROPERTY_REVOKED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRevokedAt(@jakarta.annotation.Nullable OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
  }


  public GrantRow lastUsedAt(@jakarta.annotation.Nullable OffsetDateTime lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
    return this;
  }

  /**
   * Last successful use on a public resolver (book-kept best-effort).
   * @return lastUsedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_LAST_USED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getLastUsedAt() {
    return lastUsedAt;
  }


  @JsonProperty(JSON_PROPERTY_LAST_USED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLastUsedAt(@jakarta.annotation.Nullable OffsetDateTime lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }


  public GrantRow useCount(@jakarta.annotation.Nonnull Integer useCount) {
    this.useCount = useCount;
    return this;
  }

  /**
   * Successful public-resolver uses (incremented best-effort).
   * @return useCount
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_USE_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getUseCount() {
    return useCount;
  }


  @JsonProperty(JSON_PROPERTY_USE_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUseCount(@jakarta.annotation.Nonnull Integer useCount) {
    this.useCount = useCount;
  }


  public GrantRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  public GrantRow revocable(@jakarta.annotation.Nonnull Boolean revocable) {
    this.revocable = revocable;
    return this;
  }

  /**
   * Computed: &#x60;false&#x60; for &#x60;AUTHORITY&#x60; grants (platform-managed), &#x60;true&#x60; otherwise.
   * @return revocable
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REVOCABLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getRevocable() {
    return revocable;
  }


  @JsonProperty(JSON_PROPERTY_REVOCABLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRevocable(@jakarta.annotation.Nonnull Boolean revocable) {
    this.revocable = revocable;
  }


  /**
   * Return true if this GrantRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GrantRow grantRow = (GrantRow) o;
    return Objects.equals(this.id, grantRow.id) &&
        Objects.equals(this.status, grantRow.status) &&
        Objects.equals(this.kind, grantRow.kind) &&
        Objects.equals(this.granteeName, grantRow.granteeName) &&
        Objects.equals(this.granteeEmail, grantRow.granteeEmail) &&
        Objects.equals(this.organization, grantRow.organization) &&
        Objects.equals(this.purpose, grantRow.purpose) &&
        Objects.equals(this.scopeType, grantRow.scopeType) &&
        Objects.equals(this.passportId, grantRow.passportId) &&
        Objects.equals(this.batteryUnitId, grantRow.batteryUnitId) &&
        Objects.equals(this.issuerType, grantRow.issuerType) &&
        Objects.equals(this.issuerEmail, grantRow.issuerEmail) &&
        Objects.equals(this.decidedAt, grantRow.decidedAt) &&
        Objects.equals(this.decidedBy, grantRow.decidedBy) &&
        Objects.equals(this.expiresAt, grantRow.expiresAt) &&
        Objects.equals(this.revokedAt, grantRow.revokedAt) &&
        Objects.equals(this.lastUsedAt, grantRow.lastUsedAt) &&
        Objects.equals(this.useCount, grantRow.useCount) &&
        Objects.equals(this.createdAt, grantRow.createdAt) &&
        Objects.equals(this.revocable, grantRow.revocable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, kind, granteeName, granteeEmail, organization, purpose, scopeType, passportId, batteryUnitId, issuerType, issuerEmail, decidedAt, decidedBy, expiresAt, revokedAt, lastUsedAt, useCount, createdAt, revocable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GrantRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    granteeName: ").append(toIndentedString(granteeName)).append("\n");
    sb.append("    granteeEmail: ").append(toIndentedString(granteeEmail)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
    sb.append("    purpose: ").append(toIndentedString(purpose)).append("\n");
    sb.append("    scopeType: ").append(toIndentedString(scopeType)).append("\n");
    sb.append("    passportId: ").append(toIndentedString(passportId)).append("\n");
    sb.append("    batteryUnitId: ").append(toIndentedString(batteryUnitId)).append("\n");
    sb.append("    issuerType: ").append(toIndentedString(issuerType)).append("\n");
    sb.append("    issuerEmail: ").append(toIndentedString(issuerEmail)).append("\n");
    sb.append("    decidedAt: ").append(toIndentedString(decidedAt)).append("\n");
    sb.append("    decidedBy: ").append(toIndentedString(decidedBy)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    revokedAt: ").append(toIndentedString(revokedAt)).append("\n");
    sb.append("    lastUsedAt: ").append(toIndentedString(lastUsedAt)).append("\n");
    sb.append("    useCount: ").append(toIndentedString(useCount)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    revocable: ").append(toIndentedString(revocable)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `kind` to the URL query string
    if (getKind() != null) {
      joiner.add(String.format("%skind%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getKind()))));
    }

    // add `granteeName` to the URL query string
    if (getGranteeName() != null) {
      joiner.add(String.format("%sgranteeName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGranteeName()))));
    }

    // add `granteeEmail` to the URL query string
    if (getGranteeEmail() != null) {
      joiner.add(String.format("%sgranteeEmail%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGranteeEmail()))));
    }

    // add `organization` to the URL query string
    if (getOrganization() != null) {
      joiner.add(String.format("%sorganization%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOrganization()))));
    }

    // add `purpose` to the URL query string
    if (getPurpose() != null) {
      joiner.add(String.format("%spurpose%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPurpose()))));
    }

    // add `scopeType` to the URL query string
    if (getScopeType() != null) {
      joiner.add(String.format("%sscopeType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getScopeType()))));
    }

    // add `passportId` to the URL query string
    if (getPassportId() != null) {
      joiner.add(String.format("%spassportId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPassportId()))));
    }

    // add `batteryUnitId` to the URL query string
    if (getBatteryUnitId() != null) {
      joiner.add(String.format("%sbatteryUnitId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBatteryUnitId()))));
    }

    // add `issuerType` to the URL query string
    if (getIssuerType() != null) {
      joiner.add(String.format("%sissuerType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuerType()))));
    }

    // add `issuerEmail` to the URL query string
    if (getIssuerEmail() != null) {
      joiner.add(String.format("%sissuerEmail%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuerEmail()))));
    }

    // add `decidedAt` to the URL query string
    if (getDecidedAt() != null) {
      joiner.add(String.format("%sdecidedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDecidedAt()))));
    }

    // add `decidedBy` to the URL query string
    if (getDecidedBy() != null) {
      joiner.add(String.format("%sdecidedBy%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDecidedBy()))));
    }

    // add `expiresAt` to the URL query string
    if (getExpiresAt() != null) {
      joiner.add(String.format("%sexpiresAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getExpiresAt()))));
    }

    // add `revokedAt` to the URL query string
    if (getRevokedAt() != null) {
      joiner.add(String.format("%srevokedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRevokedAt()))));
    }

    // add `lastUsedAt` to the URL query string
    if (getLastUsedAt() != null) {
      joiner.add(String.format("%slastUsedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLastUsedAt()))));
    }

    // add `useCount` to the URL query string
    if (getUseCount() != null) {
      joiner.add(String.format("%suseCount%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUseCount()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `revocable` to the URL query string
    if (getRevocable() != null) {
      joiner.add(String.format("%srevocable%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRevocable()))));
    }

    return joiner.toString();
  }
}

