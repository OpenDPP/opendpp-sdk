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
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * DecodeGs1BatchRequestItemsInner
 */
@JsonPropertyOrder({
  DecodeGs1BatchRequestItemsInner.JSON_PROPERTY_SCAN_DATA,
  DecodeGs1BatchRequestItemsInner.JSON_PROPERTY_ELEMENT_STRING,
  DecodeGs1BatchRequestItemsInner.JSON_PROPERTY_DIGITAL_LINK
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class DecodeGs1BatchRequestItemsInner {
  public static final String JSON_PROPERTY_SCAN_DATA = "scanData";
  @jakarta.annotation.Nullable
  private String scanData;

  public static final String JSON_PROPERTY_ELEMENT_STRING = "elementString";
  @jakarta.annotation.Nullable
  private String elementString;

  public static final String JSON_PROPERTY_DIGITAL_LINK = "digitalLink";
  @jakarta.annotation.Nullable
  private String digitalLink;

  public DecodeGs1BatchRequestItemsInner() { 
  }

  public DecodeGs1BatchRequestItemsInner scanData(@jakarta.annotation.Nullable String scanData) {
    this.scanData = scanData;
    return this;
  }

  /**
   * Get scanData
   * @return scanData
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SCAN_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getScanData() {
    return scanData;
  }


  @JsonProperty(JSON_PROPERTY_SCAN_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScanData(@jakarta.annotation.Nullable String scanData) {
    this.scanData = scanData;
  }


  public DecodeGs1BatchRequestItemsInner elementString(@jakarta.annotation.Nullable String elementString) {
    this.elementString = elementString;
    return this;
  }

  /**
   * Get elementString
   * @return elementString
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ELEMENT_STRING)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getElementString() {
    return elementString;
  }


  @JsonProperty(JSON_PROPERTY_ELEMENT_STRING)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setElementString(@jakarta.annotation.Nullable String elementString) {
    this.elementString = elementString;
  }


  public DecodeGs1BatchRequestItemsInner digitalLink(@jakarta.annotation.Nullable String digitalLink) {
    this.digitalLink = digitalLink;
    return this;
  }

  /**
   * Get digitalLink
   * @return digitalLink
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getDigitalLink() {
    return digitalLink;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDigitalLink(@jakarta.annotation.Nullable String digitalLink) {
    this.digitalLink = digitalLink;
  }


  /**
   * Return true if this decodeGs1Batch_request_items_inner object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DecodeGs1BatchRequestItemsInner decodeGs1BatchRequestItemsInner = (DecodeGs1BatchRequestItemsInner) o;
    return Objects.equals(this.scanData, decodeGs1BatchRequestItemsInner.scanData) &&
        Objects.equals(this.elementString, decodeGs1BatchRequestItemsInner.elementString) &&
        Objects.equals(this.digitalLink, decodeGs1BatchRequestItemsInner.digitalLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scanData, elementString, digitalLink);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DecodeGs1BatchRequestItemsInner {\n");
    sb.append("    scanData: ").append(toIndentedString(scanData)).append("\n");
    sb.append("    elementString: ").append(toIndentedString(elementString)).append("\n");
    sb.append("    digitalLink: ").append(toIndentedString(digitalLink)).append("\n");
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

    // add `scanData` to the URL query string
    if (getScanData() != null) {
      joiner.add(String.format("%sscanData%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getScanData()))));
    }

    // add `elementString` to the URL query string
    if (getElementString() != null) {
      joiner.add(String.format("%selementString%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getElementString()))));
    }

    // add `digitalLink` to the URL query string
    if (getDigitalLink() != null) {
      joiner.add(String.format("%sdigitalLink%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLink()))));
    }

    return joiner.toString();
  }
}

