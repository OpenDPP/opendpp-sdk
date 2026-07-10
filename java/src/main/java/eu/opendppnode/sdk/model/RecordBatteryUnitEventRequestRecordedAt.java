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
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.opendppnode.sdk.invoker.ApiClient;
import eu.opendppnode.sdk.invoker.JSON;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
@JsonDeserialize(using=RecordBatteryUnitEventRequestRecordedAt.RecordBatteryUnitEventRequestRecordedAtDeserializer.class)
@JsonSerialize(using = RecordBatteryUnitEventRequestRecordedAt.RecordBatteryUnitEventRequestRecordedAtSerializer.class)
public class RecordBatteryUnitEventRequestRecordedAt extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(RecordBatteryUnitEventRequestRecordedAt.class.getName());

    public static class RecordBatteryUnitEventRequestRecordedAtSerializer extends StdSerializer<RecordBatteryUnitEventRequestRecordedAt> {
        public RecordBatteryUnitEventRequestRecordedAtSerializer(Class<RecordBatteryUnitEventRequestRecordedAt> t) {
            super(t);
        }

        public RecordBatteryUnitEventRequestRecordedAtSerializer() {
            this(null);
        }

        @Override
        public void serialize(RecordBatteryUnitEventRequestRecordedAt value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class RecordBatteryUnitEventRequestRecordedAtDeserializer extends StdDeserializer<RecordBatteryUnitEventRequestRecordedAt> {
        public RecordBatteryUnitEventRequestRecordedAtDeserializer() {
            this(RecordBatteryUnitEventRequestRecordedAt.class);
        }

        public RecordBatteryUnitEventRequestRecordedAtDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public RecordBatteryUnitEventRequestRecordedAt deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();

            Object deserialized = null;
            // deserialize BigDecimal
            try {
                deserialized = tree.traverse(jp.getCodec()).readValueAs(BigDecimal.class);
                RecordBatteryUnitEventRequestRecordedAt ret = new RecordBatteryUnitEventRequestRecordedAt();
                ret.setActualInstance(deserialized);
                return ret;
            } catch (Exception e) {
                // deserialization failed, continue, log to help debugging
                log.log(Level.FINER, "Input data does not match 'RecordBatteryUnitEventRequestRecordedAt'", e);
            }

            // deserialize String
            try {
                deserialized = tree.traverse(jp.getCodec()).readValueAs(String.class);
                RecordBatteryUnitEventRequestRecordedAt ret = new RecordBatteryUnitEventRequestRecordedAt();
                ret.setActualInstance(deserialized);
                return ret;
            } catch (Exception e) {
                // deserialization failed, continue, log to help debugging
                log.log(Level.FINER, "Input data does not match 'RecordBatteryUnitEventRequestRecordedAt'", e);
            }

            throw new IOException(String.format("Failed deserialization for RecordBatteryUnitEventRequestRecordedAt: no match found"));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public RecordBatteryUnitEventRequestRecordedAt getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "RecordBatteryUnitEventRequestRecordedAt cannot be null");
        }
    }

    // store a list of schema names defined in anyOf
    public static final Map<String, Class<?>> schemas = new HashMap<String, Class<?>>();

    public RecordBatteryUnitEventRequestRecordedAt() {
        super("anyOf", Boolean.FALSE);
    }

    public RecordBatteryUnitEventRequestRecordedAt(BigDecimal o) {
        super("anyOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public RecordBatteryUnitEventRequestRecordedAt(String o) {
        super("anyOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("BigDecimal", BigDecimal.class);
        schemas.put("String", String.class);
        JSON.registerDescendants(RecordBatteryUnitEventRequestRecordedAt.class, Collections.unmodifiableMap(schemas));
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return RecordBatteryUnitEventRequestRecordedAt.schemas;
    }

    /**
     * Set the instance that matches the anyOf child schema, check
     * the instance parameter is valid against the anyOf child schemas:
     * BigDecimal, String
     *
     * It could be an instance of the 'anyOf' schemas.
     * The anyOf child schemas may themselves be a composed schema (allOf, anyOf, anyOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(BigDecimal.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(String.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be BigDecimal, String");
    }

    /**
     * Get the actual instance, which can be the following:
     * BigDecimal, String
     *
     * @return The actual instance (BigDecimal, String)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `BigDecimal`. If the actual instance is not `BigDecimal`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `BigDecimal`
     * @throws ClassCastException if the instance is not `BigDecimal`
     */
    public BigDecimal getBigDecimal() throws ClassCastException {
        return (BigDecimal)super.getActualInstance();
    }

    /**
     * Get the actual instance of `String`. If the actual instance is not `String`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `String`
     * @throws ClassCastException if the instance is not `String`
     */
    public String getString() throws ClassCastException {
        return (String)super.getActualInstance();
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

    return null;
  }

}

