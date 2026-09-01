/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.15.0
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
import eu.opendppnode.sdk.model.BatteryUnitSerialisationFailedError;
import eu.opendppnode.sdk.model.DefaultRequestRejectionError;
import eu.opendppnode.sdk.model.Error;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.opendppnode.sdk.invoker.ApiClient;
import eu.opendppnode.sdk.invoker.JSON;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
@JsonDeserialize(using = BatteryUnitSerialiseBadRequest.BatteryUnitSerialiseBadRequestDeserializer.class)
@JsonSerialize(using = BatteryUnitSerialiseBadRequest.BatteryUnitSerialiseBadRequestSerializer.class)
public class BatteryUnitSerialiseBadRequest extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(BatteryUnitSerialiseBadRequest.class.getName());

    public static class BatteryUnitSerialiseBadRequestSerializer extends StdSerializer<BatteryUnitSerialiseBadRequest> {
        public BatteryUnitSerialiseBadRequestSerializer(Class<BatteryUnitSerialiseBadRequest> t) {
            super(t);
        }

        public BatteryUnitSerialiseBadRequestSerializer() {
            this(null);
        }

        @Override
        public void serialize(BatteryUnitSerialiseBadRequest value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class BatteryUnitSerialiseBadRequestDeserializer extends StdDeserializer<BatteryUnitSerialiseBadRequest> {
        public BatteryUnitSerialiseBadRequestDeserializer() {
            this(BatteryUnitSerialiseBadRequest.class);
        }

        public BatteryUnitSerialiseBadRequestDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public BatteryUnitSerialiseBadRequest deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize BatteryUnitSerialisationFailedError
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (BatteryUnitSerialisationFailedError.class.equals(Integer.class) || BatteryUnitSerialisationFailedError.class.equals(Long.class) || BatteryUnitSerialisationFailedError.class.equals(Float.class) || BatteryUnitSerialisationFailedError.class.equals(Double.class) || BatteryUnitSerialisationFailedError.class.equals(Boolean.class) || BatteryUnitSerialisationFailedError.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((BatteryUnitSerialisationFailedError.class.equals(Integer.class) || BatteryUnitSerialisationFailedError.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((BatteryUnitSerialisationFailedError.class.equals(Float.class) || BatteryUnitSerialisationFailedError.class.equals(Double.class)) && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (BatteryUnitSerialisationFailedError.class.equals(Boolean.class) && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (BatteryUnitSerialisationFailedError.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(BatteryUnitSerialisationFailedError.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'BatteryUnitSerialisationFailedError'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'BatteryUnitSerialisationFailedError'", e);
            }

            // deserialize DefaultRequestRejectionError
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (DefaultRequestRejectionError.class.equals(Integer.class) || DefaultRequestRejectionError.class.equals(Long.class) || DefaultRequestRejectionError.class.equals(Float.class) || DefaultRequestRejectionError.class.equals(Double.class) || DefaultRequestRejectionError.class.equals(Boolean.class) || DefaultRequestRejectionError.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((DefaultRequestRejectionError.class.equals(Integer.class) || DefaultRequestRejectionError.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((DefaultRequestRejectionError.class.equals(Float.class) || DefaultRequestRejectionError.class.equals(Double.class)) && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (DefaultRequestRejectionError.class.equals(Boolean.class) && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (DefaultRequestRejectionError.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(DefaultRequestRejectionError.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'DefaultRequestRejectionError'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'DefaultRequestRejectionError'", e);
            }

            // deserialize Error
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (Error.class.equals(Integer.class) || Error.class.equals(Long.class) || Error.class.equals(Float.class) || Error.class.equals(Double.class) || Error.class.equals(Boolean.class) || Error.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((Error.class.equals(Integer.class) || Error.class.equals(Long.class)) && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((Error.class.equals(Float.class) || Error.class.equals(Double.class)) && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (Error.class.equals(Boolean.class) && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |= (Error.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(Error.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'Error'");
                }
            } catch (Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'Error'", e);
            }

            if (match == 1) {
                BatteryUnitSerialiseBadRequest ret = new BatteryUnitSerialiseBadRequest();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format("Failed deserialization for BatteryUnitSerialiseBadRequest: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public BatteryUnitSerialiseBadRequest getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "BatteryUnitSerialiseBadRequest cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<>();

    public BatteryUnitSerialiseBadRequest() {
        super("oneOf", Boolean.FALSE);
    }

    public BatteryUnitSerialiseBadRequest(BatteryUnitSerialisationFailedError o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public BatteryUnitSerialiseBadRequest(DefaultRequestRejectionError o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public BatteryUnitSerialiseBadRequest(Error o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("BatteryUnitSerialisationFailedError", BatteryUnitSerialisationFailedError.class);
        schemas.put("DefaultRequestRejectionError", DefaultRequestRejectionError.class);
        schemas.put("Error", Error.class);
        JSON.registerDescendants(BatteryUnitSerialiseBadRequest.class, Collections.unmodifiableMap(schemas));
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return BatteryUnitSerialiseBadRequest.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * BatteryUnitSerialisationFailedError, DefaultRequestRejectionError, Error
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(BatteryUnitSerialisationFailedError.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(DefaultRequestRejectionError.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(Error.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be BatteryUnitSerialisationFailedError, DefaultRequestRejectionError, Error");
    }

    /**
     * Get the actual instance, which can be the following:
     * BatteryUnitSerialisationFailedError, DefaultRequestRejectionError, Error
     *
     * @return The actual instance (BatteryUnitSerialisationFailedError, DefaultRequestRejectionError, Error)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `BatteryUnitSerialisationFailedError`. If the actual instance is not `BatteryUnitSerialisationFailedError`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `BatteryUnitSerialisationFailedError`
     * @throws ClassCastException if the instance is not `BatteryUnitSerialisationFailedError`
     */
    public BatteryUnitSerialisationFailedError getBatteryUnitSerialisationFailedError() throws ClassCastException {
        return (BatteryUnitSerialisationFailedError)super.getActualInstance();
    }

    /**
     * Get the actual instance of `DefaultRequestRejectionError`. If the actual instance is not `DefaultRequestRejectionError`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `DefaultRequestRejectionError`
     * @throws ClassCastException if the instance is not `DefaultRequestRejectionError`
     */
    public DefaultRequestRejectionError getDefaultRequestRejectionError() throws ClassCastException {
        return (DefaultRequestRejectionError)super.getActualInstance();
    }

    /**
     * Get the actual instance of `Error`. If the actual instance is not `Error`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Error`
     * @throws ClassCastException if the instance is not `Error`
     */
    public Error getError() throws ClassCastException {
        return (Error)super.getActualInstance();
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

    if (getActualInstance() instanceof Error) {
        if (getActualInstance() != null) {
          joiner.add(((Error)getActualInstance()).toUrlQueryString(prefix + "one_of_0" + suffix));
        }
        return joiner.toString();
    }
    if (getActualInstance() instanceof BatteryUnitSerialisationFailedError) {
        if (getActualInstance() != null) {
          joiner.add(((BatteryUnitSerialisationFailedError)getActualInstance()).toUrlQueryString(prefix + "one_of_1" + suffix));
        }
        return joiner.toString();
    }
    if (getActualInstance() instanceof DefaultRequestRejectionError) {
        if (getActualInstance() != null) {
          joiner.add(((DefaultRequestRejectionError)getActualInstance()).toUrlQueryString(prefix + "one_of_2" + suffix));
        }
        return joiner.toString();
    }
    return null;
  }

}

