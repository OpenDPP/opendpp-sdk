/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.13.0
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
import eu.opendppnode.sdk.model.AdvisoryItem;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;
import eu.opendppnode.sdk.model.ValidationErrorItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * 201 envelope of &#x60;POST /api/v1/passports&#x60;. &#x60;passport&#x60; is the public redacted JSON-LD; &#x60;warnings&#x60;/&#x60;notices&#x60; are always present (possibly empty); &#x60;vcReady&#x60;/&#x60;vcReadyReason&#x60; report UNTP Verifiable-Credential readiness.
 */
@JsonPropertyOrder({
  PassportIngestCreated.JSON_PROPERTY_SUCCESS,
  PassportIngestCreated.JSON_PROPERTY_MESSAGE,
  PassportIngestCreated.JSON_PROPERTY_PASSPORT,
  PassportIngestCreated.JSON_PROPERTY_WARNINGS,
  PassportIngestCreated.JSON_PROPERTY_NOTICES,
  PassportIngestCreated.JSON_PROPERTY_VC_READY,
  PassportIngestCreated.JSON_PROPERTY_VC_READY_REASON
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PassportIngestCreated {
  /**
   * Gets or Sets success
   */
  public enum SuccessEnum {
    TRUE(Boolean.valueOf("true")),
    
    UNKNOWN_DEFAULT_OPEN_API(Boolean.valueOf("11184809"));

    private Boolean value;

    SuccessEnum(Boolean value) {
      this.value = value;
    }

    @JsonValue
    public Boolean getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SuccessEnum fromValue(Boolean value) {
      for (SuccessEnum b : SuccessEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private SuccessEnum success;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @jakarta.annotation.Nonnull
  private String message;

  public static final String JSON_PROPERTY_PASSPORT = "passport";
  @jakarta.annotation.Nonnull
  private PublicPassportJsonLd passport;

  public static final String JSON_PROPERTY_WARNINGS = "warnings";
  @jakarta.annotation.Nonnull
  private List<ValidationErrorItem> warnings = new ArrayList<>();

  public static final String JSON_PROPERTY_NOTICES = "notices";
  @jakarta.annotation.Nonnull
  private List<AdvisoryItem> notices = new ArrayList<>();

  public static final String JSON_PROPERTY_VC_READY = "vcReady";
  @jakarta.annotation.Nullable
  private Boolean vcReady;

  public static final String JSON_PROPERTY_VC_READY_REASON = "vcReadyReason";
  private JsonNullable<String> vcReadyReason = JsonNullable.<String>undefined();

  public PassportIngestCreated() { 
  }

  public PassportIngestCreated success(@jakarta.annotation.Nonnull SuccessEnum success) {
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
  public SuccessEnum getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccess(@jakarta.annotation.Nonnull SuccessEnum success) {
    this.success = success;
  }


  public PassportIngestCreated message(@jakarta.annotation.Nonnull String message) {
    this.message = message;
    return this;
  }

  /**
   * \&quot;Digital Product Passport successfully validated and ingested\&quot;, or \&quot;Draft passport saved\&quot; when &#x60;draft: true&#x60;.
   * @return message
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMessage() {
    return message;
  }


  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMessage(@jakarta.annotation.Nonnull String message) {
    this.message = message;
  }


  public PassportIngestCreated passport(@jakarta.annotation.Nonnull PublicPassportJsonLd passport) {
    this.passport = passport;
    return this;
  }

  /**
   * The PUBLIC redacted JSON-LD passport document (unsealed at creation: &#x60;digitalSeal&#x60;/&#x60;proof&#x60; are null). The owner-only metadata key &#x60;facilityDetails&#x60; is always replaced with the literal string \&quot;[REDACTED - Privileged Access Required]\&quot; — even in this creator-facing echo, and even when the submitted metadata did not contain it. For &#x60;category: \&quot;batteries\&quot;&#x60;, the restricted legitimate-interest keys &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60;, and &#x60;circularityAndDisassembly&#x60; are masked the same way when present.
   * @return passport
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PASSPORT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PublicPassportJsonLd getPassport() {
    return passport;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPassport(@jakarta.annotation.Nonnull PublicPassportJsonLd passport) {
    this.passport = passport;
  }


  public PassportIngestCreated warnings(@jakarta.annotation.Nonnull List<ValidationErrorItem> warnings) {
    this.warnings = warnings;
    return this;
  }

  public PassportIngestCreated addWarningsItem(ValidationErrorItem warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * Non-blocking findings — a MIX of ESPR validation warnings (no &#x60;code&#x60;) and machine-coded advisories (a stable &#x60;code&#x60;, e.g. &#x60;NON_GS1_PRODUCT_ID&#x60;, &#x60;PII_SHAPE_DETECTED&#x60;). Always present; empty for drafts. See &#x60;AdvisoryItem&#x60; for the coded shape.
   * @return warnings
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<ValidationErrorItem> getWarnings() {
    return warnings;
  }


  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWarnings(@jakarta.annotation.Nonnull List<ValidationErrorItem> warnings) {
    this.warnings = warnings;
  }


  public PassportIngestCreated notices(@jakarta.annotation.Nonnull List<AdvisoryItem> notices) {
    this.notices = notices;
    return this;
  }

  public PassportIngestCreated addNoticesItem(AdvisoryItem noticesItem) {
    if (this.notices == null) {
      this.notices = new ArrayList<>();
    }
    this.notices.add(noticesItem);
    return this;
  }

  /**
   * Informational advisories about helpful things the API did (always coded): &#x60;OPERATOR_AUTO_ATTRIBUTED&#x60; (operatorId omitted → the workspace&#39;s sole bound operator used), &#x60;GTIN_AUTO_COPIED&#x60; (a valid GTIN/GRAI copied into metadata.gtin/grai). Always present; empty when nothing to note.
   * @return notices
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_NOTICES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<AdvisoryItem> getNotices() {
    return notices;
  }


  @JsonProperty(JSON_PROPERTY_NOTICES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNotices(@jakarta.annotation.Nonnull List<AdvisoryItem> notices) {
    this.notices = notices;
  }


  public PassportIngestCreated vcReady(@jakarta.annotation.Nullable Boolean vcReady) {
    this.vcReady = vcReady;
    return this;
  }

  /**
   * Whether this passport can emit a UNTP Verifiable Credential — true only when a manufacturing facility with a country of production is linked (&#x60;producedAtFacility&#x60; + &#x60;countryOfProduction&#x60; are required by the UNTP DPP schema; a GLN is optional). The passport still publishes and resolves as AAS / JSON-LD / HTML regardless.
   * @return vcReady
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getVcReady() {
    return vcReady;
  }


  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVcReady(@jakarta.annotation.Nullable Boolean vcReady) {
    this.vcReady = vcReady;
  }


  public PassportIngestCreated vcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
    return this;
  }

  /**
   * Null when &#x60;vcReady&#x60; is true; otherwise a short, actionable reason (link a facility with a country of production).
   * @return vcReadyReason
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getVcReadyReason() {
        return vcReadyReason.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getVcReadyReason_JsonNullable() {
    return vcReadyReason;
  }
  
  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  public void setVcReadyReason_JsonNullable(JsonNullable<String> vcReadyReason) {
    this.vcReadyReason = vcReadyReason;
  }

  public void setVcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
  }


  /**
   * Return true if this PassportIngestCreated object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportIngestCreated passportIngestCreated = (PassportIngestCreated) o;
    return Objects.equals(this.success, passportIngestCreated.success) &&
        Objects.equals(this.message, passportIngestCreated.message) &&
        Objects.equals(this.passport, passportIngestCreated.passport) &&
        Objects.equals(this.warnings, passportIngestCreated.warnings) &&
        Objects.equals(this.notices, passportIngestCreated.notices) &&
        Objects.equals(this.vcReady, passportIngestCreated.vcReady) &&
        equalsNullable(this.vcReadyReason, passportIngestCreated.vcReadyReason);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message, passport, warnings, notices, vcReady, hashCodeNullable(vcReadyReason));
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
    sb.append("class PassportIngestCreated {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    passport: ").append(toIndentedString(passport)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
    sb.append("    notices: ").append(toIndentedString(notices)).append("\n");
    sb.append("    vcReady: ").append(toIndentedString(vcReady)).append("\n");
    sb.append("    vcReadyReason: ").append(toIndentedString(vcReadyReason)).append("\n");
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

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format("%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    // add `passport` to the URL query string
    if (getPassport() != null) {
      joiner.add(getPassport().toUrlQueryString(prefix + "passport" + suffix));
    }

    // add `warnings` to the URL query string
    if (getWarnings() != null) {
      for (int i = 0; i < getWarnings().size(); i++) {
        if (getWarnings().get(i) != null) {
          joiner.add(getWarnings().get(i).toUrlQueryString(String.format("%swarnings%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `notices` to the URL query string
    if (getNotices() != null) {
      for (int i = 0; i < getNotices().size(); i++) {
        if (getNotices().get(i) != null) {
          joiner.add(getNotices().get(i).toUrlQueryString(String.format("%snotices%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `vcReady` to the URL query string
    if (getVcReady() != null) {
      joiner.add(String.format("%svcReady%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReady()))));
    }

    // add `vcReadyReason` to the URL query string
    if (getVcReadyReason() != null) {
      joiner.add(String.format("%svcReadyReason%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReadyReason()))));
    }

    return joiner.toString();
  }
}

