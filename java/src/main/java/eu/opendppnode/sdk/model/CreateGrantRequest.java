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
 * Direct-issuance body. Over-length strings are silently truncated to the documented maximum, not rejected; unknown fields are ignored. The grant kind is always &#x60;LEGITIMATE_INTEREST&#x60; — there is no &#x60;kind&#x60;/&#x60;type&#x60; field.
 */
@JsonPropertyOrder({
  CreateGrantRequest.JSON_PROPERTY_GRANTEE_NAME,
  CreateGrantRequest.JSON_PROPERTY_GRANTEE_EMAIL,
  CreateGrantRequest.JSON_PROPERTY_ORGANIZATION,
  CreateGrantRequest.JSON_PROPERTY_PURPOSE,
  CreateGrantRequest.JSON_PROPERTY_SCOPE_TYPE,
  CreateGrantRequest.JSON_PROPERTY_PASSPORT_ID,
  CreateGrantRequest.JSON_PROPERTY_BATTERY_UNIT_ID,
  CreateGrantRequest.JSON_PROPERTY_EXPIRES_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class CreateGrantRequest {
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
   * Required. &#x60;UNIT&#x60; needs &#x60;batteryUnitId&#x60;; &#x60;PASSPORT&#x60; needs &#x60;passportId&#x60;; &#x60;TENANT&#x60; is workspace-wide. Any other value ⇒ 400.
   */
  public enum ScopeTypeEnum {
    UNIT(String.valueOf("UNIT")),
    
    PASSPORT(String.valueOf("PASSPORT")),
    
    TENANT(String.valueOf("TENANT"));

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
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
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

  public static final String JSON_PROPERTY_EXPIRES_AT = "expiresAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime expiresAt;

  public CreateGrantRequest() { 
  }

  public CreateGrantRequest granteeName(@jakarta.annotation.Nonnull String granteeName) {
    this.granteeName = granteeName;
    return this;
  }

  /**
   * Required (whitespace-only is rejected as missing). Truncated to 160 characters.
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


  public CreateGrantRequest granteeEmail(@jakarta.annotation.Nullable String granteeEmail) {
    this.granteeEmail = granteeEmail;
    return this;
  }

  /**
   * Optional. Truncated to 254 characters, then must match a basic e-mail pattern (&#x60;x@y.z&#x60;) or the request fails with 400 &#x60;granteeEmail is invalid&#x60;. Stored as given (not lowercased).
   * @return granteeEmail
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_GRANTEE_EMAIL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getGranteeEmail() {
    return granteeEmail;
  }


  @JsonProperty(JSON_PROPERTY_GRANTEE_EMAIL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setGranteeEmail(@jakarta.annotation.Nullable String granteeEmail) {
    this.granteeEmail = granteeEmail;
  }


  public CreateGrantRequest organization(@jakarta.annotation.Nullable String organization) {
    this.organization = organization;
    return this;
  }

  /**
   * Optional. Truncated to 200 characters.
   * @return organization
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ORGANIZATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getOrganization() {
    return organization;
  }


  @JsonProperty(JSON_PROPERTY_ORGANIZATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOrganization(@jakarta.annotation.Nullable String organization) {
    this.organization = organization;
  }


  public CreateGrantRequest purpose(@jakarta.annotation.Nullable String purpose) {
    this.purpose = purpose;
    return this;
  }

  /**
   * Optional stated legitimate interest. Truncated to 2000 characters.
   * @return purpose
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPurpose() {
    return purpose;
  }


  @JsonProperty(JSON_PROPERTY_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPurpose(@jakarta.annotation.Nullable String purpose) {
    this.purpose = purpose;
  }


  public CreateGrantRequest scopeType(@jakarta.annotation.Nonnull ScopeTypeEnum scopeType) {
    this.scopeType = scopeType;
    return this;
  }

  /**
   * Required. &#x60;UNIT&#x60; needs &#x60;batteryUnitId&#x60;; &#x60;PASSPORT&#x60; needs &#x60;passportId&#x60;; &#x60;TENANT&#x60; is workspace-wide. Any other value ⇒ 400.
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


  public CreateGrantRequest passportId(@jakarta.annotation.Nullable String passportId) {
    this.passportId = passportId;
    return this;
  }

  /**
   * Required when &#x60;scopeType&#x60; is &#x60;PASSPORT&#x60;; ignored otherwise. Must be a non-DRAFT passport in this workspace — a missing, cross-tenant, or &#x60;DRAFT&#x60; id (or omitting the field entirely) returns 404.
   * @return passportId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPassportId() {
    return passportId;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPassportId(@jakarta.annotation.Nullable String passportId) {
    this.passportId = passportId;
  }


  public CreateGrantRequest batteryUnitId(@jakarta.annotation.Nullable String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
    return this;
  }

  /**
   * Required when &#x60;scopeType&#x60; is &#x60;UNIT&#x60;; ignored otherwise. Must be a battery unit in this workspace — a missing or cross-tenant id (or omitting the field entirely) returns 404.
   * @return batteryUnitId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getBatteryUnitId() {
    return batteryUnitId;
  }


  @JsonProperty(JSON_PROPERTY_BATTERY_UNIT_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setBatteryUnitId(@jakarta.annotation.Nullable String batteryUnitId) {
    this.batteryUnitId = batteryUnitId;
  }


  public CreateGrantRequest expiresAt(@jakarta.annotation.Nonnull OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  /**
   * Required. Date parsing is lenient (some non-ISO values may be accepted) — ISO 8601 is strongly recommended. Must be in the future and at most 366 days from now.
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


  /**
   * Return true if this CreateGrantRequest object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateGrantRequest createGrantRequest = (CreateGrantRequest) o;
    return Objects.equals(this.granteeName, createGrantRequest.granteeName) &&
        Objects.equals(this.granteeEmail, createGrantRequest.granteeEmail) &&
        Objects.equals(this.organization, createGrantRequest.organization) &&
        Objects.equals(this.purpose, createGrantRequest.purpose) &&
        Objects.equals(this.scopeType, createGrantRequest.scopeType) &&
        Objects.equals(this.passportId, createGrantRequest.passportId) &&
        Objects.equals(this.batteryUnitId, createGrantRequest.batteryUnitId) &&
        Objects.equals(this.expiresAt, createGrantRequest.expiresAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(granteeName, granteeEmail, organization, purpose, scopeType, passportId, batteryUnitId, expiresAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateGrantRequest {\n");
    sb.append("    granteeName: ").append(toIndentedString(granteeName)).append("\n");
    sb.append("    granteeEmail: ").append(toIndentedString(granteeEmail)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
    sb.append("    purpose: ").append(toIndentedString(purpose)).append("\n");
    sb.append("    scopeType: ").append(toIndentedString(scopeType)).append("\n");
    sb.append("    passportId: ").append(toIndentedString(passportId)).append("\n");
    sb.append("    batteryUnitId: ").append(toIndentedString(batteryUnitId)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
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

    // add `expiresAt` to the URL query string
    if (getExpiresAt() != null) {
      joiner.add(String.format("%sexpiresAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getExpiresAt()))));
    }

    return joiner.toString();
  }
}

