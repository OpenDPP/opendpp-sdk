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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * One node of the recursive upstream lineage DAG. All keys are always present; &#x60;location&#x60;, &#x60;readPoint&#x60; and &#x60;issuerDid&#x60; are null when unset. &#x60;location&#x60; mirrors the stored &#x60;bizLocation&#x60;. A shared ancestor reached through multiple downstream paths appears once under each path (the DAG is expanded into a tree).
 */
@JsonPropertyOrder({
  TraceLineageNode.JSON_PROPERTY_EVENT_ID,
  TraceLineageNode.JSON_PROPERTY_EVENT_TYPE,
  TraceLineageNode.JSON_PROPERTY_BIZ_STEP,
  TraceLineageNode.JSON_PROPERTY_DISPOSITION,
  TraceLineageNode.JSON_PROPERTY_EVENT_TIME,
  TraceLineageNode.JSON_PROPERTY_EPCS,
  TraceLineageNode.JSON_PROPERTY_LOCATION,
  TraceLineageNode.JSON_PROPERTY_READ_POINT,
  TraceLineageNode.JSON_PROPERTY_IS_UNTP_COMPLIANT,
  TraceLineageNode.JSON_PROPERTY_ISSUER_DID,
  TraceLineageNode.JSON_PROPERTY_PARENTS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class TraceLineageNode {
  public static final String JSON_PROPERTY_EVENT_ID = "eventId";
  @jakarta.annotation.Nonnull
  private String eventId;

  /**
   * Gets or Sets eventType
   */
  public enum EventTypeEnum {
    OBJECT_EVENT(String.valueOf("ObjectEvent")),
    
    AGGREGATION_EVENT(String.valueOf("AggregationEvent")),
    
    TRANSFORMATION_EVENT(String.valueOf("TransformationEvent")),
    
    ASSOCIATION_EVENT(String.valueOf("AssociationEvent")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    EventTypeEnum(String value) {
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
    public static EventTypeEnum fromValue(String value) {
      for (EventTypeEnum b : EventTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
  @jakarta.annotation.Nonnull
  private EventTypeEnum eventType;

  public static final String JSON_PROPERTY_BIZ_STEP = "bizStep";
  @jakarta.annotation.Nonnull
  private String bizStep;

  public static final String JSON_PROPERTY_DISPOSITION = "disposition";
  @jakarta.annotation.Nonnull
  private String disposition;

  public static final String JSON_PROPERTY_EVENT_TIME = "eventTime";
  @jakarta.annotation.Nonnull
  private OffsetDateTime eventTime;

  public static final String JSON_PROPERTY_EPCS = "epcs";
  @jakarta.annotation.Nonnull
  private List<String> epcs = new ArrayList<>();

  public static final String JSON_PROPERTY_LOCATION = "location";
  @jakarta.annotation.Nullable
  private String location;

  public static final String JSON_PROPERTY_READ_POINT = "readPoint";
  @jakarta.annotation.Nullable
  private String readPoint;

  public static final String JSON_PROPERTY_IS_UNTP_COMPLIANT = "isUntpCompliant";
  @jakarta.annotation.Nonnull
  private Boolean isUntpCompliant;

  public static final String JSON_PROPERTY_ISSUER_DID = "issuerDid";
  @jakarta.annotation.Nullable
  private String issuerDid;

  public static final String JSON_PROPERTY_PARENTS = "parents";
  @jakarta.annotation.Nonnull
  private List<TraceLineageNode> parents = new ArrayList<>();

  public TraceLineageNode() { 
  }

  public TraceLineageNode eventId(@jakarta.annotation.Nonnull String eventId) {
    this.eventId = eventId;
    return this;
  }

  /**
   * Get eventId
   * @return eventId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEventId() {
    return eventId;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEventId(@jakarta.annotation.Nonnull String eventId) {
    this.eventId = eventId;
  }


  public TraceLineageNode eventType(@jakarta.annotation.Nonnull EventTypeEnum eventType) {
    this.eventType = eventType;
    return this;
  }

  /**
   * Get eventType
   * @return eventType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public EventTypeEnum getEventType() {
    return eventType;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEventType(@jakarta.annotation.Nonnull EventTypeEnum eventType) {
    this.eventType = eventType;
  }


  public TraceLineageNode bizStep(@jakarta.annotation.Nonnull String bizStep) {
    this.bizStep = bizStep;
    return this;
  }

  /**
   * Get bizStep
   * @return bizStep
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_BIZ_STEP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getBizStep() {
    return bizStep;
  }


  @JsonProperty(JSON_PROPERTY_BIZ_STEP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setBizStep(@jakarta.annotation.Nonnull String bizStep) {
    this.bizStep = bizStep;
  }


  public TraceLineageNode disposition(@jakarta.annotation.Nonnull String disposition) {
    this.disposition = disposition;
    return this;
  }

  /**
   * Get disposition
   * @return disposition
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DISPOSITION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDisposition() {
    return disposition;
  }


  @JsonProperty(JSON_PROPERTY_DISPOSITION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDisposition(@jakarta.annotation.Nonnull String disposition) {
    this.disposition = disposition;
  }


  public TraceLineageNode eventTime(@jakarta.annotation.Nonnull OffsetDateTime eventTime) {
    this.eventTime = eventTime;
    return this;
  }

  /**
   * Get eventTime
   * @return eventTime
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EVENT_TIME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getEventTime() {
    return eventTime;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_TIME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEventTime(@jakarta.annotation.Nonnull OffsetDateTime eventTime) {
    this.eventTime = eventTime;
  }


  public TraceLineageNode epcs(@jakarta.annotation.Nonnull List<String> epcs) {
    this.epcs = epcs;
    return this;
  }

  public TraceLineageNode addEpcsItem(String epcsItem) {
    if (this.epcs == null) {
      this.epcs = new ArrayList<>();
    }
    this.epcs.add(epcsItem);
    return this;
  }

  /**
   * EPC URIs parsed from the stored EPC list (degrades to &#x60;[]&#x60; when unparseable).
   * @return epcs
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EPCS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getEpcs() {
    return epcs;
  }


  @JsonProperty(JSON_PROPERTY_EPCS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEpcs(@jakarta.annotation.Nonnull List<String> epcs) {
    this.epcs = epcs;
  }


  public TraceLineageNode location(@jakarta.annotation.Nullable String location) {
    this.location = location;
    return this;
  }

  /**
   * The event&#39;s stored &#x60;bizLocation&#x60;.
   * @return location
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_LOCATION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLocation() {
    return location;
  }


  @JsonProperty(JSON_PROPERTY_LOCATION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLocation(@jakarta.annotation.Nullable String location) {
    this.location = location;
  }


  public TraceLineageNode readPoint(@jakarta.annotation.Nullable String readPoint) {
    this.readPoint = readPoint;
    return this;
  }

  /**
   * Get readPoint
   * @return readPoint
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_READ_POINT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getReadPoint() {
    return readPoint;
  }


  @JsonProperty(JSON_PROPERTY_READ_POINT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReadPoint(@jakarta.annotation.Nullable String readPoint) {
    this.readPoint = readPoint;
  }


  public TraceLineageNode isUntpCompliant(@jakarta.annotation.Nonnull Boolean isUntpCompliant) {
    this.isUntpCompliant = isUntpCompliant;
    return this;
  }

  /**
   * Get isUntpCompliant
   * @return isUntpCompliant
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IS_UNTP_COMPLIANT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsUntpCompliant() {
    return isUntpCompliant;
  }


  @JsonProperty(JSON_PROPERTY_IS_UNTP_COMPLIANT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsUntpCompliant(@jakarta.annotation.Nonnull Boolean isUntpCompliant) {
    this.isUntpCompliant = isUntpCompliant;
  }


  public TraceLineageNode issuerDid(@jakarta.annotation.Nullable String issuerDid) {
    this.issuerDid = issuerDid;
    return this;
  }

  /**
   * Get issuerDid
   * @return issuerDid
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ISSUER_DID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getIssuerDid() {
    return issuerDid;
  }


  @JsonProperty(JSON_PROPERTY_ISSUER_DID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIssuerDid(@jakarta.annotation.Nullable String issuerDid) {
    this.issuerDid = issuerDid;
  }


  public TraceLineageNode parents(@jakarta.annotation.Nonnull List<TraceLineageNode> parents) {
    this.parents = parents;
    return this;
  }

  public TraceLineageNode addParentsItem(TraceLineageNode parentsItem) {
    if (this.parents == null) {
      this.parents = new ArrayList<>();
    }
    this.parents.add(parentsItem);
    return this;
  }

  /**
   * Upstream parent events (recursive). Empty array at the origin of the chain.
   * @return parents
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PARENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<TraceLineageNode> getParents() {
    return parents;
  }


  @JsonProperty(JSON_PROPERTY_PARENTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setParents(@jakarta.annotation.Nonnull List<TraceLineageNode> parents) {
    this.parents = parents;
  }


  /**
   * Return true if this TraceLineageNode object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TraceLineageNode traceLineageNode = (TraceLineageNode) o;
    return Objects.equals(this.eventId, traceLineageNode.eventId) &&
        Objects.equals(this.eventType, traceLineageNode.eventType) &&
        Objects.equals(this.bizStep, traceLineageNode.bizStep) &&
        Objects.equals(this.disposition, traceLineageNode.disposition) &&
        Objects.equals(this.eventTime, traceLineageNode.eventTime) &&
        Objects.equals(this.epcs, traceLineageNode.epcs) &&
        Objects.equals(this.location, traceLineageNode.location) &&
        Objects.equals(this.readPoint, traceLineageNode.readPoint) &&
        Objects.equals(this.isUntpCompliant, traceLineageNode.isUntpCompliant) &&
        Objects.equals(this.issuerDid, traceLineageNode.issuerDid) &&
        Objects.equals(this.parents, traceLineageNode.parents);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, eventType, bizStep, disposition, eventTime, epcs, location, readPoint, isUntpCompliant, issuerDid, parents);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TraceLineageNode {\n");
    sb.append("    eventId: ").append(toIndentedString(eventId)).append("\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    bizStep: ").append(toIndentedString(bizStep)).append("\n");
    sb.append("    disposition: ").append(toIndentedString(disposition)).append("\n");
    sb.append("    eventTime: ").append(toIndentedString(eventTime)).append("\n");
    sb.append("    epcs: ").append(toIndentedString(epcs)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    readPoint: ").append(toIndentedString(readPoint)).append("\n");
    sb.append("    isUntpCompliant: ").append(toIndentedString(isUntpCompliant)).append("\n");
    sb.append("    issuerDid: ").append(toIndentedString(issuerDid)).append("\n");
    sb.append("    parents: ").append(toIndentedString(parents)).append("\n");
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

    // add `eventId` to the URL query string
    if (getEventId() != null) {
      joiner.add(String.format("%seventId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventId()))));
    }

    // add `eventType` to the URL query string
    if (getEventType() != null) {
      joiner.add(String.format("%seventType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventType()))));
    }

    // add `bizStep` to the URL query string
    if (getBizStep() != null) {
      joiner.add(String.format("%sbizStep%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBizStep()))));
    }

    // add `disposition` to the URL query string
    if (getDisposition() != null) {
      joiner.add(String.format("%sdisposition%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDisposition()))));
    }

    // add `eventTime` to the URL query string
    if (getEventTime() != null) {
      joiner.add(String.format("%seventTime%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventTime()))));
    }

    // add `epcs` to the URL query string
    if (getEpcs() != null) {
      for (int i = 0; i < getEpcs().size(); i++) {
        joiner.add(String.format("%sepcs%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getEpcs().get(i)))));
      }
    }

    // add `location` to the URL query string
    if (getLocation() != null) {
      joiner.add(String.format("%slocation%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getLocation()))));
    }

    // add `readPoint` to the URL query string
    if (getReadPoint() != null) {
      joiner.add(String.format("%sreadPoint%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getReadPoint()))));
    }

    // add `isUntpCompliant` to the URL query string
    if (getIsUntpCompliant() != null) {
      joiner.add(String.format("%sisUntpCompliant%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsUntpCompliant()))));
    }

    // add `issuerDid` to the URL query string
    if (getIssuerDid() != null) {
      joiner.add(String.format("%sissuerDid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuerDid()))));
    }

    // add `parents` to the URL query string
    if (getParents() != null) {
      for (int i = 0; i < getParents().size(); i++) {
        if (getParents().get(i) != null) {
          joiner.add(getParents().get(i).toUrlQueryString(String.format("%sparents%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }
}

