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
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * PassportHistoryVersion
 */
@JsonPropertyOrder({
  PassportHistoryVersion.JSON_PROPERTY_SUCCESS,
  PassportHistoryVersion.JSON_PROPERTY_PASSPORT_ID,
  PassportHistoryVersion.JSON_PROPERTY_PRODUCT_ID,
  PassportHistoryVersion.JSON_PROPERTY_DATE,
  PassportHistoryVersion.JSON_PROPERTY_VERSION,
  PassportHistoryVersion.JSON_PROPERTY_CURRENT,
  PassportHistoryVersion.JSON_PROPERTY_VALID_FROM,
  PassportHistoryVersion.JSON_PROPERTY_VALID_UNTIL,
  PassportHistoryVersion.JSON_PROPERTY_RECORDED_AT,
  PassportHistoryVersion.JSON_PROPERTY_CHANGED_BY,
  PassportHistoryVersion.JSON_PROPERTY_CHANGE_REASON,
  PassportHistoryVersion.JSON_PROPERTY_DOCUMENT_AVAILABLE,
  PassportHistoryVersion.JSON_PROPERTY_CONTENT_HASH,
  PassportHistoryVersion.JSON_PROPERTY_PASSPORT,
  PassportHistoryVersion.JSON_PROPERTY_METADATA
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PassportHistoryVersion {
  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private Boolean success;

  public static final String JSON_PROPERTY_PASSPORT_ID = "passportId";
  @jakarta.annotation.Nonnull
  private String passportId;

  public static final String JSON_PROPERTY_PRODUCT_ID = "productId";
  @jakarta.annotation.Nonnull
  private String productId;

  public static final String JSON_PROPERTY_DATE = "date";
  @jakarta.annotation.Nullable
  private OffsetDateTime date;

  public static final String JSON_PROPERTY_VERSION = "version";
  @jakarta.annotation.Nonnull
  private Integer version;

  public static final String JSON_PROPERTY_CURRENT = "current";
  @jakarta.annotation.Nonnull
  private Boolean current;

  public static final String JSON_PROPERTY_VALID_FROM = "validFrom";
  @jakarta.annotation.Nullable
  private OffsetDateTime validFrom;

  public static final String JSON_PROPERTY_VALID_UNTIL = "validUntil";
  @jakarta.annotation.Nullable
  private OffsetDateTime validUntil;

  public static final String JSON_PROPERTY_RECORDED_AT = "recordedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime recordedAt;

  public static final String JSON_PROPERTY_CHANGED_BY = "changedBy";
  @jakarta.annotation.Nullable
  private String changedBy;

  public static final String JSON_PROPERTY_CHANGE_REASON = "changeReason";
  @jakarta.annotation.Nullable
  private String changeReason;

  public static final String JSON_PROPERTY_DOCUMENT_AVAILABLE = "documentAvailable";
  @jakarta.annotation.Nonnull
  private Boolean documentAvailable;

  public static final String JSON_PROPERTY_CONTENT_HASH = "contentHash";
  @jakarta.annotation.Nullable
  private String contentHash;

  public static final String JSON_PROPERTY_PASSPORT = "passport";
  private JsonNullable<Object> passport = JsonNullable.<Object>of(null);

  public static final String JSON_PROPERTY_METADATA = "metadata";
  private JsonNullable<Object> metadata = JsonNullable.<Object>of(null);

  public PassportHistoryVersion() { 
  }

  public PassportHistoryVersion success(@jakarta.annotation.Nonnull Boolean success) {
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


  public PassportHistoryVersion passportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
    return this;
  }

  /**
   * Get passportId
   * @return passportId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPassportId() {
    return passportId;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPassportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
  }


  public PassportHistoryVersion productId(@jakarta.annotation.Nonnull String productId) {
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


  public PassportHistoryVersion date(@jakarta.annotation.Nullable OffsetDateTime date) {
    this.date = date;
    return this;
  }

  /**
   * Only on the by-date read: the instant that was asked for, normalised.
   * @return date
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getDate() {
    return date;
  }


  @JsonProperty(JSON_PROPERTY_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDate(@jakarta.annotation.Nullable OffsetDateTime date) {
    this.date = date;
  }


  public PassportHistoryVersion version(@jakarta.annotation.Nonnull Integer version) {
    this.version = version;
    return this;
  }

  /**
   * The version number; on the by-date read this is the live version number when &#x60;current&#x60; is true.
   * minimum: 1
   * @return version
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getVersion() {
    return version;
  }


  @JsonProperty(JSON_PROPERTY_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVersion(@jakarta.annotation.Nonnull Integer version) {
    this.version = version;
  }


  public PassportHistoryVersion current(@jakarta.annotation.Nonnull Boolean current) {
    this.current = current;
    return this;
  }

  /**
   * &#x60;true&#x60; when the answer is the live passport (nothing changed since &#x60;date&#x60;), &#x60;false&#x60; for an archived version.
   * @return current
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CURRENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getCurrent() {
    return current;
  }


  @JsonProperty(JSON_PROPERTY_CURRENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCurrent(@jakarta.annotation.Nonnull Boolean current) {
    this.current = current;
  }


  public PassportHistoryVersion validFrom(@jakarta.annotation.Nullable OffsetDateTime validFrom) {
    this.validFrom = validFrom;
    return this;
  }

  /**
   * The instant this version became current — the previous version&#39;s &#x60;validUntil&#x60;, or the passport&#39;s creation.
   * @return validFrom
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VALID_FROM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getValidFrom() {
    return validFrom;
  }


  @JsonProperty(JSON_PROPERTY_VALID_FROM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setValidFrom(@jakarta.annotation.Nullable OffsetDateTime validFrom) {
    this.validFrom = validFrom;
  }


  public PassportHistoryVersion validUntil(@jakarta.annotation.Nullable OffsetDateTime validUntil) {
    this.validUntil = validUntil;
    return this;
  }

  /**
   * The instant it stopped being current; &#x60;null&#x60; for the live passport.
   * @return validUntil
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VALID_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getValidUntil() {
    return validUntil;
  }


  @JsonProperty(JSON_PROPERTY_VALID_UNTIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setValidUntil(@jakarta.annotation.Nullable OffsetDateTime validUntil) {
    this.validUntil = validUntil;
  }


  public PassportHistoryVersion recordedAt(@jakarta.annotation.Nullable OffsetDateTime recordedAt) {
    this.recordedAt = recordedAt;
    return this;
  }

  /**
   * When the snapshot was archived; &#x60;null&#x60; for the live passport.
   * @return recordedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getRecordedAt() {
    return recordedAt;
  }


  @JsonProperty(JSON_PROPERTY_RECORDED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRecordedAt(@jakarta.annotation.Nullable OffsetDateTime recordedAt) {
    this.recordedAt = recordedAt;
  }


  public PassportHistoryVersion changedBy(@jakarta.annotation.Nullable String changedBy) {
    this.changedBy = changedBy;
    return this;
  }

  /**
   * Who recorded the change that replaced this version; &#x60;null&#x60; for the live passport.
   * @return changedBy
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CHANGED_BY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getChangedBy() {
    return changedBy;
  }


  @JsonProperty(JSON_PROPERTY_CHANGED_BY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setChangedBy(@jakarta.annotation.Nullable String changedBy) {
    this.changedBy = changedBy;
  }


  public PassportHistoryVersion changeReason(@jakarta.annotation.Nullable String changeReason) {
    this.changeReason = changeReason;
    return this;
  }

  /**
   * Get changeReason
   * @return changeReason
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CHANGE_REASON)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getChangeReason() {
    return changeReason;
  }


  @JsonProperty(JSON_PROPERTY_CHANGE_REASON)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setChangeReason(@jakarta.annotation.Nullable String changeReason) {
    this.changeReason = changeReason;
  }


  public PassportHistoryVersion documentAvailable(@jakarta.annotation.Nonnull Boolean documentAvailable) {
    this.documentAvailable = documentAvailable;
    return this;
  }

  /**
   * &#x60;true&#x60; when this version is served as a document in &#x60;passport&#x60;. &#x60;false&#x60; only for a version archived before whole-row snapshots existed, where nothing can reconstruct the document — then &#x60;metadata&#x60; carries the regulated data instead and &#x60;passport&#x60; is &#x60;null&#x60;.
   * @return documentAvailable
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DOCUMENT_AVAILABLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getDocumentAvailable() {
    return documentAvailable;
  }


  @JsonProperty(JSON_PROPERTY_DOCUMENT_AVAILABLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDocumentAvailable(@jakarta.annotation.Nonnull Boolean documentAvailable) {
    this.documentAvailable = documentAvailable;
  }


  public PassportHistoryVersion contentHash(@jakarta.annotation.Nullable String contentHash) {
    this.contentHash = contentHash;
    return this;
  }

  /**
   * SHA-256 over the RFC 8785 canonical form of this version&#39;s record, chained on the previous version&#39;s hash — the integrity evidence EN 18221:2026 §4.2 asks for, so a retrieved version can be VERIFIED and not merely read. &#x60;null&#x60; on a version archived before the column existed, and on the live version (which is the passport itself).
   * @return contentHash
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CONTENT_HASH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getContentHash() {
    return contentHash;
  }


  @JsonProperty(JSON_PROPERTY_CONTENT_HASH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setContentHash(@jakarta.annotation.Nullable String contentHash) {
    this.contentHash = contentHash;
  }


  public PassportHistoryVersion passport(@jakarta.annotation.Nullable Object passport) {
    this.passport = JsonNullable.<Object>of(passport);
    return this;
  }

  /**
   * Get passport
   * @return passport
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public Object getPassport() {
        return passport.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_PASSPORT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getPassport_JsonNullable() {
    return passport;
  }
  
  @JsonProperty(JSON_PROPERTY_PASSPORT)
  public void setPassport_JsonNullable(JsonNullable<Object> passport) {
    this.passport = passport;
  }

  public void setPassport(@jakarta.annotation.Nullable Object passport) {
    this.passport = JsonNullable.<Object>of(passport);
  }


  public PassportHistoryVersion metadata(@jakarta.annotation.Nullable Object metadata) {
    this.metadata = JsonNullable.<Object>of(metadata);
    return this;
  }

  /**
   * Get metadata
   * @return metadata
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public Object getMetadata() {
        return metadata.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_METADATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getMetadata_JsonNullable() {
    return metadata;
  }
  
  @JsonProperty(JSON_PROPERTY_METADATA)
  public void setMetadata_JsonNullable(JsonNullable<Object> metadata) {
    this.metadata = metadata;
  }

  public void setMetadata(@jakarta.annotation.Nullable Object metadata) {
    this.metadata = JsonNullable.<Object>of(metadata);
  }


  /**
   * Return true if this PassportHistoryVersion object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportHistoryVersion passportHistoryVersion = (PassportHistoryVersion) o;
    return Objects.equals(this.success, passportHistoryVersion.success) &&
        Objects.equals(this.passportId, passportHistoryVersion.passportId) &&
        Objects.equals(this.productId, passportHistoryVersion.productId) &&
        Objects.equals(this.date, passportHistoryVersion.date) &&
        Objects.equals(this.version, passportHistoryVersion.version) &&
        Objects.equals(this.current, passportHistoryVersion.current) &&
        Objects.equals(this.validFrom, passportHistoryVersion.validFrom) &&
        Objects.equals(this.validUntil, passportHistoryVersion.validUntil) &&
        Objects.equals(this.recordedAt, passportHistoryVersion.recordedAt) &&
        Objects.equals(this.changedBy, passportHistoryVersion.changedBy) &&
        Objects.equals(this.changeReason, passportHistoryVersion.changeReason) &&
        Objects.equals(this.documentAvailable, passportHistoryVersion.documentAvailable) &&
        Objects.equals(this.contentHash, passportHistoryVersion.contentHash) &&
        equalsNullable(this.passport, passportHistoryVersion.passport) &&
        equalsNullable(this.metadata, passportHistoryVersion.metadata);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, passportId, productId, date, version, current, validFrom, validUntil, recordedAt, changedBy, changeReason, documentAvailable, contentHash, hashCodeNullable(passport), hashCodeNullable(metadata));
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
    sb.append("class PassportHistoryVersion {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    passportId: ").append(toIndentedString(passportId)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    current: ").append(toIndentedString(current)).append("\n");
    sb.append("    validFrom: ").append(toIndentedString(validFrom)).append("\n");
    sb.append("    validUntil: ").append(toIndentedString(validUntil)).append("\n");
    sb.append("    recordedAt: ").append(toIndentedString(recordedAt)).append("\n");
    sb.append("    changedBy: ").append(toIndentedString(changedBy)).append("\n");
    sb.append("    changeReason: ").append(toIndentedString(changeReason)).append("\n");
    sb.append("    documentAvailable: ").append(toIndentedString(documentAvailable)).append("\n");
    sb.append("    contentHash: ").append(toIndentedString(contentHash)).append("\n");
    sb.append("    passport: ").append(toIndentedString(passport)).append("\n");
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

    // add `success` to the URL query string
    if (getSuccess() != null) {
      joiner.add(String.format("%ssuccess%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSuccess()))));
    }

    // add `passportId` to the URL query string
    if (getPassportId() != null) {
      joiner.add(String.format("%spassportId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPassportId()))));
    }

    // add `productId` to the URL query string
    if (getProductId() != null) {
      joiner.add(String.format("%sproductId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProductId()))));
    }

    // add `date` to the URL query string
    if (getDate() != null) {
      joiner.add(String.format("%sdate%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDate()))));
    }

    // add `version` to the URL query string
    if (getVersion() != null) {
      joiner.add(String.format("%sversion%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
    }

    // add `current` to the URL query string
    if (getCurrent() != null) {
      joiner.add(String.format("%scurrent%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCurrent()))));
    }

    // add `validFrom` to the URL query string
    if (getValidFrom() != null) {
      joiner.add(String.format("%svalidFrom%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValidFrom()))));
    }

    // add `validUntil` to the URL query string
    if (getValidUntil() != null) {
      joiner.add(String.format("%svalidUntil%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValidUntil()))));
    }

    // add `recordedAt` to the URL query string
    if (getRecordedAt() != null) {
      joiner.add(String.format("%srecordedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRecordedAt()))));
    }

    // add `changedBy` to the URL query string
    if (getChangedBy() != null) {
      joiner.add(String.format("%schangedBy%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getChangedBy()))));
    }

    // add `changeReason` to the URL query string
    if (getChangeReason() != null) {
      joiner.add(String.format("%schangeReason%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getChangeReason()))));
    }

    // add `documentAvailable` to the URL query string
    if (getDocumentAvailable() != null) {
      joiner.add(String.format("%sdocumentAvailable%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDocumentAvailable()))));
    }

    // add `contentHash` to the URL query string
    if (getContentHash() != null) {
      joiner.add(String.format("%scontentHash%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getContentHash()))));
    }

    // add `passport` to the URL query string
    if (getPassport() != null) {
      joiner.add(String.format("%spassport%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPassport()))));
    }

    // add `metadata` to the URL query string
    if (getMetadata() != null) {
      joiner.add(String.format("%smetadata%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMetadata()))));
    }

    return joiner.toString();
  }
}

