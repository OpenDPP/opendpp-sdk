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
 * One outbox delivery record (event-level). The payload is not included.
 */
@JsonPropertyOrder({
  WebhookDeliveryRow.JSON_PROPERTY_ID,
  WebhookDeliveryRow.JSON_PROPERTY_EVENT,
  WebhookDeliveryRow.JSON_PROPERTY_STATUS,
  WebhookDeliveryRow.JSON_PROPERTY_RETRY_COUNT,
  WebhookDeliveryRow.JSON_PROPERTY_LAST_ATTEMPT,
  WebhookDeliveryRow.JSON_PROPERTY_NEXT_RETRY_AT,
  WebhookDeliveryRow.JSON_PROPERTY_ERROR_MESSAGE,
  WebhookDeliveryRow.JSON_PROPERTY_CREATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class WebhookDeliveryRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_EVENT = "event";
  @jakarta.annotation.Nonnull
  private String event;

  /**
   * Overall delivery state. FAILED after 5 exhausted attempts (dead-lettered).
   */
  public enum StatusEnum {
    PENDING(String.valueOf("PENDING")),
    
    DELIVERED(String.valueOf("DELIVERED")),
    
    FAILED(String.valueOf("FAILED")),
    
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

  public static final String JSON_PROPERTY_RETRY_COUNT = "retryCount";
  @jakarta.annotation.Nonnull
  private Integer retryCount;

  public static final String JSON_PROPERTY_LAST_ATTEMPT = "lastAttempt";
  @jakarta.annotation.Nullable
  private OffsetDateTime lastAttempt;

  public static final String JSON_PROPERTY_NEXT_RETRY_AT = "nextRetryAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime nextRetryAt;

  public static final String JSON_PROPERTY_ERROR_MESSAGE = "errorMessage";
  @jakarta.annotation.Nullable
  private String errorMessage;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public WebhookDeliveryRow() { 
  }

  public WebhookDeliveryRow id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Outbox record id.
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


  public WebhookDeliveryRow event(@jakarta.annotation.Nonnull String event) {
    this.event = event;
    return this;
  }

  /**
   * Event type, e.g. &#x60;passport.sealed&#x60;.
   * @return event
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEvent() {
    return event;
  }


  @JsonProperty(JSON_PROPERTY_EVENT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEvent(@jakarta.annotation.Nonnull String event) {
    this.event = event;
  }


  public WebhookDeliveryRow status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Overall delivery state. FAILED after 5 exhausted attempts (dead-lettered).
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


  public WebhookDeliveryRow retryCount(@jakarta.annotation.Nonnull Integer retryCount) {
    this.retryCount = retryCount;
    return this;
  }

  /**
   * Failed attempts so far (0–5).
   * @return retryCount
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_RETRY_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getRetryCount() {
    return retryCount;
  }


  @JsonProperty(JSON_PROPERTY_RETRY_COUNT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRetryCount(@jakarta.annotation.Nonnull Integer retryCount) {
    this.retryCount = retryCount;
  }


  public WebhookDeliveryRow lastAttempt(@jakarta.annotation.Nullable OffsetDateTime lastAttempt) {
    this.lastAttempt = lastAttempt;
    return this;
  }

  /**
   * Timestamp of the most recent attempt, or null if never attempted.
   * @return lastAttempt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_LAST_ATTEMPT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getLastAttempt() {
    return lastAttempt;
  }


  @JsonProperty(JSON_PROPERTY_LAST_ATTEMPT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLastAttempt(@jakarta.annotation.Nullable OffsetDateTime lastAttempt) {
    this.lastAttempt = lastAttempt;
  }


  public WebhookDeliveryRow nextRetryAt(@jakarta.annotation.Nullable OffsetDateTime nextRetryAt) {
    this.nextRetryAt = nextRetryAt;
    return this;
  }

  /**
   * When the next retry is eligible (null if delivered or dead-lettered).
   * @return nextRetryAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_NEXT_RETRY_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getNextRetryAt() {
    return nextRetryAt;
  }


  @JsonProperty(JSON_PROPERTY_NEXT_RETRY_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNextRetryAt(@jakarta.annotation.Nullable OffsetDateTime nextRetryAt) {
    this.nextRetryAt = nextRetryAt;
  }


  public WebhookDeliveryRow errorMessage(@jakarta.annotation.Nullable String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Joined per-endpoint error text from the last failed attempt, or null.
   * @return errorMessage
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ERROR_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getErrorMessage() {
    return errorMessage;
  }


  @JsonProperty(JSON_PROPERTY_ERROR_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setErrorMessage(@jakarta.annotation.Nullable String errorMessage) {
    this.errorMessage = errorMessage;
  }


  public WebhookDeliveryRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  /**
   * Return true if this WebhookDeliveryRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhookDeliveryRow webhookDeliveryRow = (WebhookDeliveryRow) o;
    return Objects.equals(this.id, webhookDeliveryRow.id) &&
        Objects.equals(this.event, webhookDeliveryRow.event) &&
        Objects.equals(this.status, webhookDeliveryRow.status) &&
        Objects.equals(this.retryCount, webhookDeliveryRow.retryCount) &&
        Objects.equals(this.lastAttempt, webhookDeliveryRow.lastAttempt) &&
        Objects.equals(this.nextRetryAt, webhookDeliveryRow.nextRetryAt) &&
        Objects.equals(this.errorMessage, webhookDeliveryRow.errorMessage) &&
        Objects.equals(this.createdAt, webhookDeliveryRow.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, event, status, retryCount, lastAttempt, nextRetryAt, errorMessage, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhookDeliveryRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    retryCount: ").append(toIndentedString(retryCount)).append("\n");
    sb.append("    lastAttempt: ").append(toIndentedString(lastAttempt)).append("\n");
    sb.append("    nextRetryAt: ").append(toIndentedString(nextRetryAt)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

    // add `event` to the URL query string
    if (getEvent() != null) {
      joiner.add(String.format("%sevent%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEvent()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `retryCount` to the URL query string
    if (getRetryCount() != null) {
      joiner.add(String.format("%sretryCount%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRetryCount()))));
    }

    // add `lastAttempt` to the URL query string
    if (getLastAttempt() != null) {
      joiner.add(String.format("%slastAttempt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLastAttempt()))));
    }

    // add `nextRetryAt` to the URL query string
    if (getNextRetryAt() != null) {
      joiner.add(String.format("%snextRetryAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getNextRetryAt()))));
    }

    // add `errorMessage` to the URL query string
    if (getErrorMessage() != null) {
      joiner.add(String.format("%serrorMessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getErrorMessage()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    return joiner.toString();
  }
}

