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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * WhoamiResponseAuth
 */
@JsonPropertyOrder({
  WhoamiResponseAuth.JSON_PROPERTY_ROLE,
  WhoamiResponseAuth.JSON_PROPERTY_PERMISSIONS,
  WhoamiResponseAuth.JSON_PROPERTY_IS_API_KEY_SESSION,
  WhoamiResponseAuth.JSON_PROPERTY_OPERATOR_ID
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class WhoamiResponseAuth {
  public static final String JSON_PROPERTY_ROLE = "role";
  @jakarta.annotation.Nonnull
  private String role;

  public static final String JSON_PROPERTY_PERMISSIONS = "permissions";
  @jakarta.annotation.Nonnull
  private List<String> permissions = new ArrayList<>();

  public static final String JSON_PROPERTY_IS_API_KEY_SESSION = "isApiKeySession";
  @jakarta.annotation.Nonnull
  private Boolean isApiKeySession;

  public static final String JSON_PROPERTY_OPERATOR_ID = "operatorId";
  @jakarta.annotation.Nullable
  private String operatorId;

  public WhoamiResponseAuth() { 
  }

  public WhoamiResponseAuth role(@jakarta.annotation.Nonnull String role) {
    this.role = role;
    return this;
  }

  /**
   * The principal&#39;s role.
   * @return role
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRole() {
    return role;
  }


  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRole(@jakarta.annotation.Nonnull String role) {
    this.role = role;
  }


  public WhoamiResponseAuth permissions(@jakarta.annotation.Nonnull List<String> permissions) {
    this.permissions = permissions;
    return this;
  }

  public WhoamiResponseAuth addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }
    this.permissions.add(permissionsItem);
    return this;
  }

  /**
   * Effective permission strings, re-derived server-side from the role (never trusted from the token). May include wildcards like &#x60;operator:*&#x60;.
   * @return permissions
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PERMISSIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getPermissions() {
    return permissions;
  }


  @JsonProperty(JSON_PROPERTY_PERMISSIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPermissions(@jakarta.annotation.Nonnull List<String> permissions) {
    this.permissions = permissions;
  }


  public WhoamiResponseAuth isApiKeySession(@jakarta.annotation.Nonnull Boolean isApiKeySession) {
    this.isApiKeySession = isApiKeySession;
    return this;
  }

  /**
   * &#x60;true&#x60; when authenticated with an API key, &#x60;false&#x60; for a session JWT.
   * @return isApiKeySession
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IS_API_KEY_SESSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsApiKeySession() {
    return isApiKeySession;
  }


  @JsonProperty(JSON_PROPERTY_IS_API_KEY_SESSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsApiKeySession(@jakarta.annotation.Nonnull Boolean isApiKeySession) {
    this.isApiKeySession = isApiKeySession;
  }


  public WhoamiResponseAuth operatorId(@jakarta.annotation.Nullable String operatorId) {
    this.operatorId = operatorId;
    return this;
  }

  /**
   * The economic operator this credential is scoped to, or &#x60;null&#x60; for a workspace-wide key. A scoped key&#39;s writes and reads are restricted to this operator.
   * @return operatorId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOperatorId() {
    return operatorId;
  }


  @JsonProperty(JSON_PROPERTY_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOperatorId(@jakarta.annotation.Nullable String operatorId) {
    this.operatorId = operatorId;
  }


  /**
   * Return true if this WhoamiResponse_auth object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WhoamiResponseAuth whoamiResponseAuth = (WhoamiResponseAuth) o;
    return Objects.equals(this.role, whoamiResponseAuth.role) &&
        Objects.equals(this.permissions, whoamiResponseAuth.permissions) &&
        Objects.equals(this.isApiKeySession, whoamiResponseAuth.isApiKeySession) &&
        Objects.equals(this.operatorId, whoamiResponseAuth.operatorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, permissions, isApiKeySession, operatorId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WhoamiResponseAuth {\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("    isApiKeySession: ").append(toIndentedString(isApiKeySession)).append("\n");
    sb.append("    operatorId: ").append(toIndentedString(operatorId)).append("\n");
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

    // add `role` to the URL query string
    if (getRole() != null) {
      joiner.add(String.format("%srole%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRole()))));
    }

    // add `permissions` to the URL query string
    if (getPermissions() != null) {
      for (int i = 0; i < getPermissions().size(); i++) {
        joiner.add(String.format("%spermissions%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getPermissions().get(i)))));
      }
    }

    // add `isApiKeySession` to the URL query string
    if (getIsApiKeySession() != null) {
      joiner.add(String.format("%sisApiKeySession%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsApiKeySession()))));
    }

    // add `operatorId` to the URL query string
    if (getOperatorId() != null) {
      joiner.add(String.format("%soperatorId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOperatorId()))));
    }

    return joiner.toString();
  }
}

