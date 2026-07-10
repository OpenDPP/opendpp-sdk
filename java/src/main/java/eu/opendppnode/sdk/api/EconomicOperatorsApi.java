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

import eu.opendppnode.sdk.model.DeleteOperatorResponse;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.OperatorGetResponse;
import eu.opendppnode.sdk.model.OperatorListResponse;
import eu.opendppnode.sdk.model.OperatorMinimalError;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.RegisterOperator400Response;
import eu.opendppnode.sdk.model.RegisterOperatorRequest;
import eu.opendppnode.sdk.model.RegisterOperatorResponse;
import eu.opendppnode.sdk.model.RestoreOperatorResponse;
import eu.opendppnode.sdk.model.UpdateOperatorRequest;
import eu.opendppnode.sdk.model.UpdateOperatorResponse;

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
public class EconomicOperatorsApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public EconomicOperatorsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EconomicOperatorsApi(ApiClient apiClient) {
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
   * Remove an operator (archives if it has passports, else hard-deletes)
   * Removes an operator, choosing automatically between two outcomes (ESPR Art. 9(2)/77 passport-persistence compliance — an operator that still has passports must never be hard-deleted):  - **Archive (soft delete)** — if the operator has one or more passports, it is archived instead of deleted: &#x60;archivedAt&#x60; is set on the operator and every active passport of the operator is archived with a &#x60;retentionUntil&#x60; deadline set to a platform-configured retention period from now (default 15 years). Archived passports remain **publicly resolvable** (the persistence duty) but are excluded from active management lists. Response: &#x60;{success: true, archived: true, archivedPassports: &lt;n&gt;}&#x60;. Fully reversible via &#x60;POST /api/v1/operators/{id}/restore&#x60;. - **Hard delete** — if the operator has no passports it is permanently deleted (tenant bindings cascade-delete; user/facility/API-key references are set to null). Response: &#x60;{success: true, archived: false}&#x60; — no &#x60;archivedPassports&#x60; field. - **Fallback** — if the hard delete fails on a residual foreign-key reference, the operator is archived instead and the response is &#x60;{success: true, archived: true}&#x60; **without** &#x60;archivedPassports&#x60;. If even the fallback archive fails, &#x60;409&#x60; is returned.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt. The operator must be bound to your workspace (&#x60;404&#x60; otherwise).  **Tenant-scoped:** this affects only **your** workspace&#39;s operator and passports — operators are not shared across workspaces.  Side effects: an &#x60;operator.archived&#x60; or &#x60;operator.deleted&#x60; audit event plus an in-app notification — on the primary archive and hard-delete paths only; the foreign-key fallback archive writes **no** audit event or notification. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return DeleteOperatorResponse
   * @throws ApiException if fails to make API call
   */
  public DeleteOperatorResponse deleteOperator(String id) throws ApiException {
    ApiResponse<DeleteOperatorResponse> localVarResponse = deleteOperatorWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Remove an operator (archives if it has passports, else hard-deletes)
   * Removes an operator, choosing automatically between two outcomes (ESPR Art. 9(2)/77 passport-persistence compliance — an operator that still has passports must never be hard-deleted):  - **Archive (soft delete)** — if the operator has one or more passports, it is archived instead of deleted: &#x60;archivedAt&#x60; is set on the operator and every active passport of the operator is archived with a &#x60;retentionUntil&#x60; deadline set to a platform-configured retention period from now (default 15 years). Archived passports remain **publicly resolvable** (the persistence duty) but are excluded from active management lists. Response: &#x60;{success: true, archived: true, archivedPassports: &lt;n&gt;}&#x60;. Fully reversible via &#x60;POST /api/v1/operators/{id}/restore&#x60;. - **Hard delete** — if the operator has no passports it is permanently deleted (tenant bindings cascade-delete; user/facility/API-key references are set to null). Response: &#x60;{success: true, archived: false}&#x60; — no &#x60;archivedPassports&#x60; field. - **Fallback** — if the hard delete fails on a residual foreign-key reference, the operator is archived instead and the response is &#x60;{success: true, archived: true}&#x60; **without** &#x60;archivedPassports&#x60;. If even the fallback archive fails, &#x60;409&#x60; is returned.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt. The operator must be bound to your workspace (&#x60;404&#x60; otherwise).  **Tenant-scoped:** this affects only **your** workspace&#39;s operator and passports — operators are not shared across workspaces.  Side effects: an &#x60;operator.archived&#x60; or &#x60;operator.deleted&#x60; audit event plus an in-app notification — on the primary archive and hard-delete paths only; the foreign-key fallback archive writes **no** audit event or notification. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return ApiResponse&lt;DeleteOperatorResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DeleteOperatorResponse> deleteOperatorWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteOperatorRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteOperator", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DeleteOperatorResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DeleteOperatorResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DeleteOperatorResponse>() {})
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

  private HttpRequest.Builder deleteOperatorRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteOperator");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

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
   * Fetch a single bound economic operator
   * Fetches one economic operator by UUID, scoped to your workspace (&#x60;404&#x60; if no operator with that id exists in your workspace).  **Permission:** &#x60;operator:read&#x60;. An **operator-scoped API key** may only fetch its own operator (&#x60;403&#x60; otherwise).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return OperatorGetResponse
   * @throws ApiException if fails to make API call
   */
  public OperatorGetResponse getOperator(String id) throws ApiException {
    ApiResponse<OperatorGetResponse> localVarResponse = getOperatorWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Fetch a single bound economic operator
   * Fetches one economic operator by UUID, scoped to your workspace (&#x60;404&#x60; if no operator with that id exists in your workspace).  **Permission:** &#x60;operator:read&#x60;. An **operator-scoped API key** may only fetch its own operator (&#x60;403&#x60; otherwise).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return ApiResponse&lt;OperatorGetResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OperatorGetResponse> getOperatorWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getOperatorRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getOperator", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<OperatorGetResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<OperatorGetResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<OperatorGetResponse>() {})
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

  private HttpRequest.Builder getOperatorRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getOperator");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
   * List economic operators bound to your workspace
   * Returns the economic operators bound to your workspace, ordered by name. Active operators only unless &#x60;?archived&#x3D;true&#x60; is passed (archived operators are off-boarded but their passports are retained and still publicly resolvable).  Each entry is the same &#x60;OperatorRow&#x60; shape returned by &#x60;POST&#x60;/&#x60;PATCH /api/v1/operators&#x60; — use the &#x60;id&#x60; to attribute a passport (&#x60;operatorId&#x60; on &#x60;POST /api/v1/passports&#x60;) or to address &#x60;PATCH&#x60;/&#x60;DELETE&#x60;.  **Permission:** &#x60;operator:read&#x60;. Requests authenticated with an **operator-scoped API key** see only their own operator.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param archived Set &#x60;true&#x60; to include archived (off-boarded) operators. Default returns active operators only. (optional)
   * @return OperatorListResponse
   * @throws ApiException if fails to make API call
   */
  public OperatorListResponse listOperators(String archived) throws ApiException {
    ApiResponse<OperatorListResponse> localVarResponse = listOperatorsWithHttpInfo(archived);
    return localVarResponse.getData();
  }

  /**
   * List economic operators bound to your workspace
   * Returns the economic operators bound to your workspace, ordered by name. Active operators only unless &#x60;?archived&#x3D;true&#x60; is passed (archived operators are off-boarded but their passports are retained and still publicly resolvable).  Each entry is the same &#x60;OperatorRow&#x60; shape returned by &#x60;POST&#x60;/&#x60;PATCH /api/v1/operators&#x60; — use the &#x60;id&#x60; to attribute a passport (&#x60;operatorId&#x60; on &#x60;POST /api/v1/passports&#x60;) or to address &#x60;PATCH&#x60;/&#x60;DELETE&#x60;.  **Permission:** &#x60;operator:read&#x60;. Requests authenticated with an **operator-scoped API key** see only their own operator.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param archived Set &#x60;true&#x60; to include archived (off-boarded) operators. Default returns active operators only. (optional)
   * @return ApiResponse&lt;OperatorListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OperatorListResponse> listOperatorsWithHttpInfo(String archived) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listOperatorsRequestBuilder(archived);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listOperators", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<OperatorListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<OperatorListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<OperatorListResponse>() {})
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

  private HttpRequest.Builder listOperatorsRequestBuilder(String archived) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "archived";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("archived", archived));

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
   * Register an economic operator and bind it to your workspace
   * Registers an economic operator (manufacturer, importer, supplier, …) and binds it to your workspace.  **Permission:** &#x60;operator:create&#x60;. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit); Bearer clients (API key / JWT) are exempt.  **Deduplication (per workspace):** operators are scoped to your workspace — &#x60;regId&#x60; is unique *within* your workspace, not across the platform. If **your workspace** already has an operator with the submitted &#x60;regId&#x60;, that existing record is returned and the submitted &#x60;name&#x60;, &#x60;role&#x60; and &#x60;regIdScheme&#x60; are **ignored**. A &#x60;regId&#x60; already used by *another* workspace is irrelevant — you always get **your own** operator row (so one workspace can never bind to, rename, or archive another&#39;s operator). The call is idempotent: re-registering an already-bound operator succeeds with &#x60;201&#x60; again. The per-workspace match includes **archived** operators: if your workspace&#39;s operator for that &#x60;regId&#x60; is archived, the archived record is returned as-is (&#x60;archivedAt&#x60; non-null) with &#x60;201&#x60; — registration does not un-archive it; use &#x60;POST /api/v1/operators/{id}/restore&#x60; to reactivate it.  **Registration-id integrity:** fabricated &#x60;EORI-MOCK…&#x60; ids are rejected on every path. When &#x60;regIdScheme&#x60; is &#x60;EORI&#x60;, &#x60;regId&#x60; must match &#x60;^[A-Z]{2}[A-Za-z0-9]{1,15}$&#x60; (2-letter ISO 3166 country prefix followed by up to 15 alphanumerics, e.g. &#x60;DE1234567890&#x60;). Validation is syntax-only by default. When the node operator enables the OPT-IN &#x60;EORI_EXISTENCE_CHECK&#x60; (#404), a declared &#x60;EORI&#x60; &#x60;regId&#x60; is additionally checked for EXISTENCE against the EU Commission EOS validation service and, if not found, a NON-BLOCKING advisory is added to the 201 &#x60;warnings[]&#x60; — the operator is still registered (best-effort, fail-open: a network error, or a freshly-issued / GB EORI, never blocks registration). With the check off, &#x60;warnings&#x60; is &#x60;[]&#x60;.  Side effects: an &#x60;operator.created&#x60; audit event and an in-app notification are recorded.  **Rate limit:** global limiter, 100 requests/min/IP (429 carries &#x60;x-ratelimit-*&#x60; headers).
   * @param registerOperatorRequest  (required)
   * @return RegisterOperatorResponse
   * @throws ApiException if fails to make API call
   */
  public RegisterOperatorResponse registerOperator(RegisterOperatorRequest registerOperatorRequest) throws ApiException {
    ApiResponse<RegisterOperatorResponse> localVarResponse = registerOperatorWithHttpInfo(registerOperatorRequest);
    return localVarResponse.getData();
  }

  /**
   * Register an economic operator and bind it to your workspace
   * Registers an economic operator (manufacturer, importer, supplier, …) and binds it to your workspace.  **Permission:** &#x60;operator:create&#x60;. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit); Bearer clients (API key / JWT) are exempt.  **Deduplication (per workspace):** operators are scoped to your workspace — &#x60;regId&#x60; is unique *within* your workspace, not across the platform. If **your workspace** already has an operator with the submitted &#x60;regId&#x60;, that existing record is returned and the submitted &#x60;name&#x60;, &#x60;role&#x60; and &#x60;regIdScheme&#x60; are **ignored**. A &#x60;regId&#x60; already used by *another* workspace is irrelevant — you always get **your own** operator row (so one workspace can never bind to, rename, or archive another&#39;s operator). The call is idempotent: re-registering an already-bound operator succeeds with &#x60;201&#x60; again. The per-workspace match includes **archived** operators: if your workspace&#39;s operator for that &#x60;regId&#x60; is archived, the archived record is returned as-is (&#x60;archivedAt&#x60; non-null) with &#x60;201&#x60; — registration does not un-archive it; use &#x60;POST /api/v1/operators/{id}/restore&#x60; to reactivate it.  **Registration-id integrity:** fabricated &#x60;EORI-MOCK…&#x60; ids are rejected on every path. When &#x60;regIdScheme&#x60; is &#x60;EORI&#x60;, &#x60;regId&#x60; must match &#x60;^[A-Z]{2}[A-Za-z0-9]{1,15}$&#x60; (2-letter ISO 3166 country prefix followed by up to 15 alphanumerics, e.g. &#x60;DE1234567890&#x60;). Validation is syntax-only by default. When the node operator enables the OPT-IN &#x60;EORI_EXISTENCE_CHECK&#x60; (#404), a declared &#x60;EORI&#x60; &#x60;regId&#x60; is additionally checked for EXISTENCE against the EU Commission EOS validation service and, if not found, a NON-BLOCKING advisory is added to the 201 &#x60;warnings[]&#x60; — the operator is still registered (best-effort, fail-open: a network error, or a freshly-issued / GB EORI, never blocks registration). With the check off, &#x60;warnings&#x60; is &#x60;[]&#x60;.  Side effects: an &#x60;operator.created&#x60; audit event and an in-app notification are recorded.  **Rate limit:** global limiter, 100 requests/min/IP (429 carries &#x60;x-ratelimit-*&#x60; headers).
   * @param registerOperatorRequest  (required)
   * @return ApiResponse&lt;RegisterOperatorResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegisterOperatorResponse> registerOperatorWithHttpInfo(RegisterOperatorRequest registerOperatorRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = registerOperatorRequestBuilder(registerOperatorRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("registerOperator", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<RegisterOperatorResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<RegisterOperatorResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<RegisterOperatorResponse>() {})
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

  private HttpRequest.Builder registerOperatorRequestBuilder(RegisterOperatorRequest registerOperatorRequest) throws ApiException {
    // verify the required parameter 'registerOperatorRequest' is set
    if (registerOperatorRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'registerOperatorRequest' when calling registerOperator");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(registerOperatorRequest);
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
   * Restore an archived operator and its archived passports
   * Un-archives an operator that was soft-deleted by &#x60;DELETE /api/v1/operators/{id}&#x60; and brings its archived passports back into the active catalogue: clears the operator&#39;s &#x60;archivedAt&#x60;, then clears &#x60;archivedAt&#x60; and &#x60;retentionUntil&#x60; on every archived passport of the operator **except** passports that were independently &#x60;DECOMMISSIONED&#x60; (those keep their own retention clock and stay archived).  Safe to call on a non-archived operator — it simply restores any archived passports the operator may have (&#x60;restoredPassports&#x60; may be &#x60;0&#x60;). No request body.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt. &#x60;404&#x60; if the operator is not bound to your workspace.  Side effects: an &#x60;operator.restored&#x60; audit event and an in-app notification. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return RestoreOperatorResponse
   * @throws ApiException if fails to make API call
   */
  public RestoreOperatorResponse restoreOperator(String id) throws ApiException {
    ApiResponse<RestoreOperatorResponse> localVarResponse = restoreOperatorWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Restore an archived operator and its archived passports
   * Un-archives an operator that was soft-deleted by &#x60;DELETE /api/v1/operators/{id}&#x60; and brings its archived passports back into the active catalogue: clears the operator&#39;s &#x60;archivedAt&#x60;, then clears &#x60;archivedAt&#x60; and &#x60;retentionUntil&#x60; on every archived passport of the operator **except** passports that were independently &#x60;DECOMMISSIONED&#x60; (those keep their own retention clock and stay archived).  Safe to call on a non-archived operator — it simply restores any archived passports the operator may have (&#x60;restoredPassports&#x60; may be &#x60;0&#x60;). No request body.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt. &#x60;404&#x60; if the operator is not bound to your workspace.  Side effects: an &#x60;operator.restored&#x60; audit event and an in-app notification. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @return ApiResponse&lt;RestoreOperatorResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RestoreOperatorResponse> restoreOperatorWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = restoreOperatorRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("restoreOperator", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<RestoreOperatorResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<RestoreOperatorResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<RestoreOperatorResponse>() {})
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

  private HttpRequest.Builder restoreOperatorRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling restoreOperator");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators/{id}/restore"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Update an operator&#39;s name or role (regId is immutable)
   * Edits an operator bound to your workspace. Only &#x60;name&#x60; and &#x60;role&#x60; are editable; &#x60;regId&#x60; is the legal registry identifier and is **intentionally immutable** here (register the operator again under the correct id instead). Non-string or whitespace-only values are silently ignored; submitted values are trimmed. If no usable field is supplied (every field missing, non-string, or whitespace-only — including an empty object &#x60;{}&#x60; or an omitted body), the current operator row is returned unchanged with &#x60;200&#x60; and no audit event is written. The handler does not diff against current values: supplying a value identical to the current one still performs an update and writes an audit event.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt.  **Tenant-scoped:** operators are scoped to your workspace — &#x60;regId&#x60; is not globally unique. Registering a &#x60;regId&#x60; that another workspace also uses creates **your own** operator row; &#x60;name&#x60;/&#x60;role&#x60; edits never affect another workspace.  When a change is applied, an &#x60;operator.updated&#x60; audit event is recorded. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @param updateOperatorRequest  (optional)
   * @return UpdateOperatorResponse
   * @throws ApiException if fails to make API call
   */
  public UpdateOperatorResponse updateOperator(String id, UpdateOperatorRequest updateOperatorRequest) throws ApiException {
    ApiResponse<UpdateOperatorResponse> localVarResponse = updateOperatorWithHttpInfo(id, updateOperatorRequest);
    return localVarResponse.getData();
  }

  /**
   * Update an operator&#39;s name or role (regId is immutable)
   * Edits an operator bound to your workspace. Only &#x60;name&#x60; and &#x60;role&#x60; are editable; &#x60;regId&#x60; is the legal registry identifier and is **intentionally immutable** here (register the operator again under the correct id instead). Non-string or whitespace-only values are silently ignored; submitted values are trimmed. If no usable field is supplied (every field missing, non-string, or whitespace-only — including an empty object &#x60;{}&#x60; or an omitted body), the current operator row is returned unchanged with &#x60;200&#x60; and no audit event is written. The handler does not diff against current values: supplying a value identical to the current one still performs an update and writes an audit event.  **Permission:** &#x60;operator:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt.  **Tenant-scoped:** operators are scoped to your workspace — &#x60;regId&#x60; is not globally unique. Registering a &#x60;regId&#x60; that another workspace also uses creates **your own** operator row; &#x60;name&#x60;/&#x60;role&#x60; edits never affect another workspace.  When a change is applied, an &#x60;operator.updated&#x60; audit event is recorded. Unhandled database errors are normalized by the global error handler to the standard &#x60;{success: false, error, message}&#x60; envelope with a generic message (details are logged server-side).  **Rate limit:** global limiter, 100 requests/min/IP.
   * @param id Operator UUID (&#x60;EconomicOperator.id&#x60;). Must be bound to your workspace. (required)
   * @param updateOperatorRequest  (optional)
   * @return ApiResponse&lt;UpdateOperatorResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UpdateOperatorResponse> updateOperatorWithHttpInfo(String id, UpdateOperatorRequest updateOperatorRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updateOperatorRequestBuilder(id, updateOperatorRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updateOperator", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<UpdateOperatorResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<UpdateOperatorResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<UpdateOperatorResponse>() {})
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

  private HttpRequest.Builder updateOperatorRequestBuilder(String id, UpdateOperatorRequest updateOperatorRequest) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updateOperator");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/operators/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateOperatorRequest);
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
