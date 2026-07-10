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
import eu.opendppnode.sdk.model.WebhookEventFilter;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * The full subscription row as returned ONLY by the 201 create response — includes the HMAC-SHA256 signing secret.
 */
@JsonPropertyOrder({
  WebhookSubscriptionWithSecret.JSON_PROPERTY_ID,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_TENANT_ID,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_URL,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_EVENTS,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_IS_ACTIVE,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_CREATED_AT,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_UPDATED_AT,
  WebhookSubscriptionWithSecret.JSON_PROPERTY_SECRET
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class WebhookSubscriptionWithSecret {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
  @jakarta.annotation.Nonnull
  private String tenantId;

  public static final String JSON_PROPERTY_URL = "url";
  @jakarta.annotation.Nonnull
  private URI url;

  public static final String JSON_PROPERTY_EVENTS = "events";
  @jakarta.annotation.Nonnull
  private List<WebhookEventFilter> events = new ArrayList<>();

  public static final String JSON_PROPERTY_IS_ACTIVE = "isActive";
  @jakarta.annotation.Nonnull
  private Boolean isActive;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public static final String JSON_PROPERTY_SECRET = "secret";
  @jakarta.annotation.Nonnull
  private String secret;

  public WebhookSubscriptionWithSecret() { 
  }

  public WebhookSubscriptionWithSecret id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Subscription id.
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


  public WebhookSubscriptionWithSecret tenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Owning workspace (tenant) id.
   * @return tenantId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTenantId() {
    return tenantId;
  }


  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
  }


  public WebhookSubscriptionWithSecret url(@jakarta.annotation.Nonnull URI url) {
    this.url = url;
    return this;
  }

  /**
   * Receiver endpoint URL.
   * @return url
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getUrl() {
    return url;
  }


  @JsonProperty(JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUrl(@jakarta.annotation.Nonnull URI url) {
    this.url = url;
  }


  public WebhookSubscriptionWithSecret events(@jakarta.annotation.Nonnull List<WebhookEventFilter> events) {
    this.events = events;
    return this;
  }

  public WebhookSubscriptionWithSecret addEventsItem(WebhookEventFilter eventsItem) {
    if (this.events == null) {
      this.events = new ArrayList<>();
    }
    this.events.add(eventsItem);
    return this;
  }

  /**
   * Event filters this subscription matches (validated at creation).
   * @return events
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<WebhookEventFilter> getEvents() {
    return events;
  }


  @JsonProperty(JSON_PROPERTY_EVENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEvents(@jakarta.annotation.Nonnull List<WebhookEventFilter> events) {
    this.events = events;
  }


  public WebhookSubscriptionWithSecret isActive(@jakarta.annotation.Nonnull Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * &#x60;true&#x60; while the subscription receives deliveries; only active subscriptions are delivered to. Toggle with &#x60;PATCH /api/v1/webhooks/subscriptions/{id}&#x60; (&#x60;isActive&#x60;) to pause/resume without deleting.
   * @return isActive
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IS_ACTIVE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsActive() {
    return isActive;
  }


  @JsonProperty(JSON_PROPERTY_IS_ACTIVE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsActive(@jakarta.annotation.Nonnull Boolean isActive) {
    this.isActive = isActive;
  }


  public WebhookSubscriptionWithSecret createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  public WebhookSubscriptionWithSecret updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }


  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUpdatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  public WebhookSubscriptionWithSecret secret(@jakarta.annotation.Nonnull String secret) {
    this.secret = secret;
    return this;
  }

  /**
   * Server-generated HMAC-SHA256 signing key (&#x60;whsec_&#x60; + 32 lowercase hex chars). Shown ONCE — here and in the &#x60;rotate-secret&#x60; response only; the list endpoint strips it. The FULL string, including the &#x60;whsec_&#x60; prefix, is the HMAC key for delivery signatures. Rotate it with &#x60;POST /api/v1/webhooks/subscriptions/{id}/rotate-secret&#x60;.
   * @return secret
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SECRET)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSecret() {
    return secret;
  }


  @JsonProperty(JSON_PROPERTY_SECRET)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSecret(@jakarta.annotation.Nonnull String secret) {
    this.secret = secret;
  }


  /**
   * Return true if this WebhookSubscriptionWithSecret object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebhookSubscriptionWithSecret webhookSubscriptionWithSecret = (WebhookSubscriptionWithSecret) o;
    return Objects.equals(this.id, webhookSubscriptionWithSecret.id) &&
        Objects.equals(this.tenantId, webhookSubscriptionWithSecret.tenantId) &&
        Objects.equals(this.url, webhookSubscriptionWithSecret.url) &&
        Objects.equals(this.events, webhookSubscriptionWithSecret.events) &&
        Objects.equals(this.isActive, webhookSubscriptionWithSecret.isActive) &&
        Objects.equals(this.createdAt, webhookSubscriptionWithSecret.createdAt) &&
        Objects.equals(this.updatedAt, webhookSubscriptionWithSecret.updatedAt) &&
        Objects.equals(this.secret, webhookSubscriptionWithSecret.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, url, events, isActive, createdAt, updatedAt, secret);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebhookSubscriptionWithSecret {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    events: ").append(toIndentedString(events)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
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

    // add `tenantId` to the URL query string
    if (getTenantId() != null) {
      joiner.add(String.format("%stenantId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTenantId()))));
    }

    // add `url` to the URL query string
    if (getUrl() != null) {
      joiner.add(String.format("%surl%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
    }

    // add `events` to the URL query string
    if (getEvents() != null) {
      for (int i = 0; i < getEvents().size(); i++) {
        if (getEvents().get(i) != null) {
          joiner.add(String.format("%sevents%s%s=%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
              ApiClient.urlEncode(ApiClient.valueToString(getEvents().get(i)))));
        }
      }
    }

    // add `isActive` to the URL query string
    if (getIsActive() != null) {
      joiner.add(String.format("%sisActive%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsActive()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `updatedAt` to the URL query string
    if (getUpdatedAt() != null) {
      joiner.add(String.format("%supdatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUpdatedAt()))));
    }

    // add `secret` to the URL query string
    if (getSecret() != null) {
      joiner.add(String.format("%ssecret%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSecret()))));
    }

    return joiner.toString();
  }
}

