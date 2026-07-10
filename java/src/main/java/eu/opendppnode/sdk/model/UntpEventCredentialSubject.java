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
import eu.opendppnode.sdk.model.UntpEventCredentialSubjectOriginLocation;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * The EPCIS event payload. &#x60;eventType&#x60; is effectively required: it is persisted into a server-side enum, and a missing or unknown value is rejected at the persistence layer (surfacing as the 500 &#x60;Database Persistence Failed&#x60; body, not a 400).
 */
@JsonPropertyOrder({
  UntpEventCredentialSubject.JSON_PROPERTY_ID,
  UntpEventCredentialSubject.JSON_PROPERTY_EVENT_TYPE,
  UntpEventCredentialSubject.JSON_PROPERTY_ACTION,
  UntpEventCredentialSubject.JSON_PROPERTY_BIZ_STEP,
  UntpEventCredentialSubject.JSON_PROPERTY_DISPOSITION,
  UntpEventCredentialSubject.JSON_PROPERTY_READ_POINT,
  UntpEventCredentialSubject.JSON_PROPERTY_BIZ_LOCATION,
  UntpEventCredentialSubject.JSON_PROPERTY_EVENT_TIME,
  UntpEventCredentialSubject.JSON_PROPERTY_EPC_LIST,
  UntpEventCredentialSubject.JSON_PROPERTY_PARENT_EPC,
  UntpEventCredentialSubject.JSON_PROPERTY_CHILD_EPCS,
  UntpEventCredentialSubject.JSON_PROPERTY_INPUT_EPC_LIST,
  UntpEventCredentialSubject.JSON_PROPERTY_OUTPUT_EPC_LIST,
  UntpEventCredentialSubject.JSON_PROPERTY_ORIGIN_LOCATION,
  UntpEventCredentialSubject.JSON_PROPERTY_RESPONSIBLE_OPERATOR_DID
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class UntpEventCredentialSubject {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nullable
  private String id;

  /**
   * EPCIS 2.0 event type (server-side enum).
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

  /**
   * EPCIS action (server-side enum). Optional — but MUST be absent (or null) on &#x60;TransformationEvent&#x60;, otherwise 400 &#x60;Schema Validation Error&#x60;. An unknown value is rejected at the persistence layer (500).
   */
  public enum ActionEnum {
    ADD(String.valueOf("ADD")),
    
    OBSERVE(String.valueOf("OBSERVE")),
    
    DELETE(String.valueOf("DELETE")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ActionEnum(String value) {
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
    public static ActionEnum fromValue(String value) {
      for (ActionEnum b : ActionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_ACTION = "action";
  @jakarta.annotation.Nullable
  private ActionEnum action;

  public static final String JSON_PROPERTY_BIZ_STEP = "bizStep";
  @jakarta.annotation.Nullable
  private String bizStep = "urn:epcglobal:cbv:bizstep:receiving";

  public static final String JSON_PROPERTY_DISPOSITION = "disposition";
  @jakarta.annotation.Nullable
  private String disposition = "urn:epcglobal:cbv:disp:in_progress";

  public static final String JSON_PROPERTY_READ_POINT = "readPoint";
  @jakarta.annotation.Nullable
  private String readPoint;

  public static final String JSON_PROPERTY_BIZ_LOCATION = "bizLocation";
  @jakarta.annotation.Nullable
  private String bizLocation;

  public static final String JSON_PROPERTY_EVENT_TIME = "eventTime";
  @jakarta.annotation.Nullable
  private OffsetDateTime eventTime;

  public static final String JSON_PROPERTY_EPC_LIST = "epcList";
  @jakarta.annotation.Nullable
  private List<String> epcList = new ArrayList<>();

  public static final String JSON_PROPERTY_PARENT_EPC = "parentEpc";
  @jakarta.annotation.Nullable
  private String parentEpc;

  public static final String JSON_PROPERTY_CHILD_EPCS = "childEpcs";
  @jakarta.annotation.Nullable
  private List<String> childEpcs = new ArrayList<>();

  public static final String JSON_PROPERTY_INPUT_EPC_LIST = "inputEpcList";
  @jakarta.annotation.Nullable
  private List<String> inputEpcList = new ArrayList<>();

  public static final String JSON_PROPERTY_OUTPUT_EPC_LIST = "outputEpcList";
  @jakarta.annotation.Nullable
  private List<String> outputEpcList = new ArrayList<>();

  public static final String JSON_PROPERTY_ORIGIN_LOCATION = "originLocation";
  @jakarta.annotation.Nullable
  private UntpEventCredentialSubjectOriginLocation originLocation;

  public static final String JSON_PROPERTY_RESPONSIBLE_OPERATOR_DID = "responsibleOperatorDid";
  @jakarta.annotation.Nullable
  private String responsibleOperatorDid;

  public UntpEventCredentialSubject() { 
  }

  public UntpEventCredentialSubject id(@jakarta.annotation.Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * EPC identifier of the subject (e.g. &#x60;urn:epc:id:sgtin:0950110153.0003.SN-2026-000123&#x60;). When &#x60;epcList&#x60; is not supplied as an array, the stored EPC list defaults to &#x60;[id]&#x60; (or &#x60;[]&#x60; if absent).
   * @return id
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setId(@jakarta.annotation.Nullable String id) {
    this.id = id;
  }


  public UntpEventCredentialSubject eventType(@jakarta.annotation.Nonnull EventTypeEnum eventType) {
    this.eventType = eventType;
    return this;
  }

  /**
   * EPCIS 2.0 event type (server-side enum).
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


  public UntpEventCredentialSubject action(@jakarta.annotation.Nullable ActionEnum action) {
    this.action = action;
    return this;
  }

  /**
   * EPCIS action (server-side enum). Optional — but MUST be absent (or null) on &#x60;TransformationEvent&#x60;, otherwise 400 &#x60;Schema Validation Error&#x60;. An unknown value is rejected at the persistence layer (500).
   * @return action
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ACTION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public ActionEnum getAction() {
    return action;
  }


  @JsonProperty(JSON_PROPERTY_ACTION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAction(@jakarta.annotation.Nullable ActionEnum action) {
    this.action = action;
  }


  public UntpEventCredentialSubject bizStep(@jakarta.annotation.Nullable String bizStep) {
    this.bizStep = bizStep;
    return this;
  }

  /**
   * CBV business step URI. Defaults to &#x60;urn:epcglobal:cbv:bizstep:receiving&#x60;.
   * @return bizStep
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_BIZ_STEP)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getBizStep() {
    return bizStep;
  }


  @JsonProperty(JSON_PROPERTY_BIZ_STEP)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setBizStep(@jakarta.annotation.Nullable String bizStep) {
    this.bizStep = bizStep;
  }


  public UntpEventCredentialSubject disposition(@jakarta.annotation.Nullable String disposition) {
    this.disposition = disposition;
    return this;
  }

  /**
   * CBV disposition URI. Defaults to &#x60;urn:epcglobal:cbv:disp:in_progress&#x60;.
   * @return disposition
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DISPOSITION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getDisposition() {
    return disposition;
  }


  @JsonProperty(JSON_PROPERTY_DISPOSITION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDisposition(@jakarta.annotation.Nullable String disposition) {
    this.disposition = disposition;
  }


  public UntpEventCredentialSubject readPoint(@jakarta.annotation.Nullable String readPoint) {
    this.readPoint = readPoint;
    return this;
  }

  /**
   * Where the event was observed (e.g. &#x60;geo:41.1496,-8.6109&#x60;). When absent and &#x60;originLocation&#x60; is present, defaults to &#x60;geo:&lt;latitude&gt;,&lt;longitude&gt;&#x60;.
   * @return readPoint
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_READ_POINT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getReadPoint() {
    return readPoint;
  }


  @JsonProperty(JSON_PROPERTY_READ_POINT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setReadPoint(@jakarta.annotation.Nullable String readPoint) {
    this.readPoint = readPoint;
  }


  public UntpEventCredentialSubject bizLocation(@jakarta.annotation.Nullable String bizLocation) {
    this.bizLocation = bizLocation;
    return this;
  }

  /**
   * Business location (SGLN URI, DID, or free identifier). When absent, defaults to &#x60;responsibleOperatorDid&#x60;.
   * @return bizLocation
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_BIZ_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getBizLocation() {
    return bizLocation;
  }


  @JsonProperty(JSON_PROPERTY_BIZ_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setBizLocation(@jakarta.annotation.Nullable String bizLocation) {
    this.bizLocation = bizLocation;
  }


  public UntpEventCredentialSubject eventTime(@jakarta.annotation.Nullable OffsetDateTime eventTime) {
    this.eventTime = eventTime;
    return this;
  }

  /**
   * When the event occurred (anything &#x60;new Date()&#x60; parses). Defaults to the credential &#x60;issuanceDate&#x60;, else the server clock.
   * @return eventTime
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_EVENT_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getEventTime() {
    return eventTime;
  }


  @JsonProperty(JSON_PROPERTY_EVENT_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEventTime(@jakarta.annotation.Nullable OffsetDateTime eventTime) {
    this.eventTime = eventTime;
  }


  public UntpEventCredentialSubject epcList(@jakarta.annotation.Nullable List<String> epcList) {
    this.epcList = epcList;
    return this;
  }

  public UntpEventCredentialSubject addEpcListItem(String epcListItem) {
    if (this.epcList == null) {
      this.epcList = new ArrayList<>();
    }
    this.epcList.add(epcListItem);
    return this;
  }

  /**
   * EPC URIs observed by the event. A non-array value is replaced with &#x60;[credentialSubject.id]&#x60; (or &#x60;[]&#x60;).
   * @return epcList
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getEpcList() {
    return epcList;
  }


  @JsonProperty(JSON_PROPERTY_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEpcList(@jakarta.annotation.Nullable List<String> epcList) {
    this.epcList = epcList;
  }


  public UntpEventCredentialSubject parentEpc(@jakarta.annotation.Nullable String parentEpc) {
    this.parentEpc = parentEpc;
    return this;
  }

  /**
   * Parent EPC for AggregationEvent.
   * @return parentEpc
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PARENT_EPC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getParentEpc() {
    return parentEpc;
  }


  @JsonProperty(JSON_PROPERTY_PARENT_EPC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setParentEpc(@jakarta.annotation.Nullable String parentEpc) {
    this.parentEpc = parentEpc;
  }


  public UntpEventCredentialSubject childEpcs(@jakarta.annotation.Nullable List<String> childEpcs) {
    this.childEpcs = childEpcs;
    return this;
  }

  public UntpEventCredentialSubject addChildEpcsItem(String childEpcsItem) {
    if (this.childEpcs == null) {
      this.childEpcs = new ArrayList<>();
    }
    this.childEpcs.add(childEpcsItem);
    return this;
  }

  /**
   * Child EPCs for AggregationEvent (stored as JSON verbatim).
   * @return childEpcs
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CHILD_EPCS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getChildEpcs() {
    return childEpcs;
  }


  @JsonProperty(JSON_PROPERTY_CHILD_EPCS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setChildEpcs(@jakarta.annotation.Nullable List<String> childEpcs) {
    this.childEpcs = childEpcs;
  }


  public UntpEventCredentialSubject inputEpcList(@jakarta.annotation.Nullable List<String> inputEpcList) {
    this.inputEpcList = inputEpcList;
    return this;
  }

  public UntpEventCredentialSubject addInputEpcListItem(String inputEpcListItem) {
    if (this.inputEpcList == null) {
      this.inputEpcList = new ArrayList<>();
    }
    this.inputEpcList.add(inputEpcListItem);
    return this;
  }

  /**
   * Input EPCs for TransformationEvent (stored as JSON verbatim).
   * @return inputEpcList
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_INPUT_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getInputEpcList() {
    return inputEpcList;
  }


  @JsonProperty(JSON_PROPERTY_INPUT_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInputEpcList(@jakarta.annotation.Nullable List<String> inputEpcList) {
    this.inputEpcList = inputEpcList;
  }


  public UntpEventCredentialSubject outputEpcList(@jakarta.annotation.Nullable List<String> outputEpcList) {
    this.outputEpcList = outputEpcList;
    return this;
  }

  public UntpEventCredentialSubject addOutputEpcListItem(String outputEpcListItem) {
    if (this.outputEpcList == null) {
      this.outputEpcList = new ArrayList<>();
    }
    this.outputEpcList.add(outputEpcListItem);
    return this;
  }

  /**
   * Output EPCs for TransformationEvent (stored as JSON verbatim).
   * @return outputEpcList
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_OUTPUT_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getOutputEpcList() {
    return outputEpcList;
  }


  @JsonProperty(JSON_PROPERTY_OUTPUT_EPC_LIST)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOutputEpcList(@jakarta.annotation.Nullable List<String> outputEpcList) {
    this.outputEpcList = outputEpcList;
  }


  public UntpEventCredentialSubject originLocation(@jakarta.annotation.Nullable UntpEventCredentialSubjectOriginLocation originLocation) {
    this.originLocation = originLocation;
    return this;
  }

  /**
   * Get originLocation
   * @return originLocation
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ORIGIN_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UntpEventCredentialSubjectOriginLocation getOriginLocation() {
    return originLocation;
  }


  @JsonProperty(JSON_PROPERTY_ORIGIN_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOriginLocation(@jakarta.annotation.Nullable UntpEventCredentialSubjectOriginLocation originLocation) {
    this.originLocation = originLocation;
  }


  public UntpEventCredentialSubject responsibleOperatorDid(@jakarta.annotation.Nullable String responsibleOperatorDid) {
    this.responsibleOperatorDid = responsibleOperatorDid;
    return this;
  }

  /**
   * DID of the responsible economic operator. Fallback for &#x60;bizLocation&#x60;, and (only when &#x60;issuer&#x60; is absent) for the operator-scope check on operator-scoped API keys.
   * @return responsibleOperatorDid
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RESPONSIBLE_OPERATOR_DID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getResponsibleOperatorDid() {
    return responsibleOperatorDid;
  }


  @JsonProperty(JSON_PROPERTY_RESPONSIBLE_OPERATOR_DID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setResponsibleOperatorDid(@jakarta.annotation.Nullable String responsibleOperatorDid) {
    this.responsibleOperatorDid = responsibleOperatorDid;
  }


  /**
   * Return true if this UntpEventCredentialSubject object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UntpEventCredentialSubject untpEventCredentialSubject = (UntpEventCredentialSubject) o;
    return Objects.equals(this.id, untpEventCredentialSubject.id) &&
        Objects.equals(this.eventType, untpEventCredentialSubject.eventType) &&
        Objects.equals(this.action, untpEventCredentialSubject.action) &&
        Objects.equals(this.bizStep, untpEventCredentialSubject.bizStep) &&
        Objects.equals(this.disposition, untpEventCredentialSubject.disposition) &&
        Objects.equals(this.readPoint, untpEventCredentialSubject.readPoint) &&
        Objects.equals(this.bizLocation, untpEventCredentialSubject.bizLocation) &&
        Objects.equals(this.eventTime, untpEventCredentialSubject.eventTime) &&
        Objects.equals(this.epcList, untpEventCredentialSubject.epcList) &&
        Objects.equals(this.parentEpc, untpEventCredentialSubject.parentEpc) &&
        Objects.equals(this.childEpcs, untpEventCredentialSubject.childEpcs) &&
        Objects.equals(this.inputEpcList, untpEventCredentialSubject.inputEpcList) &&
        Objects.equals(this.outputEpcList, untpEventCredentialSubject.outputEpcList) &&
        Objects.equals(this.originLocation, untpEventCredentialSubject.originLocation) &&
        Objects.equals(this.responsibleOperatorDid, untpEventCredentialSubject.responsibleOperatorDid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, eventType, action, bizStep, disposition, readPoint, bizLocation, eventTime, epcList, parentEpc, childEpcs, inputEpcList, outputEpcList, originLocation, responsibleOperatorDid);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UntpEventCredentialSubject {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    bizStep: ").append(toIndentedString(bizStep)).append("\n");
    sb.append("    disposition: ").append(toIndentedString(disposition)).append("\n");
    sb.append("    readPoint: ").append(toIndentedString(readPoint)).append("\n");
    sb.append("    bizLocation: ").append(toIndentedString(bizLocation)).append("\n");
    sb.append("    eventTime: ").append(toIndentedString(eventTime)).append("\n");
    sb.append("    epcList: ").append(toIndentedString(epcList)).append("\n");
    sb.append("    parentEpc: ").append(toIndentedString(parentEpc)).append("\n");
    sb.append("    childEpcs: ").append(toIndentedString(childEpcs)).append("\n");
    sb.append("    inputEpcList: ").append(toIndentedString(inputEpcList)).append("\n");
    sb.append("    outputEpcList: ").append(toIndentedString(outputEpcList)).append("\n");
    sb.append("    originLocation: ").append(toIndentedString(originLocation)).append("\n");
    sb.append("    responsibleOperatorDid: ").append(toIndentedString(responsibleOperatorDid)).append("\n");
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

    // add `eventType` to the URL query string
    if (getEventType() != null) {
      joiner.add(String.format("%seventType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventType()))));
    }

    // add `action` to the URL query string
    if (getAction() != null) {
      joiner.add(String.format("%saction%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAction()))));
    }

    // add `bizStep` to the URL query string
    if (getBizStep() != null) {
      joiner.add(String.format("%sbizStep%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBizStep()))));
    }

    // add `disposition` to the URL query string
    if (getDisposition() != null) {
      joiner.add(String.format("%sdisposition%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDisposition()))));
    }

    // add `readPoint` to the URL query string
    if (getReadPoint() != null) {
      joiner.add(String.format("%sreadPoint%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getReadPoint()))));
    }

    // add `bizLocation` to the URL query string
    if (getBizLocation() != null) {
      joiner.add(String.format("%sbizLocation%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getBizLocation()))));
    }

    // add `eventTime` to the URL query string
    if (getEventTime() != null) {
      joiner.add(String.format("%seventTime%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEventTime()))));
    }

    // add `epcList` to the URL query string
    if (getEpcList() != null) {
      for (int i = 0; i < getEpcList().size(); i++) {
        joiner.add(String.format("%sepcList%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getEpcList().get(i)))));
      }
    }

    // add `parentEpc` to the URL query string
    if (getParentEpc() != null) {
      joiner.add(String.format("%sparentEpc%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getParentEpc()))));
    }

    // add `childEpcs` to the URL query string
    if (getChildEpcs() != null) {
      for (int i = 0; i < getChildEpcs().size(); i++) {
        joiner.add(String.format("%schildEpcs%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getChildEpcs().get(i)))));
      }
    }

    // add `inputEpcList` to the URL query string
    if (getInputEpcList() != null) {
      for (int i = 0; i < getInputEpcList().size(); i++) {
        joiner.add(String.format("%sinputEpcList%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getInputEpcList().get(i)))));
      }
    }

    // add `outputEpcList` to the URL query string
    if (getOutputEpcList() != null) {
      for (int i = 0; i < getOutputEpcList().size(); i++) {
        joiner.add(String.format("%soutputEpcList%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getOutputEpcList().get(i)))));
      }
    }

    // add `originLocation` to the URL query string
    if (getOriginLocation() != null) {
      joiner.add(getOriginLocation().toUrlQueryString(prefix + "originLocation" + suffix));
    }

    // add `responsibleOperatorDid` to the URL query string
    if (getResponsibleOperatorDid() != null) {
      joiner.add(String.format("%sresponsibleOperatorDid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getResponsibleOperatorDid()))));
    }

    return joiner.toString();
  }
}

