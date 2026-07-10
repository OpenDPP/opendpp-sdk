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
 * Health-check body of &#x60;GET /health&#x60;. Carries the running build identity (&#x60;apiVersion&#x60;/&#x60;commit&#x60;/&#x60;builtAt&#x60;) in addition to the liveness fields.
 */
@JsonPropertyOrder({
  HealthStatus.JSON_PROPERTY_STATUS,
  HealthStatus.JSON_PROPERTY_SERVICE,
  HealthStatus.JSON_PROPERTY_TIMESTAMP,
  HealthStatus.JSON_PROPERTY_API_VERSION,
  HealthStatus.JSON_PROPERTY_COMMIT,
  HealthStatus.JSON_PROPERTY_BUILT_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class HealthStatus {
  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    OK(String.valueOf("OK")),
    
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
   * Gets or Sets service
   */
  public enum ServiceEnum {
    OPEN_DPP_B2_B_ENTERPRISE_ENGINE(String.valueOf("OpenDPP B2B Enterprise Engine")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ServiceEnum(String value) {
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
    public static ServiceEnum fromValue(String value) {
      for (ServiceEnum b : ServiceEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_SERVICE = "service";
  @jakarta.annotation.Nonnull
  private ServiceEnum service;

  public static final String JSON_PROPERTY_TIMESTAMP = "timestamp";
  @jakarta.annotation.Nonnull
  private OffsetDateTime timestamp;

  public static final String JSON_PROPERTY_API_VERSION = "apiVersion";
  @jakarta.annotation.Nonnull
  private String apiVersion;

  public static final String JSON_PROPERTY_COMMIT = "commit";
  @jakarta.annotation.Nonnull
  private String commit;

  public static final String JSON_PROPERTY_BUILT_AT = "builtAt";
  @jakarta.annotation.Nonnull
  private String builtAt;

  public HealthStatus() { 
  }

  public HealthStatus status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
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


  public HealthStatus service(@jakarta.annotation.Nonnull ServiceEnum service) {
    this.service = service;
    return this;
  }

  /**
   * Get service
   * @return service
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SERVICE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ServiceEnum getService() {
    return service;
  }


  @JsonProperty(JSON_PROPERTY_SERVICE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setService(@jakarta.annotation.Nonnull ServiceEnum service) {
    this.service = service;
  }


  public HealthStatus timestamp(@jakarta.annotation.Nonnull OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Current server time, ISO 8601 UTC with milliseconds.
   * @return timestamp
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TIMESTAMP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }


  @JsonProperty(JSON_PROPERTY_TIMESTAMP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTimestamp(@jakarta.annotation.Nonnull OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }


  public HealthStatus apiVersion(@jakarta.annotation.Nonnull String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  /**
   * SemVer of the public API contract currently served (equals the OpenAPI document&#39;s &#x60;info.version&#x60;; its MAJOR equals the &#x60;/api/v1&#x60; URL major).
   * @return apiVersion
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_API_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getApiVersion() {
    return apiVersion;
  }


  @JsonProperty(JSON_PROPERTY_API_VERSION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApiVersion(@jakarta.annotation.Nonnull String apiVersion) {
    this.apiVersion = apiVersion;
  }


  public HealthStatus commit(@jakarta.annotation.Nonnull String commit) {
    this.commit = commit;
    return this;
  }

  /**
   * Short git commit SHA of the running build, or &#x60;\&quot;unknown\&quot;&#x60; when a build did not inject it.
   * @return commit
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_COMMIT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCommit() {
    return commit;
  }


  @JsonProperty(JSON_PROPERTY_COMMIT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCommit(@jakarta.annotation.Nonnull String commit) {
    this.commit = commit;
  }


  public HealthStatus builtAt(@jakarta.annotation.Nonnull String builtAt) {
    this.builtAt = builtAt;
    return this;
  }

  /**
   * Build/deploy timestamp (ISO 8601 UTC), or &#x60;\&quot;unknown\&quot;&#x60; when a build did not inject it.
   * @return builtAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_BUILT_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getBuiltAt() {
    return builtAt;
  }


  @JsonProperty(JSON_PROPERTY_BUILT_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setBuiltAt(@jakarta.annotation.Nonnull String builtAt) {
    this.builtAt = builtAt;
  }


  /**
   * Return true if this HealthStatus object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HealthStatus healthStatus = (HealthStatus) o;
    return Objects.equals(this.status, healthStatus.status) &&
        Objects.equals(this.service, healthStatus.service) &&
        Objects.equals(this.timestamp, healthStatus.timestamp) &&
        Objects.equals(this.apiVersion, healthStatus.apiVersion) &&
        Objects.equals(this.commit, healthStatus.commit) &&
        Objects.equals(this.builtAt, healthStatus.builtAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, service, timestamp, apiVersion, commit, builtAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HealthStatus {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    service: ").append(toIndentedString(service)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    commit: ").append(toIndentedString(commit)).append("\n");
    sb.append("    builtAt: ").append(toIndentedString(builtAt)).append("\n");
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

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `service` to the URL query string
    if (getService() != null) {
      joiner.add(String.format("%sservice%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getService()))));
    }

    // add `timestamp` to the URL query string
    if (getTimestamp() != null) {
      joiner.add(String.format("%stimestamp%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTimestamp()))));
    }

    // add `apiVersion` to the URL query string
    if (getApiVersion() != null) {
      joiner.add(String.format("%sapiVersion%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getApiVersion()))));
    }

    // add `commit` to the URL query string
    if (getCommit() != null) {
      joiner.add(String.format("%scommit%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCommit()))));
    }

    // add `builtAt` to the URL query string
    if (getBuiltAt() != null) {
      joiner.add(String.format("%sbuiltAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBuiltAt()))));
    }

    return joiner.toString();
  }
}

