/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.17.0
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

import eu.opendppnode.sdk.model.En18222ProductIdQuery;
import eu.opendppnode.sdk.model.En18222Result;
import java.time.OffsetDateTime;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;
import java.net.URI;

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
public class En18222LifeCycleApiApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public En18222LifeCycleApiApi() {
    this(Configuration.getDefaultApiClient());
  }

  public En18222LifeCycleApiApi(ApiClient apiClient) {
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
   * CreateDPP — create a new DPP and return its identifier
   * **EN 18222:2026 clause 4.6, Table 5; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator. OpenDPP is declared the main provider and never the creator: the economic operator is always the creator, and this method is how a creator&#39;s passport comes to be hosted here.  Table 5&#39;s output is the **DPP ID**, not the passport, so a successful creation answers &#x60;201&#x60; (EN 18222 Table 15 &#x60;SuccessCreated&#x60;) with the new &#x60;digitalProductPassportId&#x60; as the body and a &#x60;Location&#x60; header pointing at its &#x60;ReadDPPById&#x60; path.  **Permission:** &#x60;passport:create&#x60;.
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside &#x60;compressed&#x60; / &#x60;full&#x60; / &#x60;expanded&#x60; is nonetheless refused with &#x60;400&#x60;: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not. (optional)
   * @return URI
   * @throws ApiException if fails to make API call
   */
  public URI createDPP(Object body, String representation) throws ApiException {
    ApiResponse<URI> localVarResponse = createDPPWithHttpInfo(body, representation);
    return localVarResponse.getData();
  }

  /**
   * CreateDPP — create a new DPP and return its identifier
   * **EN 18222:2026 clause 4.6, Table 5; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator. OpenDPP is declared the main provider and never the creator: the economic operator is always the creator, and this method is how a creator&#39;s passport comes to be hosted here.  Table 5&#39;s output is the **DPP ID**, not the passport, so a successful creation answers &#x60;201&#x60; (EN 18222 Table 15 &#x60;SuccessCreated&#x60;) with the new &#x60;digitalProductPassportId&#x60; as the body and a &#x60;Location&#x60; header pointing at its &#x60;ReadDPPById&#x60; path.  **Permission:** &#x60;passport:create&#x60;.
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside &#x60;compressed&#x60; / &#x60;full&#x60; / &#x60;expanded&#x60; is nonetheless refused with &#x60;400&#x60;: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not. (optional)
   * @return ApiResponse&lt;URI&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<URI> createDPPWithHttpInfo(Object body, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = createDPPRequestBuilder(body, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("createDPP", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<URI>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<URI>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<URI>() {})
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

  private HttpRequest.Builder createDPPRequestBuilder(Object body, String representation) throws ApiException {
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createDPP");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(body);
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
   * DeleteDPPById — remove the DPP with the specified DPP ID
   * **EN 18222:2026 clause 4.8, Table 7; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator.  Table 7 declares no output payload, so a successful deletion answers &#x60;204&#x60; (EN 18222 Table 15 &#x60;SuccessNoContent&#x60;).  **Access rules control whether deletion is allowed**, and here they are strict: a passport that was ever published or sealed is **not** deletable, because ESPR Article 77(8) makes it persist. That refusal is reported, not softened — this method removes a passport that was never published.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteDPPById(String dppId) throws ApiException {
    deleteDPPByIdWithHttpInfo(dppId);
  }

  /**
   * DeleteDPPById — remove the DPP with the specified DPP ID
   * **EN 18222:2026 clause 4.8, Table 7; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator.  Table 7 declares no output payload, so a successful deletion answers &#x60;204&#x60; (EN 18222 Table 15 &#x60;SuccessNoContent&#x60;).  **Access rules control whether deletion is allowed**, and here they are strict: a passport that was ever published or sealed is **not** deletable, because ESPR Article 77(8) makes it persist. That refusal is reported, not softened — this method removes a passport that was never published.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteDPPByIdWithHttpInfo(String dppId) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteDPPByIdRequestBuilder(dppId);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteDPPById", localVarResponse);
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

  private HttpRequest.Builder deleteDPPByIdRequestBuilder(String dppId) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling deleteDPPById");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps/{dppId}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * ReadDPPById — return the DPP with the specified DPP ID
   * **EN 18222:2026 clause 4.2, Table 1; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available by the creator of the digital product passport or their main DPP service provider, as well as by a back-up DPP service provider. OpenDPP is declared the **main** provider.  The response is the passport itself: Table 16 reports the result \&quot;except status code\&quot;, so the status code is the HTTP status (EN 18222 Table 15) and the body is the payload alone, with no envelope.  **Permission:** &#x60;passport:read&#x60;. Access rules decide which data elements the response carries — an owner key sees the owner tier, other credentials the public-redacted one.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd readDPPById(String dppId, String representation) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = readDPPByIdWithHttpInfo(dppId, representation);
    return localVarResponse.getData();
  }

  /**
   * ReadDPPById — return the DPP with the specified DPP ID
   * **EN 18222:2026 clause 4.2, Table 1; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available by the creator of the digital product passport or their main DPP service provider, as well as by a back-up DPP service provider. OpenDPP is declared the **main** provider.  The response is the passport itself: Table 16 reports the result \&quot;except status code\&quot;, so the status code is the HTTP status (EN 18222 Table 15) and the body is the payload alone, with no envelope.  **Permission:** &#x60;passport:read&#x60;. Access rules decide which data elements the response carries — an owner key sees the owner tier, other credentials the public-redacted one.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> readDPPByIdWithHttpInfo(String dppId, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = readDPPByIdRequestBuilder(dppId, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("readDPPById", localVarResponse);
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

  private HttpRequest.Builder readDPPByIdRequestBuilder(String dppId, String representation) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling readDPPById");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps/{dppId}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

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
   * ReadDPPByProductId — return the current active DPP for a product identifier
   * **EN 18222:2026 clause 4.3, Table 2; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available.  Table 2 returns \&quot;the current active DPP (latest version)\&quot; for the unique product identifier as defined in EN 18219. This door reads the product identifier and nothing else — a &#x60;digitalProductPassportId&#x60; is not accepted, because EN 18222 gives that its own path.  **Permission:** &#x60;passport:read&#x60;.
   * @param productId The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator&#39;s own SKU), percent-encoded as one path segment. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd readDPPByProductId(String productId, String representation) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = readDPPByProductIdWithHttpInfo(productId, representation);
    return localVarResponse.getData();
  }

  /**
   * ReadDPPByProductId — return the current active DPP for a product identifier
   * **EN 18222:2026 clause 4.3, Table 2; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available.  Table 2 returns \&quot;the current active DPP (latest version)\&quot; for the unique product identifier as defined in EN 18219. This door reads the product identifier and nothing else — a &#x60;digitalProductPassportId&#x60; is not accepted, because EN 18222 gives that its own path.  **Permission:** &#x60;passport:read&#x60;.
   * @param productId The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator&#39;s own SKU), percent-encoded as one path segment. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> readDPPByProductIdWithHttpInfo(String productId, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = readDPPByProductIdRequestBuilder(productId, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("readDPPByProductId", localVarResponse);
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

  private HttpRequest.Builder readDPPByProductIdRequestBuilder(String productId, String representation) throws ApiException {
    // verify the required parameter 'productId' is set
    if (productId == null) {
      throw new ApiException(400, "Missing the required parameter 'productId' when calling readDPPByProductId");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dppsByProductId/{productId}"
        .replace("{productId}", ApiClient.urlEncode(productId.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

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
   * ReadDPPIdsByProductIds — return the DPP identifiers matching a set of product identifiers
   * **EN 18222:2026 clause 4.5, Table 4; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available, and it is the method EN 18222&#39;s own Annex ZA leans on hardest — five of Table ZA.1&#39;s six rows cite clause 4.5.  The result is the set of &#x60;digitalProductPassportId&#x60; values, not the passports; fetch each with &#x60;ReadDPPById&#x60;.  **Pagination.** Clause 4.5 lets the client set &#x60;limit&#x60; and &#x60;cursor&#x60;, and requires that \&quot;the value of the cursor shall not be empty\&quot; — a &#x60;cursor&#x60; present but empty is refused rather than treated as a restart, which would repeat rows the client already holds. Table 16 reports the result \&quot;except status code **and pagination**\&quot;, so the continuation is carried out of band as an RFC 8288 &#x60;Link&#x60; header with &#x60;rel&#x3D;\&quot;next\&quot;&#x60;; its absence means the last page.  **Permission:** &#x60;passport:read&#x60;.
   * @param en18222ProductIdQuery  (required)
   * @param limit Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one. (optional, default to 100)
   * @param cursor The continuation from a previous page&#39;s &#x60;Link: rel&#x3D;\&quot;next\&quot;&#x60; header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page. (optional)
   * @return List&lt;URI&gt;
   * @throws ApiException if fails to make API call
   */
  public List<URI> readDPPIdsByProductIds(En18222ProductIdQuery en18222ProductIdQuery, Integer limit, String cursor) throws ApiException {
    ApiResponse<List<URI>> localVarResponse = readDPPIdsByProductIdsWithHttpInfo(en18222ProductIdQuery, limit, cursor);
    return localVarResponse.getData();
  }

  /**
   * ReadDPPIdsByProductIds — return the DPP identifiers matching a set of product identifiers
   * **EN 18222:2026 clause 4.5, Table 4; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available, and it is the method EN 18222&#39;s own Annex ZA leans on hardest — five of Table ZA.1&#39;s six rows cite clause 4.5.  The result is the set of &#x60;digitalProductPassportId&#x60; values, not the passports; fetch each with &#x60;ReadDPPById&#x60;.  **Pagination.** Clause 4.5 lets the client set &#x60;limit&#x60; and &#x60;cursor&#x60;, and requires that \&quot;the value of the cursor shall not be empty\&quot; — a &#x60;cursor&#x60; present but empty is refused rather than treated as a restart, which would repeat rows the client already holds. Table 16 reports the result \&quot;except status code **and pagination**\&quot;, so the continuation is carried out of band as an RFC 8288 &#x60;Link&#x60; header with &#x60;rel&#x3D;\&quot;next\&quot;&#x60;; its absence means the last page.  **Permission:** &#x60;passport:read&#x60;.
   * @param en18222ProductIdQuery  (required)
   * @param limit Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one. (optional, default to 100)
   * @param cursor The continuation from a previous page&#39;s &#x60;Link: rel&#x3D;\&quot;next\&quot;&#x60; header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page. (optional)
   * @return ApiResponse&lt;List&lt;URI&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<URI>> readDPPIdsByProductIdsWithHttpInfo(En18222ProductIdQuery en18222ProductIdQuery, Integer limit, String cursor) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = readDPPIdsByProductIdsRequestBuilder(en18222ProductIdQuery, limit, cursor);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("readDPPIdsByProductIds", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<List<URI>>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<List<URI>>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<List<URI>>() {})
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

  private HttpRequest.Builder readDPPIdsByProductIdsRequestBuilder(En18222ProductIdQuery en18222ProductIdQuery, Integer limit, String cursor) throws ApiException {
    // verify the required parameter 'en18222ProductIdQuery' is set
    if (en18222ProductIdQuery == null) {
      throw new ApiException(400, "Missing the required parameter 'en18222ProductIdQuery' when calling readDPPIdsByProductIds");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dppsByProductIds";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
    localVarQueryParameterBaseName = "cursor";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("cursor", cursor));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(en18222ProductIdQuery);
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
   * ReadDPPVersionByIdAndDate — return the DPP version current at a given date
   * **EN 18222:2026 clause 4.4, Table 3; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main provider and by a back-up provider — expressly not by the creator. Table 3 notes the method is optional for the economic operator.  The version returned is the one that was current at the instant given: the node archives a snapshot before each change, so the first snapshot recorded after the instant holds the state that was current at it.  **Permission:** &#x60;passport:read&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param date The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. &#x60;2026-09-03T12:00:00Z&#x60;. A value without a UTC designator is refused rather than read in the server&#39;s local zone. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd readDPPVersionByIdAndDate(String dppId, OffsetDateTime date, String representation) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = readDPPVersionByIdAndDateWithHttpInfo(dppId, date, representation);
    return localVarResponse.getData();
  }

  /**
   * ReadDPPVersionByIdAndDate — return the DPP version current at a given date
   * **EN 18222:2026 clause 4.4, Table 3; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main provider and by a back-up provider — expressly not by the creator. Table 3 notes the method is optional for the economic operator.  The version returned is the one that was current at the instant given: the node archives a snapshot before each change, so the first snapshot recorded after the instant holds the state that was current at it.  **Permission:** &#x60;passport:read&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param date The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. &#x60;2026-09-03T12:00:00Z&#x60;. A value without a UTC designator is refused rather than read in the server&#39;s local zone. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> readDPPVersionByIdAndDateWithHttpInfo(String dppId, OffsetDateTime date, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = readDPPVersionByIdAndDateRequestBuilder(dppId, date, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("readDPPVersionByIdAndDate", localVarResponse);
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

  private HttpRequest.Builder readDPPVersionByIdAndDateRequestBuilder(String dppId, OffsetDateTime date, String representation) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling readDPPVersionByIdAndDate");
    }
    // verify the required parameter 'date' is set
    if (date == null) {
      throw new ApiException(400, "Missing the required parameter 'date' when calling readDPPVersionByIdAndDate");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dppsByIdAndDate/{dppId}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "date";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("date", date));
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

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
   * ReadDataElement — return one data element by its absolute path
   * **EN 18222:2026 clause 6.2, Table 9; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available by the creator or their main provider, as well as by a back-up provider. Clause 6.1 itself is permissive (\&quot;may be made available\&quot;), so the obligation is the one clause 4.1 states.  The element is addressed within the passport document in the requested representation, so the access tier that governs &#x60;ReadDPPById&#x60; governs which elements are addressable here too.  **Permission:** &#x60;passport:read&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param elementIdPath The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier &#x60;$&#x60;, name selectors in both spellings (&#x60;$.name&#x60;, &#x60;$[&#39;name&#39;]&#x60;, &#x60;$[\&quot;name\&quot;]&#x60;) and the index selector including RFC 9535&#39;s negative form (&#x60;$.materials[0]&#x60;, &#x60;$.materials[-1]&#x60;). The wildcard, descendant (&#x60;..&#x60;), slice and filter selectors, and a comma union of several selectors, are **refused with &#x60;400&#x60;** naming the construct used. EN 18222 Table 9 and Table 10 both return a single &#x60;DataElement&#x60;, and RFC 9535&#39;s general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object readDataElement(String dppId, String elementIdPath, String representation) throws ApiException {
    ApiResponse<Object> localVarResponse = readDataElementWithHttpInfo(dppId, elementIdPath, representation);
    return localVarResponse.getData();
  }

  /**
   * ReadDataElement — return one data element by its absolute path
   * **EN 18222:2026 clause 6.2, Table 9; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available by the creator or their main provider, as well as by a back-up provider. Clause 6.1 itself is permissive (\&quot;may be made available\&quot;), so the obligation is the one clause 4.1 states.  The element is addressed within the passport document in the requested representation, so the access tier that governs &#x60;ReadDPPById&#x60; governs which elements are addressable here too.  **Permission:** &#x60;passport:read&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param elementIdPath The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier &#x60;$&#x60;, name selectors in both spellings (&#x60;$.name&#x60;, &#x60;$[&#39;name&#39;]&#x60;, &#x60;$[\&quot;name\&quot;]&#x60;) and the index selector including RFC 9535&#39;s negative form (&#x60;$.materials[0]&#x60;, &#x60;$.materials[-1]&#x60;). The wildcard, descendant (&#x60;..&#x60;), slice and filter selectors, and a comma union of several selectors, are **refused with &#x60;400&#x60;** naming the construct used. EN 18222 Table 9 and Table 10 both return a single &#x60;DataElement&#x60;, and RFC 9535&#39;s general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Object> readDataElementWithHttpInfo(String dppId, String elementIdPath, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = readDataElementRequestBuilder(dppId, elementIdPath, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("readDataElement", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<Object>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<Object>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<Object>() {})
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

  private HttpRequest.Builder readDataElementRequestBuilder(String dppId, String elementIdPath, String representation) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling readDataElement");
    }
    // verify the required parameter 'elementIdPath' is set
    if (elementIdPath == null) {
      throw new ApiException(400, "Missing the required parameter 'elementIdPath' when calling readDataElement");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps/{dppId}/elements/{elementIdPath}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()))
        .replace("{elementIdPath}", ApiClient.urlEncode(elementIdPath.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Accept", "application/json");

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
   * UpdateDPPById — partial update of a DPP with a specified DPP ID
   * **EN 18222:2026 clause 4.7, Table 6; bound by clause 8.2, Table 16.** Clause 4.1 makes this a **shall** for the creator or their main provider *if authorized third parties have access rights to update parts of the DPP*.  The body is a &#x60;partialDPP&#x60; — \&quot;only contains data that need to be updated or are extended with new data\&quot;. It is applied as an RFC 7396 JSON Merge Patch, which clause 8.1 names for update operations: a member set to &#x60;null&#x60; is removed, an object member merges recursively, anything else replaces. Data elements may be named directly (they are the passport&#39;s own product data) or wrapped in &#x60;metadata&#x60;.  Clause 4.7 requires that \&quot;if the update of some parts fails the complete update process will fail and there should be no changes adopted in the DPP\&quot; — the merge is computed in full and written in one transaction, so a rejected member changes nothing. It also requires that \&quot;all changes to the digital product passport shall be archived in accordance with EN 18221:2026\&quot;: the write goes through this node&#39;s single passport-mutation seam, which snapshots the passport as it was and chains a content hash over the previous version.  Members this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status and the embedded operator and facility nodes — are refused with &#x60;403&#x60; rather than silently ignored.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd updateDPPById(String dppId, Object body, String representation) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = updateDPPByIdWithHttpInfo(dppId, body, representation);
    return localVarResponse.getData();
  }

  /**
   * UpdateDPPById — partial update of a DPP with a specified DPP ID
   * **EN 18222:2026 clause 4.7, Table 6; bound by clause 8.2, Table 16.** Clause 4.1 makes this a **shall** for the creator or their main provider *if authorized third parties have access rights to update parts of the DPP*.  The body is a &#x60;partialDPP&#x60; — \&quot;only contains data that need to be updated or are extended with new data\&quot;. It is applied as an RFC 7396 JSON Merge Patch, which clause 8.1 names for update operations: a member set to &#x60;null&#x60; is removed, an object member merges recursively, anything else replaces. Data elements may be named directly (they are the passport&#39;s own product data) or wrapped in &#x60;metadata&#x60;.  Clause 4.7 requires that \&quot;if the update of some parts fails the complete update process will fail and there should be no changes adopted in the DPP\&quot; — the merge is computed in full and written in one transaction, so a rejected member changes nothing. It also requires that \&quot;all changes to the digital product passport shall be archived in accordance with EN 18221:2026\&quot;: the write goes through this node&#39;s single passport-mutation seam, which snapshots the passport as it was and chains a content hash over the previous version.  Members this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status and the embedded operator and facility nodes — are refused with &#x60;403&#x60; rather than silently ignored.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 — &#x60;compressed&#x60; (the default, EN 18223 clause 5.2) or &#x60;full&#x60; (EN 18223 Annex A). &#x60;expanded&#x60; is accepted as an alias of &#x60;full&#x60;, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded. (optional)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> updateDPPByIdWithHttpInfo(String dppId, Object body, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updateDPPByIdRequestBuilder(dppId, body, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updateDPPById", localVarResponse);
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

  private HttpRequest.Builder updateDPPByIdRequestBuilder(String dppId, Object body, String representation) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling updateDPPById");
    }
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateDPPById");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps/{dppId}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(body);
      localVarRequestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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
   * UpdateDataElement — update, amend or remove one data element
   * **EN 18222:2026 clause 6.3, Table 10; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available if authorized third parties have access rights to update parts of the DPP.  Clause 6.3: the method \&quot;also allows to amend or remove data element information\&quot;. So a path whose parent exists but whose member does not is **created**, and an explicit &#x60;null&#x60; body **removes** the element — the RFC 7396 convention clause 8.1 points at. A removal has nothing to return and answers &#x60;204&#x60; (EN 18222 Table 15 &#x60;SuccessNoContent&#x60;); any other update answers &#x60;200&#x60; with the changed element (Table 10).  Elements this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status, the embedded operator and facility nodes — are refused with &#x60;403&#x60;: they are published in the document but are computed from the record and its seal, not written by the operator.  The write goes through the same passport-mutation seam as every other, so the EN 18221 archiving duty is discharged identically.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param elementIdPath The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier &#x60;$&#x60;, name selectors in both spellings (&#x60;$.name&#x60;, &#x60;$[&#39;name&#39;]&#x60;, &#x60;$[\&quot;name\&quot;]&#x60;) and the index selector including RFC 9535&#39;s negative form (&#x60;$.materials[0]&#x60;, &#x60;$.materials[-1]&#x60;). The wildcard, descendant (&#x60;..&#x60;), slice and filter selectors, and a comma union of several selectors, are **refused with &#x60;400&#x60;** naming the construct used. EN 18222 Table 9 and Table 10 both return a single &#x60;DataElement&#x60;, and RFC 9535&#39;s general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside &#x60;compressed&#x60; / &#x60;full&#x60; / &#x60;expanded&#x60; is nonetheless refused with &#x60;400&#x60;: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not. (optional)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object updateDataElement(String dppId, String elementIdPath, Object body, String representation) throws ApiException {
    ApiResponse<Object> localVarResponse = updateDataElementWithHttpInfo(dppId, elementIdPath, body, representation);
    return localVarResponse.getData();
  }

  /**
   * UpdateDataElement — update, amend or remove one data element
   * **EN 18222:2026 clause 6.3, Table 10; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available if authorized third parties have access rights to update parts of the DPP.  Clause 6.3: the method \&quot;also allows to amend or remove data element information\&quot;. So a path whose parent exists but whose member does not is **created**, and an explicit &#x60;null&#x60; body **removes** the element — the RFC 7396 convention clause 8.1 points at. A removal has nothing to return and answers &#x60;204&#x60; (EN 18222 Table 15 &#x60;SuccessNoContent&#x60;); any other update answers &#x60;200&#x60; with the changed element (Table 10).  Elements this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status, the embedded operator and facility nodes — are refused with &#x60;403&#x60;: they are published in the document but are computed from the record and its seal, not written by the operator.  The write goes through the same passport-mutation seam as every other, so the EN 18221 archiving duty is discharged identically.  **Permission:** &#x60;passport:update&#x60;.
   * @param dppId The passport&#39;s &#x60;digitalProductPassportId&#x60; (EN 18223 clause 4.1.2.1) — either this node&#39;s resolvable instance URI &#x60;{base}/passport/{id}&#x60;, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, &#x60;/v1/dppsByProductId/{productId}&#x60;. (required)
   * @param elementIdPath The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier &#x60;$&#x60;, name selectors in both spellings (&#x60;$.name&#x60;, &#x60;$[&#39;name&#39;]&#x60;, &#x60;$[\&quot;name\&quot;]&#x60;) and the index selector including RFC 9535&#39;s negative form (&#x60;$.materials[0]&#x60;, &#x60;$.materials[-1]&#x60;). The wildcard, descendant (&#x60;..&#x60;), slice and filter selectors, and a comma union of several selectors, are **refused with &#x60;400&#x60;** naming the construct used. EN 18222 Table 9 and Table 10 both return a single &#x60;DataElement&#x60;, and RFC 9535&#39;s general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
   * @param body  (required)
   * @param representation EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside &#x60;compressed&#x60; / &#x60;full&#x60; / &#x60;expanded&#x60; is nonetheless refused with &#x60;400&#x60;: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not. (optional)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Object> updateDataElementWithHttpInfo(String dppId, String elementIdPath, Object body, String representation) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updateDataElementRequestBuilder(dppId, elementIdPath, body, representation);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updateDataElement", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<Object>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<Object>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<Object>() {})
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

  private HttpRequest.Builder updateDataElementRequestBuilder(String dppId, String elementIdPath, Object body, String representation) throws ApiException {
    // verify the required parameter 'dppId' is set
    if (dppId == null) {
      throw new ApiException(400, "Missing the required parameter 'dppId' when calling updateDataElement");
    }
    // verify the required parameter 'elementIdPath' is set
    if (elementIdPath == null) {
      throw new ApiException(400, "Missing the required parameter 'elementIdPath' when calling updateDataElement");
    }
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateDataElement");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/v1/dpps/{dppId}/elements/{elementIdPath}"
        .replace("{dppId}", ApiClient.urlEncode(dppId.toString()))
        .replace("{elementIdPath}", ApiClient.urlEncode(elementIdPath.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "representation";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("representation", representation));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(body);
      localVarRequestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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

}
