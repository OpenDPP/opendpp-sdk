/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.16.0
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
import java.net.URI;
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
 * One data element of the EN 18223:2026 Annex A expanded form: its relative identifier, its clause 4 subclass, the identifier of its definition in the dictionary served at &#x60;GET /ns/dpp&#x60;, and — for a single-valued element — the Table 7 &#x60;valueDataType&#x60; and the &#x60;value&#x60;; a collection carries its members under &#x60;elements&#x60;, an ordered list its items under &#x60;value&#x60;, each item named by its position.
 */
@JsonPropertyOrder({
  En18223DataElement.JSON_PROPERTY_ELEMENT_ID,
  En18223DataElement.JSON_PROPERTY_OBJECT_TYPE,
  En18223DataElement.JSON_PROPERTY_DICTIONARY_REFERENCE,
  En18223DataElement.JSON_PROPERTY_VALUE_DATA_TYPE,
  En18223DataElement.JSON_PROPERTY_VALUE,
  En18223DataElement.JSON_PROPERTY_ELEMENTS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class En18223DataElement {
  public static final String JSON_PROPERTY_ELEMENT_ID = "elementId";
  @jakarta.annotation.Nonnull
  private String elementId;

  /**
   * The concrete DataElement subclass (4.1.2.3).
   */
  public enum ObjectTypeEnum {
    DATA_ELEMENT_COLLECTION(String.valueOf("DataElementCollection")),
    
    SINGLE_VALUED_DATA_ELEMENT(String.valueOf("SingleValuedDataElement")),
    
    MULTI_VALUED_DATA_ELEMENT(String.valueOf("MultiValuedDataElement")),
    
    RELATED_RESOURCE(String.valueOf("RelatedResource")),
    
    MULTI_LANGUAGE_DATA_ELEMENT(String.valueOf("MultiLanguageDataElement")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ObjectTypeEnum(String value) {
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
    public static ObjectTypeEnum fromValue(String value) {
      for (ObjectTypeEnum b : ObjectTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_OBJECT_TYPE = "objectType";
  @jakarta.annotation.Nonnull
  private ObjectTypeEnum objectType;

  public static final String JSON_PROPERTY_DICTIONARY_REFERENCE = "dictionaryReference";
  @jakarta.annotation.Nonnull
  private URI dictionaryReference;

  public static final String JSON_PROPERTY_VALUE_DATA_TYPE = "valueDataType";
  @jakarta.annotation.Nullable
  private String valueDataType;

  public static final String JSON_PROPERTY_VALUE = "value";
  private JsonNullable<Object> value = JsonNullable.<Object>of(null);

  public static final String JSON_PROPERTY_ELEMENTS = "elements";
  @jakarta.annotation.Nullable
  private List<En18223DataElement> elements = new ArrayList<>();

  public En18223DataElement() { 
  }

  public En18223DataElement elementId(@jakarta.annotation.Nonnull String elementId) {
    this.elementId = elementId;
    return this;
  }

  /**
   * The relative identifier of the element within its location (Table 2) — the key the compressed form uses.
   * @return elementId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ELEMENT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getElementId() {
    return elementId;
  }


  @JsonProperty(JSON_PROPERTY_ELEMENT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setElementId(@jakarta.annotation.Nonnull String elementId) {
    this.elementId = elementId;
  }


  public En18223DataElement objectType(@jakarta.annotation.Nonnull ObjectTypeEnum objectType) {
    this.objectType = objectType;
    return this;
  }

  /**
   * The concrete DataElement subclass (4.1.2.3).
   * @return objectType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_OBJECT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ObjectTypeEnum getObjectType() {
    return objectType;
  }


  @JsonProperty(JSON_PROPERTY_OBJECT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setObjectType(@jakarta.annotation.Nonnull ObjectTypeEnum objectType) {
    this.objectType = objectType;
  }


  public En18223DataElement dictionaryReference(@jakarta.annotation.Nonnull URI dictionaryReference) {
    this.dictionaryReference = dictionaryReference;
    return this;
  }

  /**
   * The unique identifier of the element&#39;s definition — &#x60;https://opendpp-node.eu/ns/dpp#&lt;path&gt;&#x60;, resolvable at &#x60;GET /ns/dpp&#x60; (4.3).
   * @return dictionaryReference
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DICTIONARY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getDictionaryReference() {
    return dictionaryReference;
  }


  @JsonProperty(JSON_PROPERTY_DICTIONARY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDictionaryReference(@jakarta.annotation.Nonnull URI dictionaryReference) {
    this.dictionaryReference = dictionaryReference;
  }


  public En18223DataElement valueDataType(@jakarta.annotation.Nullable String valueDataType) {
    this.valueDataType = valueDataType;
    return this;
  }

  /**
   * The XSD data type of the value (Table 7, e.g. &#x60;xsd:decimal&#x60;); present on single-valued elements and on lists whose items share one native type.
   * @return valueDataType
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VALUE_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getValueDataType() {
    return valueDataType;
  }


  @JsonProperty(JSON_PROPERTY_VALUE_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setValueDataType(@jakarta.annotation.Nullable String valueDataType) {
    this.valueDataType = valueDataType;
  }


  public En18223DataElement value(@jakarta.annotation.Nullable Object value) {
    this.value = JsonNullable.<Object>of(value);
    return this;
  }

  /**
   * Get value
   * @return value
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public Object getValue() {
        return value.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_VALUE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getValue_JsonNullable() {
    return value;
  }
  
  @JsonProperty(JSON_PROPERTY_VALUE)
  public void setValue_JsonNullable(JsonNullable<Object> value) {
    this.value = value;
  }

  public void setValue(@jakarta.annotation.Nullable Object value) {
    this.value = JsonNullable.<Object>of(value);
  }


  public En18223DataElement elements(@jakarta.annotation.Nullable List<En18223DataElement> elements) {
    this.elements = elements;
    return this;
  }

  public En18223DataElement addElementsItem(En18223DataElement elementsItem) {
    if (this.elements == null) {
      this.elements = new ArrayList<>();
    }
    this.elements.add(elementsItem);
    return this;
  }

  /**
   * A collection&#39;s members.
   * @return elements
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ELEMENTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<En18223DataElement> getElements() {
    return elements;
  }


  @JsonProperty(JSON_PROPERTY_ELEMENTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setElements(@jakarta.annotation.Nullable List<En18223DataElement> elements) {
    this.elements = elements;
  }


  /**
   * Return true if this En18223DataElement object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    En18223DataElement en18223DataElement = (En18223DataElement) o;
    return Objects.equals(this.elementId, en18223DataElement.elementId) &&
        Objects.equals(this.objectType, en18223DataElement.objectType) &&
        Objects.equals(this.dictionaryReference, en18223DataElement.dictionaryReference) &&
        Objects.equals(this.valueDataType, en18223DataElement.valueDataType) &&
        equalsNullable(this.value, en18223DataElement.value) &&
        Objects.equals(this.elements, en18223DataElement.elements);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementId, objectType, dictionaryReference, valueDataType, hashCodeNullable(value), elements);
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
    sb.append("class En18223DataElement {\n");
    sb.append("    elementId: ").append(toIndentedString(elementId)).append("\n");
    sb.append("    objectType: ").append(toIndentedString(objectType)).append("\n");
    sb.append("    dictionaryReference: ").append(toIndentedString(dictionaryReference)).append("\n");
    sb.append("    valueDataType: ").append(toIndentedString(valueDataType)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    elements: ").append(toIndentedString(elements)).append("\n");
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

    // add `elementId` to the URL query string
    if (getElementId() != null) {
      joiner.add(String.format("%selementId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getElementId()))));
    }

    // add `objectType` to the URL query string
    if (getObjectType() != null) {
      joiner.add(String.format("%sobjectType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getObjectType()))));
    }

    // add `dictionaryReference` to the URL query string
    if (getDictionaryReference() != null) {
      joiner.add(String.format("%sdictionaryReference%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDictionaryReference()))));
    }

    // add `valueDataType` to the URL query string
    if (getValueDataType() != null) {
      joiner.add(String.format("%svalueDataType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValueDataType()))));
    }

    // add `value` to the URL query string
    if (getValue() != null) {
      joiner.add(String.format("%svalue%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
    }

    // add `elements` to the URL query string
    if (getElements() != null) {
      for (int i = 0; i < getElements().size(); i++) {
        if (getElements().get(i) != null) {
          joiner.add(getElements().get(i).toUrlQueryString(String.format("%selements%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }
}

