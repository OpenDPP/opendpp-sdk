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

package eu.opendppnode.sdk.api;

import eu.opendppnode.sdk.invoker.ApiClient;
import eu.opendppnode.sdk.invoker.ApiException;
import eu.opendppnode.sdk.invoker.ApiResponse;
import eu.opendppnode.sdk.invoker.Configuration;
import eu.opendppnode.sdk.invoker.Pair;

import eu.opendppnode.sdk.model.BatteryUnitTombstoneJsonLd;
import eu.opendppnode.sdk.model.DecodeGs1200Response;
import eu.opendppnode.sdk.model.DecodeGs1Batch200Response;
import eu.opendppnode.sdk.model.DecodeGs1BatchRequest;
import eu.opendppnode.sdk.model.DecodeGs1Request;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.MintGtinCheckDigit200Response;
import eu.opendppnode.sdk.model.MintGtinCheckDigit400Response;
import eu.opendppnode.sdk.model.MintGtinCheckDigitRequest;
import eu.opendppnode.sdk.model.PublicBatteryUnitJsonLd;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PublicResolutionApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public PublicResolutionApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PublicResolutionApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
    memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
  }

  protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
    String body = response.body() == null ? null : new String(response.body().readAllBytes());
    String message = formatExceptionMessage(operationId, response.statusCode(), body);
    return new ApiException(response.statusCode(), message, response.headers(), body);
  }

  private String formatExceptionMessage(String operationId, int statusCode, String body) {
    if (body == null || body.isEmpty()) {
      body = "[no body]";
    }
    return operationId + " call failed with: " + statusCode + " - " + body;
  }

  /**
   * Decode GS1 scan data / element string / Digital Link into structured AIs + HRI
   * Decodes raw scanner output — AIM-symbology-prefixed **scan data** (e.g. &#x60;]Q1https://id.gs1.org/01/09501101532007/21/VM-1&#x60;, &#x60;]C1010950…&#x60;), a bracketed GS1 **element string** (&#x60;(01)09501101532007(21)VM-1&#x60;), or a **Digital Link** URI — into its structured Application Identifiers, the Human-Readable Interpretation (HRI), and a Digital Link that resolves on this node. Parsing is performed by GS1&#39;s authoritative Barcode Syntax Engine (vendored WASM), so check digits and the AI grammar are validated, not approximated.  **Public + stateless** — no permission and no tenant data is touched; it complements the public resolver. Supply **exactly one** of &#x60;scanData&#x60;, &#x60;elementString&#x60;, &#x60;digitalLink&#x60;; zero or more than one returns 400. After decoding, &#x60;GET&#x60; the returned &#x60;digitalLinkUri&#x60; (the canonical path rehosted on this node) to resolve the passport/unit.  **Errors:** missing/multiple/over-long input, or a value GS1&#39;s grammar rejects, returns **400** (&#x60;Provide exactly one of: scanData, elementString, digitalLink&#x60; or &#x60;Not a valid GS1 &lt;kind&gt;: &lt;engine message&gt;&#x60;); **503** if the engine is unavailable.  **Rate limit:** global limiter only — 100 req/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param decodeGs1Request  (required)
   * @return DecodeGs1200Response
   * @throws ApiException if fails to make API call
   */
  public DecodeGs1200Response decodeGs1(DecodeGs1Request decodeGs1Request) throws ApiException {
    ApiResponse<DecodeGs1200Response> localVarResponse = decodeGs1WithHttpInfo(decodeGs1Request);
    return localVarResponse.getData();
  }

  /**
   * Decode GS1 scan data / element string / Digital Link into structured AIs + HRI
   * Decodes raw scanner output — AIM-symbology-prefixed **scan data** (e.g. &#x60;]Q1https://id.gs1.org/01/09501101532007/21/VM-1&#x60;, &#x60;]C1010950…&#x60;), a bracketed GS1 **element string** (&#x60;(01)09501101532007(21)VM-1&#x60;), or a **Digital Link** URI — into its structured Application Identifiers, the Human-Readable Interpretation (HRI), and a Digital Link that resolves on this node. Parsing is performed by GS1&#39;s authoritative Barcode Syntax Engine (vendored WASM), so check digits and the AI grammar are validated, not approximated.  **Public + stateless** — no permission and no tenant data is touched; it complements the public resolver. Supply **exactly one** of &#x60;scanData&#x60;, &#x60;elementString&#x60;, &#x60;digitalLink&#x60;; zero or more than one returns 400. After decoding, &#x60;GET&#x60; the returned &#x60;digitalLinkUri&#x60; (the canonical path rehosted on this node) to resolve the passport/unit.  **Errors:** missing/multiple/over-long input, or a value GS1&#39;s grammar rejects, returns **400** (&#x60;Provide exactly one of: scanData, elementString, digitalLink&#x60; or &#x60;Not a valid GS1 &lt;kind&gt;: &lt;engine message&gt;&#x60;); **503** if the engine is unavailable.  **Rate limit:** global limiter only — 100 req/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param decodeGs1Request  (required)
   * @return ApiResponse&lt;DecodeGs1200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DecodeGs1200Response> decodeGs1WithHttpInfo(DecodeGs1Request decodeGs1Request) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = decodeGs1RequestBuilder(decodeGs1Request);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("decodeGs1", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DecodeGs1200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DecodeGs1200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DecodeGs1200Response>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder decodeGs1RequestBuilder(DecodeGs1Request decodeGs1Request) throws ApiException {
    // verify the required parameter 'decodeGs1Request' is set
    if (decodeGs1Request == null) {
      throw new ApiException(400, "Missing the required parameter 'decodeGs1Request' when calling decodeGs1");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/gs1/decode";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(decodeGs1Request);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Batch-decode many GS1 scans / element strings / Digital Links in one request
   * Batch form of &#x60;POST /api/v1/gs1/decode&#x60; for line-side / warehouse stations capturing many scans per second. Send &#x60;{ \&quot;items\&quot;: [ … ] }&#x60; (≤200), each item exactly one of &#x60;scanData&#x60;/&#x60;elementString&#x60;/&#x60;digitalLink&#x60;, and receive a &#x60;results&#x60; array aligned to input order — each entry either a decoded scan (&#x60;ok: true&#x60;, the same fields as the single-scan 200 minus &#x60;success&#x60;) or an error (&#x60;ok: false&#x60; + &#x60;error&#x60;). **Partial-success:** one bad item never fails the batch — the request returns **200** and per-item failures are reported in place. Parsing uses GS1&#39;s authoritative Barcode Syntax Engine (vendored WASM). **Public + stateless** (no permission, no tenant data).  **Errors:** a missing/empty/non-array &#x60;items&#x60;, or more than 200 items, returns **400**; a body over the 256 KiB route cap returns **413**; **503** if the engine is unavailable.  **Rate limit:** global limiter only — 100 req/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param decodeGs1BatchRequest  (required)
   * @return DecodeGs1Batch200Response
   * @throws ApiException if fails to make API call
   */
  public DecodeGs1Batch200Response decodeGs1Batch(DecodeGs1BatchRequest decodeGs1BatchRequest) throws ApiException {
    ApiResponse<DecodeGs1Batch200Response> localVarResponse = decodeGs1BatchWithHttpInfo(decodeGs1BatchRequest);
    return localVarResponse.getData();
  }

  /**
   * Batch-decode many GS1 scans / element strings / Digital Links in one request
   * Batch form of &#x60;POST /api/v1/gs1/decode&#x60; for line-side / warehouse stations capturing many scans per second. Send &#x60;{ \&quot;items\&quot;: [ … ] }&#x60; (≤200), each item exactly one of &#x60;scanData&#x60;/&#x60;elementString&#x60;/&#x60;digitalLink&#x60;, and receive a &#x60;results&#x60; array aligned to input order — each entry either a decoded scan (&#x60;ok: true&#x60;, the same fields as the single-scan 200 minus &#x60;success&#x60;) or an error (&#x60;ok: false&#x60; + &#x60;error&#x60;). **Partial-success:** one bad item never fails the batch — the request returns **200** and per-item failures are reported in place. Parsing uses GS1&#39;s authoritative Barcode Syntax Engine (vendored WASM). **Public + stateless** (no permission, no tenant data).  **Errors:** a missing/empty/non-array &#x60;items&#x60;, or more than 200 items, returns **400**; a body over the 256 KiB route cap returns **413**; **503** if the engine is unavailable.  **Rate limit:** global limiter only — 100 req/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param decodeGs1BatchRequest  (required)
   * @return ApiResponse&lt;DecodeGs1Batch200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DecodeGs1Batch200Response> decodeGs1BatchWithHttpInfo(DecodeGs1BatchRequest decodeGs1BatchRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = decodeGs1BatchRequestBuilder(decodeGs1BatchRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("decodeGs1Batch", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DecodeGs1Batch200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DecodeGs1Batch200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DecodeGs1Batch200Response>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder decodeGs1BatchRequestBuilder(DecodeGs1BatchRequest decodeGs1BatchRequest) throws ApiException {
    // verify the required parameter 'decodeGs1BatchRequest' is set
    if (decodeGs1BatchRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'decodeGs1BatchRequest' when calling decodeGs1Batch");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/gs1/decode/batch";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(decodeGs1BatchRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Compute a GTIN check digit from a company prefix + item reference
   * The actionable counterpart to the non-GS1 ingest advisory (#255): given the **GS1 company prefix your organisation legally owns** plus an item reference, OpenDPP computes the GS1 **mod-10 check digit** and returns the resulting 14-digit GTIN + a Digital Link preview. Set the GTIN as a passport &#x60;productId&#x60; to get a scannable GS1 Digital Link.  **It ONLY completes the check digit** — it never allocates a GS1 company prefix or asserts ownership (a real GTIN requires a prefix licensed to you by GS1). &#x60;gs1CompanyPrefix&#x60; is REQUIRED; a request with none is refused (**400**). &#x60;gs1CompanyPrefix + itemRef&#x60; must be exactly **13 digits** (the check digit forms the 14th) and both must be digit strings, else **400**.  Public + stateless (pure arithmetic; no tenant data). No authentication. Rate limit: global 100 requests/min per IP.
   * @param mintGtinCheckDigitRequest  (required)
   * @return MintGtinCheckDigit200Response
   * @throws ApiException if fails to make API call
   */
  public MintGtinCheckDigit200Response mintGtinCheckDigit(MintGtinCheckDigitRequest mintGtinCheckDigitRequest) throws ApiException {
    ApiResponse<MintGtinCheckDigit200Response> localVarResponse = mintGtinCheckDigitWithHttpInfo(mintGtinCheckDigitRequest);
    return localVarResponse.getData();
  }

  /**
   * Compute a GTIN check digit from a company prefix + item reference
   * The actionable counterpart to the non-GS1 ingest advisory (#255): given the **GS1 company prefix your organisation legally owns** plus an item reference, OpenDPP computes the GS1 **mod-10 check digit** and returns the resulting 14-digit GTIN + a Digital Link preview. Set the GTIN as a passport &#x60;productId&#x60; to get a scannable GS1 Digital Link.  **It ONLY completes the check digit** — it never allocates a GS1 company prefix or asserts ownership (a real GTIN requires a prefix licensed to you by GS1). &#x60;gs1CompanyPrefix&#x60; is REQUIRED; a request with none is refused (**400**). &#x60;gs1CompanyPrefix + itemRef&#x60; must be exactly **13 digits** (the check digit forms the 14th) and both must be digit strings, else **400**.  Public + stateless (pure arithmetic; no tenant data). No authentication. Rate limit: global 100 requests/min per IP.
   * @param mintGtinCheckDigitRequest  (required)
   * @return ApiResponse&lt;MintGtinCheckDigit200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MintGtinCheckDigit200Response> mintGtinCheckDigitWithHttpInfo(MintGtinCheckDigitRequest mintGtinCheckDigitRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = mintGtinCheckDigitRequestBuilder(mintGtinCheckDigitRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("mintGtinCheckDigit", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<MintGtinCheckDigit200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<MintGtinCheckDigit200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<MintGtinCheckDigit200Response>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder mintGtinCheckDigitRequestBuilder(MintGtinCheckDigitRequest mintGtinCheckDigitRequest) throws ApiException {
    // verify the required parameter 'mintGtinCheckDigitRequest' is set
    if (mintGtinCheckDigitRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'mintGtinCheckDigitRequest' when calling mintGtinCheckDigit");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/gs1/gtin";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(mintGtinCheckDigitRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * GS1 Digital Link resolution by GRAI (AI 8003)
   * Unified GS1 Digital Link gateway, GRAI branch (Global Returnable Asset Identifier). The GRAI is matched against &#x60;metadata.gtin&#x60;, &#x60;metadata.grai&#x60;, or the passport&#39;s &#x60;productId&#x60;. Everything else — content negotiation (JSON-LD default / &#x60;application/aas+json&#x60; / &#x60;application/vc+jwt&#x60; / &#x60;application/vc+ld+json&#x60; / &#x60;application/dc+sd-jwt&#x60; / &#x60;text/html&#x60;, &#x60;Vary: Accept&#x60;), access tiers (public / grant &#x60;dpp_li_…&#x60;·&#x60;dpp_auth_…&#x60; via Bearer or &#x60;?grant&#x3D;&#x60; / owner &#x3D; tenant API key as Bearer or legacy &#x60;opendpp_session&#x60; cookie value, never a Console JWT session), DRAFT hiding, tenant-subdomain scoping, the no-tenant-scope ambiguity 400, access-audit logging, grant response headers, and the 30 req/min/IP in-memory rate limit (two-field 429 body without &#x60;success&#x60;; the limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global 100 req/min/IP limit, which applies on top) — is identical to &#x60;GET /01/{gtin14}&#x60;; see that operation and &#x60;GET /passport/{id}&#x60; for full semantics.  An additional &#x60;/21/{serial}&#x60; AI pair after the GRAI behaves exactly like &#x60;GET /01/{gtin14}/21/{serial}&#x60; (302 redirect to &#x60;/unit/{id}&#x60; or &#x60;/passport/{id}&#x60;). No permission string (public endpoint).
   * @param grai GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd resolveGs1Grai(String grai, String grant) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = resolveGs1GraiWithHttpInfo(grai, grant);
    return localVarResponse.getData();
  }

  /**
   * GS1 Digital Link resolution by GRAI (AI 8003)
   * Unified GS1 Digital Link gateway, GRAI branch (Global Returnable Asset Identifier). The GRAI is matched against &#x60;metadata.gtin&#x60;, &#x60;metadata.grai&#x60;, or the passport&#39;s &#x60;productId&#x60;. Everything else — content negotiation (JSON-LD default / &#x60;application/aas+json&#x60; / &#x60;application/vc+jwt&#x60; / &#x60;application/vc+ld+json&#x60; / &#x60;application/dc+sd-jwt&#x60; / &#x60;text/html&#x60;, &#x60;Vary: Accept&#x60;), access tiers (public / grant &#x60;dpp_li_…&#x60;·&#x60;dpp_auth_…&#x60; via Bearer or &#x60;?grant&#x3D;&#x60; / owner &#x3D; tenant API key as Bearer or legacy &#x60;opendpp_session&#x60; cookie value, never a Console JWT session), DRAFT hiding, tenant-subdomain scoping, the no-tenant-scope ambiguity 400, access-audit logging, grant response headers, and the 30 req/min/IP in-memory rate limit (two-field 429 body without &#x60;success&#x60;; the limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global 100 req/min/IP limit, which applies on top) — is identical to &#x60;GET /01/{gtin14}&#x60;; see that operation and &#x60;GET /passport/{id}&#x60; for full semantics.  An additional &#x60;/21/{serial}&#x60; AI pair after the GRAI behaves exactly like &#x60;GET /01/{gtin14}/21/{serial}&#x60; (302 redirect to &#x60;/unit/{id}&#x60; or &#x60;/passport/{id}&#x60;). No permission string (public endpoint).
   * @param grai GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> resolveGs1GraiWithHttpInfo(String grai, String grant) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = resolveGs1GraiRequestBuilder(grai, grant);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("resolveGs1Grai", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PublicPassportJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PublicPassportJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PublicPassportJsonLd>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder resolveGs1GraiRequestBuilder(String grai, String grant) throws ApiException {
    // verify the required parameter 'grai' is set
    if (grai == null) {
      throw new ApiException(400, "Missing the required parameter 'grai' when calling resolveGs1Grai");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/8003/{grai}"
        .replace("{grai}", ApiClient.urlEncode(grai.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "grant";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("grant", grant));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/ld+json, application/aas+json, application/vc+jwt, application/vc+ld+json, application/dc+sd-jwt, text/html, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * GS1 Digital Link resolution by GTIN-14 (AI 01)
   * Unified GS1 Digital Link gateway, GTIN branch. The GTIN-14 is matched against &#x60;metadata.gtin&#x60;, &#x60;metadata.grai&#x60;, or the passport&#39;s &#x60;productId&#x60;. On tenant workspaces (&#x60;https://{tenant}.opendpp-node.eu&#x60;) the lookup is scoped to that tenant — an unknown subdomain returns 404. Without a tenant scope, a GTIN matching more than one passport is rejected with 400 (ambiguous); disambiguate via a brand subdomain (the &#x60;?subdomain&#x3D;&#x60; query override is honoured in non-production environments only).  Content negotiation (RFC 7231 §5.3.2 &#x60;Accept&#x60; q-value negotiation; JSON-LD default / &#x60;application/aas+json&#x60; / &#x60;application/vc+jwt&#x60; / &#x60;application/vc+ld+json&#x60; / &#x60;application/dc+sd-jwt&#x60; / &#x60;text/html&#x60;, &#x60;Vary: Accept&#x60; always set), access tiers (public / &#x60;dpp_li_…&#x60;·&#x60;dpp_auth_…&#x60; grant via Bearer or &#x60;?grant&#x3D;&#x60; / owner &#x3D; a tenant **API key** sent as Bearer or, legacy, as the literal &#x60;opendpp_session&#x60; cookie value — Console JWT login sessions do **not** unlock owner tier), DRAFT hiding, access-audit logging (anonymized IP), and grant response headers (&#x60;Cache-Control: private, no-store&#x60;, &#x60;Referrer-Policy: no-referrer&#x60;) are identical to &#x60;GET /passport/{id}&#x60; — see that operation for the full tier semantics. No permission string (public endpoint); invalid credentials silently degrade to the public tier, never 401/403.  The gateway also accepts additional GS1 AI key/value path pairs after the GTIN; the only one acted on is AI 21 (serial) — documented separately as &#x60;GET /01/{gtin14}/21/{serial}&#x60;. (The underlying route is &#x60;GET /{ai}/_*&#x60;; AI prefixes other than &#x60;01&#x60; and &#x60;8003&#x60; get a 400.) This resolver handles only the **UNCOMPRESSED** GS1 Digital Link grammar; a **compressed** Digital Link (its AI data encoded as a base64url blob) is detected and rejected with a clear 400 that points to the uncompressed form (#261).  **Rate limit:** 30 requests/min/IP, per-process in-memory limiter; two-field 429 body without &#x60;success&#x60;. The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers on responses come from the global platform limit (100 req/min/IP), which applies on top.
   * @param gtin14 GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are &#x60;private, no-store&#x60; and the parameter is redacted from logs. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd resolveGs1Gtin(String gtin14, String grant) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = resolveGs1GtinWithHttpInfo(gtin14, grant);
    return localVarResponse.getData();
  }

  /**
   * GS1 Digital Link resolution by GTIN-14 (AI 01)
   * Unified GS1 Digital Link gateway, GTIN branch. The GTIN-14 is matched against &#x60;metadata.gtin&#x60;, &#x60;metadata.grai&#x60;, or the passport&#39;s &#x60;productId&#x60;. On tenant workspaces (&#x60;https://{tenant}.opendpp-node.eu&#x60;) the lookup is scoped to that tenant — an unknown subdomain returns 404. Without a tenant scope, a GTIN matching more than one passport is rejected with 400 (ambiguous); disambiguate via a brand subdomain (the &#x60;?subdomain&#x3D;&#x60; query override is honoured in non-production environments only).  Content negotiation (RFC 7231 §5.3.2 &#x60;Accept&#x60; q-value negotiation; JSON-LD default / &#x60;application/aas+json&#x60; / &#x60;application/vc+jwt&#x60; / &#x60;application/vc+ld+json&#x60; / &#x60;application/dc+sd-jwt&#x60; / &#x60;text/html&#x60;, &#x60;Vary: Accept&#x60; always set), access tiers (public / &#x60;dpp_li_…&#x60;·&#x60;dpp_auth_…&#x60; grant via Bearer or &#x60;?grant&#x3D;&#x60; / owner &#x3D; a tenant **API key** sent as Bearer or, legacy, as the literal &#x60;opendpp_session&#x60; cookie value — Console JWT login sessions do **not** unlock owner tier), DRAFT hiding, access-audit logging (anonymized IP), and grant response headers (&#x60;Cache-Control: private, no-store&#x60;, &#x60;Referrer-Policy: no-referrer&#x60;) are identical to &#x60;GET /passport/{id}&#x60; — see that operation for the full tier semantics. No permission string (public endpoint); invalid credentials silently degrade to the public tier, never 401/403.  The gateway also accepts additional GS1 AI key/value path pairs after the GTIN; the only one acted on is AI 21 (serial) — documented separately as &#x60;GET /01/{gtin14}/21/{serial}&#x60;. (The underlying route is &#x60;GET /{ai}/_*&#x60;; AI prefixes other than &#x60;01&#x60; and &#x60;8003&#x60; get a 400.) This resolver handles only the **UNCOMPRESSED** GS1 Digital Link grammar; a **compressed** Digital Link (its AI data encoded as a base64url blob) is detected and rejected with a clear 400 that points to the uncompressed form (#261).  **Rate limit:** 30 requests/min/IP, per-process in-memory limiter; two-field 429 body without &#x60;success&#x60;. The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers on responses come from the global platform limit (100 req/min/IP), which applies on top.
   * @param gtin14 GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are &#x60;private, no-store&#x60; and the parameter is redacted from logs. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> resolveGs1GtinWithHttpInfo(String gtin14, String grant) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = resolveGs1GtinRequestBuilder(gtin14, grant);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("resolveGs1Gtin", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PublicPassportJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PublicPassportJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PublicPassportJsonLd>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder resolveGs1GtinRequestBuilder(String gtin14, String grant) throws ApiException {
    // verify the required parameter 'gtin14' is set
    if (gtin14 == null) {
      throw new ApiException(400, "Missing the required parameter 'gtin14' when calling resolveGs1Gtin");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/01/{gtin14}"
        .replace("{gtin14}", ApiClient.urlEncode(gtin14.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "grant";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("grant", grant));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/ld+json, application/aas+json, application/vc+jwt, application/vc+ld+json, application/dc+sd-jwt, text/html, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * GS1 Digital Link serialised-item redirect (AI 01 + AI 21)
   * GS1 Digital Link resolution of an *individual serialised item*. This path never returns a document directly — on success it issues a &#x60;302&#x60; redirect (the query string, including &#x60;?grant&#x3D;&#x60;, is preserved on the &#x60;Location&#x60; URL):  1. If the GTIN resolves to a SKU/type passport that has a serialised battery unit whose &#x60;serialNumber&#x60; equals the AI-21 value → &#x60;302&#x60; to &#x60;/unit/{unitId}&#x60; (Battery Reg. Art. 77(2) per-unit view). 2. Otherwise (legacy fallback) the AI-21 value is matched against the passport UUID, &#x60;metadata.serialNumber&#x60;, or &#x60;metadata[\&quot;21\&quot;]&#x60;; if a passport matches → &#x60;302&#x60; to &#x60;/passport/{passportId}&#x60;. 3. Otherwise → &#x60;404&#x60; (content-negotiated).  The ambiguity check of the bare-GTIN branch is skipped when an AI-21 serial is present. The redirect handler itself never evaluates credentials — access tiers (owner / grant / public) apply at the redirect target; carry the grant in &#x60;?grant&#x3D;&#x60; (preserved across the redirect) or re-send the &#x60;Authorization&#x60; header to the target. On tenant subdomains the lookup is scoped to that tenant (unknown subdomain → 404, JSON only).  No permission string (public endpoint). **Rate limit:** 30 requests/min/IP (in-memory public limiter; two-field 429 body without &#x60;success&#x60;). The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global platform limit, which applies on top — and the redirect target counts as a second request against both.
   * @param gtin14 GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side). (required)
   * @param serial GS1 AI-21 serial. For serialised battery units this is the unit&#39;s physical serial (units are created matching &#x60;^[A-Za-z0-9._-]{1,20}$&#x60;); the legacy fallback also matches a passport UUID or the passport&#39;s &#x60;metadata.serialNumber&#x60; / &#x60;metadata[\&quot;21\&quot;]&#x60; value. Percent-encode reserved characters; the segment is URL-decoded before matching. (required)
   * @param grant Capability grant token. Not evaluated by this redirect handler — it is preserved on the &#x60;Location&#x60; URL and takes effect at the redirect target. (optional)
   * @throws ApiException if fails to make API call
   */
  public void resolveGs1GtinSerial(String gtin14, String serial, String grant) throws ApiException {
    resolveGs1GtinSerialWithHttpInfo(gtin14, serial, grant);
  }

  /**
   * GS1 Digital Link serialised-item redirect (AI 01 + AI 21)
   * GS1 Digital Link resolution of an *individual serialised item*. This path never returns a document directly — on success it issues a &#x60;302&#x60; redirect (the query string, including &#x60;?grant&#x3D;&#x60;, is preserved on the &#x60;Location&#x60; URL):  1. If the GTIN resolves to a SKU/type passport that has a serialised battery unit whose &#x60;serialNumber&#x60; equals the AI-21 value → &#x60;302&#x60; to &#x60;/unit/{unitId}&#x60; (Battery Reg. Art. 77(2) per-unit view). 2. Otherwise (legacy fallback) the AI-21 value is matched against the passport UUID, &#x60;metadata.serialNumber&#x60;, or &#x60;metadata[\&quot;21\&quot;]&#x60;; if a passport matches → &#x60;302&#x60; to &#x60;/passport/{passportId}&#x60;. 3. Otherwise → &#x60;404&#x60; (content-negotiated).  The ambiguity check of the bare-GTIN branch is skipped when an AI-21 serial is present. The redirect handler itself never evaluates credentials — access tiers (owner / grant / public) apply at the redirect target; carry the grant in &#x60;?grant&#x3D;&#x60; (preserved across the redirect) or re-send the &#x60;Authorization&#x60; header to the target. On tenant subdomains the lookup is scoped to that tenant (unknown subdomain → 404, JSON only).  No permission string (public endpoint). **Rate limit:** 30 requests/min/IP (in-memory public limiter; two-field 429 body without &#x60;success&#x60;). The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global platform limit, which applies on top — and the redirect target counts as a second request against both.
   * @param gtin14 GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side). (required)
   * @param serial GS1 AI-21 serial. For serialised battery units this is the unit&#39;s physical serial (units are created matching &#x60;^[A-Za-z0-9._-]{1,20}$&#x60;); the legacy fallback also matches a passport UUID or the passport&#39;s &#x60;metadata.serialNumber&#x60; / &#x60;metadata[\&quot;21\&quot;]&#x60; value. Percent-encode reserved characters; the segment is URL-decoded before matching. (required)
   * @param grant Capability grant token. Not evaluated by this redirect handler — it is preserved on the &#x60;Location&#x60; URL and takes effect at the redirect target. (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> resolveGs1GtinSerialWithHttpInfo(String gtin14, String serial, String grant) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = resolveGs1GtinSerialRequestBuilder(gtin14, serial, grant);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("resolveGs1GtinSerial", localVarResponse);
        }
        return new ApiResponse<>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            null
        );
      } finally {
        // Drain the InputStream
        while (localVarResponse.body().read() != -1) {
          // Ignore
        }
        localVarResponse.body().close();
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder resolveGs1GtinSerialRequestBuilder(String gtin14, String serial, String grant) throws ApiException {
    // verify the required parameter 'gtin14' is set
    if (gtin14 == null) {
      throw new ApiException(400, "Missing the required parameter 'gtin14' when calling resolveGs1GtinSerial");
    }
    // verify the required parameter 'serial' is set
    if (serial == null) {
      throw new ApiException(400, "Missing the required parameter 'serial' when calling resolveGs1GtinSerial");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/01/{gtin14}/21/{serial}"
        .replace("{gtin14}", ApiClient.urlEncode(gtin14.toString()))
        .replace("{serial}", ApiClient.urlEncode(serial.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "grant";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("grant", grant));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/json, text/html");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Resolve an individual serialised battery unit
   * Public, content-negotiated view of one individual serialised unit (battery; Reg. (EU) 2023/1542 Art. 77(2)) by its unit UUID, including the embedded SKU/type passport (&#x60;ofModel&#x60;, masked by the same tier rules as &#x60;GET /passport/{id}&#x60;).  **Content negotiation:** &#x60;Accept&#x60; containing &#x60;application/vc+jwt&#x60; (or bare &#x60;vc+jwt&#x60;) → a signed PER-UNIT (item-granularity) UNTP DigitalProductPassport credential (public tier; &#x60;406 Not Acceptable&#x60; when the unit&#39;s type passport has no manufacturing facility with a country of production); &#x60;application/vc+ld+json&#x60; (or bare &#x60;vc+ld+json&#x60;) → the same per-unit credential with an embedded &#x60;ecdsa-jcs-2019&#x60; Data Integrity proof, same &#x60;406&#x60;; &#x60;text/html&#x60; → server-rendered unit page; everything else → JSON-LD (&#x60;application/ld+json&#x60;). No AAS representation on this route. &#x60;Vary: Accept&#x60; always set on the 200. The &#x60;410&#x60; tombstone check (below) precedes content negotiation, so a recycled/ceased unit never yields a &#x60;vc+jwt&#x60; or &#x60;vc+ld+json&#x60;.  **Per-unit telemetry is never public** (Annex XIII(2)-(4)): anonymous responses omit &#x60;currentState&#x60;/&#x60;dynamicData&#x60; and instead carry a &#x60;restrictedData&#x60; notice with a &#x60;/request-access&#x60; pointer. An owner credential — a tenant **API key** (&#x60;op_dpp_token_…&#x60;) of the owning or operator-bound tenant, sent as Bearer or, legacy, as the literal &#x60;opendpp_session&#x60; cookie value (a Console JWT login session does **not** unlock owner tier) — or a valid grant token (&#x60;dpp_li_…&#x60;/&#x60;dpp_auth_…&#x60; as Bearer or &#x60;?grant&#x3D;&#x60;; TENANT, PASSPORT or UNIT scope) unlocks &#x60;currentState&#x60; and &#x60;dynamicData&#x60; — up to the 500 most recent events, newest first. Invalid credentials silently degrade to the public tier (never 401/402/403). Grant-unlocked responses add &#x60;Cache-Control: private, no-store&#x60; + &#x60;Referrer-Policy: no-referrer&#x60;. No permission string (public endpoint).  **Art. 77(8) tombstone:** once the unit&#39;s status is &#x60;RECYCLED&#x60; (or &#x60;ceasedAt&#x60; is set) this URL answers &#x60;410 Gone&#x60; with a minimal tombstone for everyone — grants and owner credentials do NOT override it (the owning tenant retains internal access via &#x60;GET /api/v1/units/{id}&#x60;).  Every resolution is access-audit-logged with an anonymized IP. **Rate limit:** 30 requests/min/IP (in-memory public limiter; two-field 429 body without &#x60;success&#x60;). The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global platform limit, which applies on top.
   * @param id The battery unit&#39;s server-assigned UUID (AI-21 serial resolution via &#x60;GET /01/{gtin14}/21/{serial}&#x60; redirects here). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret. (optional)
   * @return PublicBatteryUnitJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicBatteryUnitJsonLd resolvePublicBatteryUnit(String id, String grant) throws ApiException {
    ApiResponse<PublicBatteryUnitJsonLd> localVarResponse = resolvePublicBatteryUnitWithHttpInfo(id, grant);
    return localVarResponse.getData();
  }

  /**
   * Resolve an individual serialised battery unit
   * Public, content-negotiated view of one individual serialised unit (battery; Reg. (EU) 2023/1542 Art. 77(2)) by its unit UUID, including the embedded SKU/type passport (&#x60;ofModel&#x60;, masked by the same tier rules as &#x60;GET /passport/{id}&#x60;).  **Content negotiation:** &#x60;Accept&#x60; containing &#x60;application/vc+jwt&#x60; (or bare &#x60;vc+jwt&#x60;) → a signed PER-UNIT (item-granularity) UNTP DigitalProductPassport credential (public tier; &#x60;406 Not Acceptable&#x60; when the unit&#39;s type passport has no manufacturing facility with a country of production); &#x60;application/vc+ld+json&#x60; (or bare &#x60;vc+ld+json&#x60;) → the same per-unit credential with an embedded &#x60;ecdsa-jcs-2019&#x60; Data Integrity proof, same &#x60;406&#x60;; &#x60;text/html&#x60; → server-rendered unit page; everything else → JSON-LD (&#x60;application/ld+json&#x60;). No AAS representation on this route. &#x60;Vary: Accept&#x60; always set on the 200. The &#x60;410&#x60; tombstone check (below) precedes content negotiation, so a recycled/ceased unit never yields a &#x60;vc+jwt&#x60; or &#x60;vc+ld+json&#x60;.  **Per-unit telemetry is never public** (Annex XIII(2)-(4)): anonymous responses omit &#x60;currentState&#x60;/&#x60;dynamicData&#x60; and instead carry a &#x60;restrictedData&#x60; notice with a &#x60;/request-access&#x60; pointer. An owner credential — a tenant **API key** (&#x60;op_dpp_token_…&#x60;) of the owning or operator-bound tenant, sent as Bearer or, legacy, as the literal &#x60;opendpp_session&#x60; cookie value (a Console JWT login session does **not** unlock owner tier) — or a valid grant token (&#x60;dpp_li_…&#x60;/&#x60;dpp_auth_…&#x60; as Bearer or &#x60;?grant&#x3D;&#x60;; TENANT, PASSPORT or UNIT scope) unlocks &#x60;currentState&#x60; and &#x60;dynamicData&#x60; — up to the 500 most recent events, newest first. Invalid credentials silently degrade to the public tier (never 401/402/403). Grant-unlocked responses add &#x60;Cache-Control: private, no-store&#x60; + &#x60;Referrer-Policy: no-referrer&#x60;. No permission string (public endpoint).  **Art. 77(8) tombstone:** once the unit&#39;s status is &#x60;RECYCLED&#x60; (or &#x60;ceasedAt&#x60; is set) this URL answers &#x60;410 Gone&#x60; with a minimal tombstone for everyone — grants and owner credentials do NOT override it (the owning tenant retains internal access via &#x60;GET /api/v1/units/{id}&#x60;).  Every resolution is access-audit-logged with an anonymized IP. **Rate limit:** 30 requests/min/IP (in-memory public limiter; two-field 429 body without &#x60;success&#x60;). The limiter adds no headers of its own — &#x60;x-ratelimit-*&#x60; headers come from the global platform limit, which applies on top.
   * @param id The battery unit&#39;s server-assigned UUID (AI-21 serial resolution via &#x60;GET /01/{gtin14}/21/{serial}&#x60; redirects here). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; / &#x60;dpp_auth_…&#x60;); equivalent to &#x60;Authorization: Bearer&#x60;. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret. (optional)
   * @return ApiResponse&lt;PublicBatteryUnitJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicBatteryUnitJsonLd> resolvePublicBatteryUnitWithHttpInfo(String id, String grant) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = resolvePublicBatteryUnitRequestBuilder(id, grant);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("resolvePublicBatteryUnit", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PublicBatteryUnitJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PublicBatteryUnitJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PublicBatteryUnitJsonLd>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder resolvePublicBatteryUnitRequestBuilder(String id, String grant) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling resolvePublicBatteryUnit");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/unit/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "grant";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("grant", grant));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/ld+json, text/html, application/vc+jwt, application/vc+ld+json, application/dc+sd-jwt, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Resolve a passport by UUID (JSON-LD / AAS / HTML)
   * Public, content-negotiated resolution of a Digital Product Passport by its server-assigned UUID. Lookup is by primary key only — GTIN/GRAI/serial lookups go through the GS1 Digital Link gateway (&#x60;GET /01/{gtin14}&#x60;, &#x60;GET /8003/{grai}&#x60;).  **Content negotiation** — the representation is chosen by RFC 7231 §5.3.2 &#x60;Accept&#x60; q-value negotiation (highest q wins; ties broken by media-range specificity, then the client&#39;s stated order): &#x60;application/aas+json&#x60; (or bare &#x60;aas+json&#x60;) → role-filtered Asset Administration Shell environment; &#x60;application/vc+jwt&#x60; (or bare &#x60;vc+jwt&#x60;) → a signed UNTP DigitalProductPassport credential (public tier; &#x60;406 Not Acceptable&#x60; when the passport has no manufacturing facility with a country of production); &#x60;application/vc+ld+json&#x60; (or bare &#x60;vc+ld+json&#x60;) → the same credential with an embedded W3C Data Integrity proof (&#x60;ecdsa-jcs-2019&#x60;), same &#x60;406&#x60; condition; &#x60;application/dc+sd-jwt&#x60; (or the legacy &#x60;vc+sd-jwt&#x60;) → the same credential as an SD-JWT-VC for cryptographic selective disclosure (a holder presents a subset of &#x60;credentialSubject&#x60; claims), same &#x60;406&#x60; condition; &#x60;text/html&#x60; → server-rendered passport page. An absent &#x60;Accept&#x60;, or one matching only &#x60;*_/_*&#x60;, yields the canonical default JSON-LD (&#x60;application/ld+json&#x60;); an unsupported type is ignored. Because q-values and client order are honoured, &#x60;Accept: text/html, application/vc+jwt&#x60; selects HTML (the client&#39;s first preference), NOT &#x60;vc+jwt&#x60;. &#x60;Vary: Accept&#x60; is always set on the 200.  **Access tiers** — no permission string (public endpoint). Credentials are *optional* and never produce 401/402/403 here; an invalid or foreign credential silently degrades to the public tier: - **Public** (anonymous): restricted metadata keys (for category &#x60;batteries&#x60;: &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60;, &#x60;circularityAndDisassembly&#x60; — masked only when present) and the owner-only key &#x60;facilityDetails&#x60; (present-as-placeholder in every non-owner response, even when the underlying metadata never contained it) carry the literal placeholder &#x60;[REDACTED - Privileged Access Required]&#x60;. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in &#x60;proof.redactedLeaves&#x60;, so the eIDAS seal stays offline-verifiable after redaction; a placeholder-valued key with no &#x60;redactedLeaves&#x60; entry was never in the sealed metadata and must be excluded when rebuilding the root. - **Legitimate interest / authority**: a capability grant token — &#x60;dpp_li_…&#x60; (tenant-issued) or &#x60;dpp_auth_…&#x60; (platform-issued, not tenant-revocable) — sent as &#x60;Authorization: Bearer &lt;token&gt;&#x60; or &#x60;?grant&#x3D;&lt;token&gt;&#x60;, with TENANT or PASSPORT scope covering this passport, unlocks the restricted tier-2 keys. &#x60;facilityDetails&#x60;, the facility street address and DRAFT passports stay hidden. Grant-unlocked responses add &#x60;Cache-Control: private, no-store&#x60; and &#x60;Referrer-Policy: no-referrer&#x60;. - **Owner**: a tenant **API key** (&#x60;op_dpp_token_…&#x60;, shown once at creation) belonging to the owning tenant or to a tenant bound to the passport&#39;s economic operator — sent as Bearer or, legacy, as the literal value of the &#x60;opendpp_session&#x60; cookie. Only API keys are matched on the public resolvers: a Console JWT login session in that cookie does **not** unlock owner tier (it silently resolves as public). Owners see everything, including DRAFT passports, owner-only metadata keys and the facility street address (&#x60;manufacturingFacility.streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60;). In the AAS representation the owner credential&#39;s API-key role drives element filtering; a grant maps to the &#x60;legitimate_interest&#x60; filter tier, anonymous to &#x60;public&#x60;.  DRAFT passports are hidden from everyone but the owner (404 with a body identical to a true miss). Every resolution is recorded in the passport&#39;s access audit log with an anonymized IP.  **Rate limit:** 30 requests/min/IP via a per-process in-memory limiter; its 429 body is the two-field public error shape (no &#x60;success&#x60; field). This limiter adds no headers of its own — the &#x60;x-ratelimit-*&#x60; headers still present on responses (including these 429s) belong to the global platform limit (100 req/min/IP, 600/min for known crawler user agents), which applies on top.
   * @param id The passport&#39;s server-assigned UUID (returned as &#x60;id&#x60; on creation and embedded as AI-21 in the SKU-level Digital Link URI). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; legitimate-interest, &#x60;dpp_auth_…&#x60; authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as &#x60;Authorization: Bearer&#x60;. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace&#39;s sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry &#x60;Cache-Control: private, no-store&#x60; + &#x60;Referrer-Policy: no-referrer&#x60;, and the server log redacts the parameter. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd resolvePublicPassport(String id, String grant) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = resolvePublicPassportWithHttpInfo(id, grant);
    return localVarResponse.getData();
  }

  /**
   * Resolve a passport by UUID (JSON-LD / AAS / HTML)
   * Public, content-negotiated resolution of a Digital Product Passport by its server-assigned UUID. Lookup is by primary key only — GTIN/GRAI/serial lookups go through the GS1 Digital Link gateway (&#x60;GET /01/{gtin14}&#x60;, &#x60;GET /8003/{grai}&#x60;).  **Content negotiation** — the representation is chosen by RFC 7231 §5.3.2 &#x60;Accept&#x60; q-value negotiation (highest q wins; ties broken by media-range specificity, then the client&#39;s stated order): &#x60;application/aas+json&#x60; (or bare &#x60;aas+json&#x60;) → role-filtered Asset Administration Shell environment; &#x60;application/vc+jwt&#x60; (or bare &#x60;vc+jwt&#x60;) → a signed UNTP DigitalProductPassport credential (public tier; &#x60;406 Not Acceptable&#x60; when the passport has no manufacturing facility with a country of production); &#x60;application/vc+ld+json&#x60; (or bare &#x60;vc+ld+json&#x60;) → the same credential with an embedded W3C Data Integrity proof (&#x60;ecdsa-jcs-2019&#x60;), same &#x60;406&#x60; condition; &#x60;application/dc+sd-jwt&#x60; (or the legacy &#x60;vc+sd-jwt&#x60;) → the same credential as an SD-JWT-VC for cryptographic selective disclosure (a holder presents a subset of &#x60;credentialSubject&#x60; claims), same &#x60;406&#x60; condition; &#x60;text/html&#x60; → server-rendered passport page. An absent &#x60;Accept&#x60;, or one matching only &#x60;*_/_*&#x60;, yields the canonical default JSON-LD (&#x60;application/ld+json&#x60;); an unsupported type is ignored. Because q-values and client order are honoured, &#x60;Accept: text/html, application/vc+jwt&#x60; selects HTML (the client&#39;s first preference), NOT &#x60;vc+jwt&#x60;. &#x60;Vary: Accept&#x60; is always set on the 200.  **Access tiers** — no permission string (public endpoint). Credentials are *optional* and never produce 401/402/403 here; an invalid or foreign credential silently degrades to the public tier: - **Public** (anonymous): restricted metadata keys (for category &#x60;batteries&#x60;: &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60;, &#x60;circularityAndDisassembly&#x60; — masked only when present) and the owner-only key &#x60;facilityDetails&#x60; (present-as-placeholder in every non-owner response, even when the underlying metadata never contained it) carry the literal placeholder &#x60;[REDACTED - Privileged Access Required]&#x60;. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in &#x60;proof.redactedLeaves&#x60;, so the eIDAS seal stays offline-verifiable after redaction; a placeholder-valued key with no &#x60;redactedLeaves&#x60; entry was never in the sealed metadata and must be excluded when rebuilding the root. - **Legitimate interest / authority**: a capability grant token — &#x60;dpp_li_…&#x60; (tenant-issued) or &#x60;dpp_auth_…&#x60; (platform-issued, not tenant-revocable) — sent as &#x60;Authorization: Bearer &lt;token&gt;&#x60; or &#x60;?grant&#x3D;&lt;token&gt;&#x60;, with TENANT or PASSPORT scope covering this passport, unlocks the restricted tier-2 keys. &#x60;facilityDetails&#x60;, the facility street address and DRAFT passports stay hidden. Grant-unlocked responses add &#x60;Cache-Control: private, no-store&#x60; and &#x60;Referrer-Policy: no-referrer&#x60;. - **Owner**: a tenant **API key** (&#x60;op_dpp_token_…&#x60;, shown once at creation) belonging to the owning tenant or to a tenant bound to the passport&#39;s economic operator — sent as Bearer or, legacy, as the literal value of the &#x60;opendpp_session&#x60; cookie. Only API keys are matched on the public resolvers: a Console JWT login session in that cookie does **not** unlock owner tier (it silently resolves as public). Owners see everything, including DRAFT passports, owner-only metadata keys and the facility street address (&#x60;manufacturingFacility.streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60;). In the AAS representation the owner credential&#39;s API-key role drives element filtering; a grant maps to the &#x60;legitimate_interest&#x60; filter tier, anonymous to &#x60;public&#x60;.  DRAFT passports are hidden from everyone but the owner (404 with a body identical to a true miss). Every resolution is recorded in the passport&#39;s access audit log with an anonymized IP.  **Rate limit:** 30 requests/min/IP via a per-process in-memory limiter; its 429 body is the two-field public error shape (no &#x60;success&#x60; field). This limiter adds no headers of its own — the &#x60;x-ratelimit-*&#x60; headers still present on responses (including these 429s) belong to the global platform limit (100 req/min/IP, 600/min for known crawler user agents), which applies on top.
   * @param id The passport&#39;s server-assigned UUID (returned as &#x60;id&#x60; on creation and embedded as AI-21 in the SKU-level Digital Link URI). (required)
   * @param grant Capability grant token (&#x60;dpp_li_…&#x60; legitimate-interest, &#x60;dpp_auth_…&#x60; authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as &#x60;Authorization: Bearer&#x60;. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace&#39;s sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry &#x60;Cache-Control: private, no-store&#x60; + &#x60;Referrer-Policy: no-referrer&#x60;, and the server log redacts the parameter. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> resolvePublicPassportWithHttpInfo(String id, String grant) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = resolvePublicPassportRequestBuilder(id, grant);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("resolvePublicPassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PublicPassportJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PublicPassportJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PublicPassportJsonLd>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder resolvePublicPassportRequestBuilder(String id, String grant) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling resolvePublicPassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/passport/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "grant";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("grant", grant));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/ld+json, application/aas+json, application/vc+jwt, application/vc+ld+json, application/dc+sd-jwt, text/html, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

}
