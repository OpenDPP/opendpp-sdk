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
 * Present only when &#x60;payload.proof.rfc3161.token&#x60; was supplied AND verification proceeds past the key-registration and operator-binding gates (the two policy &#x60;verified: false&#x60; responses omit it). Reports presence, the TSA-asserted genTime from the token&#39;s TSTInfo, and — when the node has a TSA CA configured (&#x60;TSA_CA_PEM&#x60;) — &#x60;timeAuthenticated&#x60;: the result of verifying the token&#39;s CMS SignedData signature over its TSTInfo and chaining the signer certificate to that anchor.
 */
@JsonPropertyOrder({
  SealTimestampReport.JSON_PROPERTY_PRESENT,
  SealTimestampReport.JSON_PROPERTY_GEN_TIME,
  SealTimestampReport.JSON_PROPERTY_TIME_AUTHENTICATED,
  SealTimestampReport.JSON_PROPERTY_NOTE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SealTimestampReport {
  /**
   * Gets or Sets present
   */
  public enum PresentEnum {
    TRUE(Boolean.valueOf("true"));

    private Boolean value;

    PresentEnum(Boolean value) {
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
    public static PresentEnum fromValue(Boolean value) {
      for (PresentEnum b : PresentEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PRESENT = "present";
  @jakarta.annotation.Nonnull
  private PresentEnum present;

  public static final String JSON_PROPERTY_GEN_TIME = "genTime";
  @jakarta.annotation.Nullable
  private OffsetDateTime genTime;

  public static final String JSON_PROPERTY_TIME_AUTHENTICATED = "timeAuthenticated";
  @jakarta.annotation.Nullable
  private Boolean timeAuthenticated;

  /**
   * Present only when &#x60;genTime&#x60; is null.
   */
  public enum NoteEnum {
    TOKEN_PRESENT_BUT_TST_INFO_COULD_NOT_BE_PARSED(String.valueOf("token present but TSTInfo could not be parsed"));

    private String value;

    NoteEnum(String value) {
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
    public static NoteEnum fromValue(String value) {
      for (NoteEnum b : NoteEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_NOTE = "note";
  @jakarta.annotation.Nullable
  private NoteEnum note;

  public SealTimestampReport() { 
  }

  public SealTimestampReport present(@jakarta.annotation.Nonnull PresentEnum present) {
    this.present = present;
    return this;
  }

  /**
   * Get present
   * @return present
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PRESENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PresentEnum getPresent() {
    return present;
  }


  @JsonProperty(JSON_PROPERTY_PRESENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPresent(@jakarta.annotation.Nonnull PresentEnum present) {
    this.present = present;
  }


  public SealTimestampReport genTime(@jakarta.annotation.Nullable OffsetDateTime genTime) {
    this.genTime = genTime;
    return this;
  }

  /**
   * TSA-asserted generation time (ISO 8601), or null when the token&#39;s TSTInfo could not be parsed.
   * @return genTime
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_GEN_TIME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getGenTime() {
    return genTime;
  }


  @JsonProperty(JSON_PROPERTY_GEN_TIME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGenTime(@jakarta.annotation.Nullable OffsetDateTime genTime) {
    this.genTime = genTime;
  }


  public SealTimestampReport timeAuthenticated(@jakarta.annotation.Nullable Boolean timeAuthenticated) {
    this.timeAuthenticated = timeAuthenticated;
    return this;
  }

  /**
   * True only when the token&#39;s RFC 3161 CMS SignedData signature verifies over its TSTInfo AND the signer passes full trust-path validation to the node&#39;s configured TSA CA anchor (&#x60;TSA_CA_PEM&#x60;): the signer is an end-entity timestamping certificate (a CRITICAL &#x60;id-kp-timeStamping&#x60; EKU, not a CA) that is VALID at the asserted &#x60;genTime&#x60; and chains through CA-constrained, genTime-valid intermediates to the anchor (itself a CA valid at &#x60;genTime&#x60;). False when the signature fails, when the path is not policy-valid, and when no TSA CA is configured (the asserted &#x60;genTime&#x60; is then unauthenticated). This is the node&#39;s own cryptographic check and does not replace a verifier&#39;s independent &#x60;openssl ts -verify&#x60;.
   * @return timeAuthenticated
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TIME_AUTHENTICATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getTimeAuthenticated() {
    return timeAuthenticated;
  }


  @JsonProperty(JSON_PROPERTY_TIME_AUTHENTICATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTimeAuthenticated(@jakarta.annotation.Nullable Boolean timeAuthenticated) {
    this.timeAuthenticated = timeAuthenticated;
  }


  public SealTimestampReport note(@jakarta.annotation.Nullable NoteEnum note) {
    this.note = note;
    return this;
  }

  /**
   * Present only when &#x60;genTime&#x60; is null.
   * @return note
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_NOTE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public NoteEnum getNote() {
    return note;
  }


  @JsonProperty(JSON_PROPERTY_NOTE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNote(@jakarta.annotation.Nullable NoteEnum note) {
    this.note = note;
  }


  /**
   * Return true if this SealTimestampReport object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SealTimestampReport sealTimestampReport = (SealTimestampReport) o;
    return Objects.equals(this.present, sealTimestampReport.present) &&
        Objects.equals(this.genTime, sealTimestampReport.genTime) &&
        Objects.equals(this.timeAuthenticated, sealTimestampReport.timeAuthenticated) &&
        Objects.equals(this.note, sealTimestampReport.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(present, genTime, timeAuthenticated, note);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SealTimestampReport {\n");
    sb.append("    present: ").append(toIndentedString(present)).append("\n");
    sb.append("    genTime: ").append(toIndentedString(genTime)).append("\n");
    sb.append("    timeAuthenticated: ").append(toIndentedString(timeAuthenticated)).append("\n");
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
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

    // add `present` to the URL query string
    if (getPresent() != null) {
      joiner.add(String.format("%spresent%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPresent()))));
    }

    // add `genTime` to the URL query string
    if (getGenTime() != null) {
      joiner.add(String.format("%sgenTime%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGenTime()))));
    }

    // add `timeAuthenticated` to the URL query string
    if (getTimeAuthenticated() != null) {
      joiner.add(String.format("%stimeAuthenticated%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTimeAuthenticated()))));
    }

    // add `note` to the URL query string
    if (getNote() != null) {
      joiner.add(String.format("%snote%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNote()))));
    }

    return joiner.toString();
  }
}

