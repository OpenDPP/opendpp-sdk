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
import eu.opendppnode.sdk.model.WhoamiResponseAuth;
import eu.opendppnode.sdk.model.WhoamiResponseTenant;
import eu.opendppnode.sdk.model.WhoamiResponseUsage;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * WhoamiResponse
 */
@JsonPropertyOrder({
  WhoamiResponse.JSON_PROPERTY_SUCCESS,
  WhoamiResponse.JSON_PROPERTY_TENANT,
  WhoamiResponse.JSON_PROPERTY_AUTH,
  WhoamiResponse.JSON_PROPERTY_USAGE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class WhoamiResponse {
  /**
   * Gets or Sets success
   */
  public enum SuccessEnum {
    TRUE(Boolean.valueOf("true"));

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
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private SuccessEnum success;

  public static final String JSON_PROPERTY_TENANT = "tenant";
  @jakarta.annotation.Nonnull
  private WhoamiResponseTenant tenant;

  public static final String JSON_PROPERTY_AUTH = "auth";
  @jakarta.annotation.Nonnull
  private WhoamiResponseAuth auth;

  public static final String JSON_PROPERTY_USAGE = "usage";
  @jakarta.annotation.Nonnull
  private WhoamiResponseUsage usage;

  public WhoamiResponse() { 
  }

  public WhoamiResponse success(@jakarta.annotation.Nonnull SuccessEnum success) {
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


  public WhoamiResponse tenant(@jakarta.annotation.Nonnull WhoamiResponseTenant tenant) {
    this.tenant = tenant;
    return this;
  }

  /**
   * Get tenant
   * @return tenant
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TENANT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public WhoamiResponseTenant getTenant() {
    return tenant;
  }


  @JsonProperty(JSON_PROPERTY_TENANT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTenant(@jakarta.annotation.Nonnull WhoamiResponseTenant tenant) {
    this.tenant = tenant;
  }


  public WhoamiResponse auth(@jakarta.annotation.Nonnull WhoamiResponseAuth auth) {
    this.auth = auth;
    return this;
  }

  /**
   * Get auth
   * @return auth
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AUTH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public WhoamiResponseAuth getAuth() {
    return auth;
  }


  @JsonProperty(JSON_PROPERTY_AUTH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAuth(@jakarta.annotation.Nonnull WhoamiResponseAuth auth) {
    this.auth = auth;
  }


  public WhoamiResponse usage(@jakarta.annotation.Nonnull WhoamiResponseUsage usage) {
    this.usage = usage;
    return this;
  }

  /**
   * Get usage
   * @return usage
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_USAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public WhoamiResponseUsage getUsage() {
    return usage;
  }


  @JsonProperty(JSON_PROPERTY_USAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUsage(@jakarta.annotation.Nonnull WhoamiResponseUsage usage) {
    this.usage = usage;
  }


  /**
   * Return true if this WhoamiResponse object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WhoamiResponse whoamiResponse = (WhoamiResponse) o;
    return Objects.equals(this.success, whoamiResponse.success) &&
        Objects.equals(this.tenant, whoamiResponse.tenant) &&
        Objects.equals(this.auth, whoamiResponse.auth) &&
        Objects.equals(this.usage, whoamiResponse.usage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, tenant, auth, usage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WhoamiResponse {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    tenant: ").append(toIndentedString(tenant)).append("\n");
    sb.append("    auth: ").append(toIndentedString(auth)).append("\n");
    sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
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

    // add `tenant` to the URL query string
    if (getTenant() != null) {
      joiner.add(getTenant().toUrlQueryString(prefix + "tenant" + suffix));
    }

    // add `auth` to the URL query string
    if (getAuth() != null) {
      joiner.add(getAuth().toUrlQueryString(prefix + "auth" + suffix));
    }

    // add `usage` to the URL query string
    if (getUsage() != null) {
      joiner.add(getUsage().toUrlQueryString(prefix + "usage" + suffix));
    }

    return joiner.toString();
  }
}

