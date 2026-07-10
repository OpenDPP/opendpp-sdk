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

import eu.opendppnode.sdk.model.ApproveGrantRequest;
import eu.opendppnode.sdk.model.CreateGrantRequest;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.GrantDecisionResponse;
import eu.opendppnode.sdk.model.GrantIssuedResponse;
import eu.opendppnode.sdk.model.GrantListResponse;
import eu.opendppnode.sdk.model.GrantRouteError;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.RevokeGrant403Response;

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
public class AccessGrantsApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public AccessGrantsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AccessGrantsApi(ApiClient apiClient) {
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
   * Approve a pending access request and mint its token
   * Approves a &#x60;PENDING&#x60; third-party access request (submitted via the hosted request-access page). Approval mints the legitimate-interest capability token **at this moment** — pending requests carry no token — sets &#x60;status: ACTIVE&#x60;, records &#x60;decidedAt&#x60;/&#x60;decidedBy&#x60;, and replaces the request&#39;s provisional 90-day expiry with the &#x60;expiresAt&#x60; you supply (required; future; max **366 days** out).  The raw token is returned **once** in this response. If the request has a &#x60;granteeEmail&#x60;, the grantee is additionally e-mailed an inspection link containing the token (&#x60;…/unit/{batteryUnitId}?grant&#x3D;dpp_li_…&#x60; or &#x60;…/passport/{passportId}?grant&#x3D;dpp_li_…&#x60;) — the only other place the raw token ever exists. The decision is audited as &#x60;grant.approved&#x60;.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The access-request (AccessGrant) id. (required)
   * @param approveGrantRequest  (required)
   * @return GrantIssuedResponse
   * @throws ApiException if fails to make API call
   */
  public GrantIssuedResponse approveGrantRequest(String id, ApproveGrantRequest approveGrantRequest) throws ApiException {
    ApiResponse<GrantIssuedResponse> localVarResponse = approveGrantRequestWithHttpInfo(id, approveGrantRequest);
    return localVarResponse.getData();
  }

  /**
   * Approve a pending access request and mint its token
   * Approves a &#x60;PENDING&#x60; third-party access request (submitted via the hosted request-access page). Approval mints the legitimate-interest capability token **at this moment** — pending requests carry no token — sets &#x60;status: ACTIVE&#x60;, records &#x60;decidedAt&#x60;/&#x60;decidedBy&#x60;, and replaces the request&#39;s provisional 90-day expiry with the &#x60;expiresAt&#x60; you supply (required; future; max **366 days** out).  The raw token is returned **once** in this response. If the request has a &#x60;granteeEmail&#x60;, the grantee is additionally e-mailed an inspection link containing the token (&#x60;…/unit/{batteryUnitId}?grant&#x3D;dpp_li_…&#x60; or &#x60;…/passport/{passportId}?grant&#x3D;dpp_li_…&#x60;) — the only other place the raw token ever exists. The decision is audited as &#x60;grant.approved&#x60;.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The access-request (AccessGrant) id. (required)
   * @param approveGrantRequest  (required)
   * @return ApiResponse&lt;GrantIssuedResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GrantIssuedResponse> approveGrantRequestWithHttpInfo(String id, ApproveGrantRequest approveGrantRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = approveGrantRequestRequestBuilder(id, approveGrantRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("approveGrantRequest", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<GrantIssuedResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<GrantIssuedResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<GrantIssuedResponse>() {})
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

  private HttpRequest.Builder approveGrantRequestRequestBuilder(String id, ApproveGrantRequest approveGrantRequest) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling approveGrantRequest");
    }
    // verify the required parameter 'approveGrantRequest' is set
    if (approveGrantRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'approveGrantRequest' when calling approveGrantRequest");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/grants/{id}/approve"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(approveGrantRequest);
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
   * Issue a legitimate-interest access grant directly
   * Directly issues an &#x60;ACTIVE&#x60; legitimate-interest access grant (no pending request involved) and mints its capability token. The raw token (&#x60;dpp_li_&#x60; + 32 hex characters) is returned **once** in this response; only its SHA-256 hash is stored. The grantee presents it to the public resolution endpoints as &#x60;Authorization: Bearer dpp_li_…&#x60; or &#x60;?grant&#x3D;dpp_li_…&#x60; to unlock the restricted (tier-2 / per-unit) data of the granted scope.  **Permission:** &#x60;grant:write&#x60; (write operations are subject to subscription gating, so 402 is possible). Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header; Bearer clients are exempt. On workspaces that enforce multi-factor authentication, user sessions that did not authenticate with a second factor receive 403 on writes (API-key clients are exempt). **Rate limit:** global limiter, 100 requests/min per IP.  Scope semantics: - &#x60;UNIT&#x60; — &#x60;batteryUnitId&#x60; is required; the unit must belong to this workspace. The unit&#39;s parent &#x60;passportId&#x60; is recorded on the grant. - &#x60;PASSPORT&#x60; — &#x60;passportId&#x60; is required; the passport must belong to this workspace and must not be a &#x60;DRAFT&#x60; (drafts return 404). - &#x60;TENANT&#x60; — workspace-wide; no target id needed.  &#x60;expiresAt&#x60; is required, must be in the future, and at most **366 days** out. This endpoint always mints &#x60;kind: LEGITIMATE_INTEREST&#x60; — &#x60;AUTHORITY&#x60; (&#x60;dpp_auth_…&#x60;) grants are platform-issued only and cannot be created here. The issuance is audited as &#x60;grant.issued&#x60;.  String fields longer than their documented maximum are **silently truncated**, not rejected; unknown fields are ignored.
   * @param createGrantRequest  (required)
   * @return GrantIssuedResponse
   * @throws ApiException if fails to make API call
   */
  public GrantIssuedResponse createGrant(CreateGrantRequest createGrantRequest) throws ApiException {
    ApiResponse<GrantIssuedResponse> localVarResponse = createGrantWithHttpInfo(createGrantRequest);
    return localVarResponse.getData();
  }

  /**
   * Issue a legitimate-interest access grant directly
   * Directly issues an &#x60;ACTIVE&#x60; legitimate-interest access grant (no pending request involved) and mints its capability token. The raw token (&#x60;dpp_li_&#x60; + 32 hex characters) is returned **once** in this response; only its SHA-256 hash is stored. The grantee presents it to the public resolution endpoints as &#x60;Authorization: Bearer dpp_li_…&#x60; or &#x60;?grant&#x3D;dpp_li_…&#x60; to unlock the restricted (tier-2 / per-unit) data of the granted scope.  **Permission:** &#x60;grant:write&#x60; (write operations are subject to subscription gating, so 402 is possible). Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header; Bearer clients are exempt. On workspaces that enforce multi-factor authentication, user sessions that did not authenticate with a second factor receive 403 on writes (API-key clients are exempt). **Rate limit:** global limiter, 100 requests/min per IP.  Scope semantics: - &#x60;UNIT&#x60; — &#x60;batteryUnitId&#x60; is required; the unit must belong to this workspace. The unit&#39;s parent &#x60;passportId&#x60; is recorded on the grant. - &#x60;PASSPORT&#x60; — &#x60;passportId&#x60; is required; the passport must belong to this workspace and must not be a &#x60;DRAFT&#x60; (drafts return 404). - &#x60;TENANT&#x60; — workspace-wide; no target id needed.  &#x60;expiresAt&#x60; is required, must be in the future, and at most **366 days** out. This endpoint always mints &#x60;kind: LEGITIMATE_INTEREST&#x60; — &#x60;AUTHORITY&#x60; (&#x60;dpp_auth_…&#x60;) grants are platform-issued only and cannot be created here. The issuance is audited as &#x60;grant.issued&#x60;.  String fields longer than their documented maximum are **silently truncated**, not rejected; unknown fields are ignored.
   * @param createGrantRequest  (required)
   * @return ApiResponse&lt;GrantIssuedResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GrantIssuedResponse> createGrantWithHttpInfo(CreateGrantRequest createGrantRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = createGrantRequestBuilder(createGrantRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("createGrant", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<GrantIssuedResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<GrantIssuedResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<GrantIssuedResponse>() {})
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

  private HttpRequest.Builder createGrantRequestBuilder(CreateGrantRequest createGrantRequest) throws ApiException {
    // verify the required parameter 'createGrantRequest' is set
    if (createGrantRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createGrantRequest' when calling createGrant");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/grants";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(createGrantRequest);
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
   * Deny a pending access request
   * Denies a &#x60;PENDING&#x60; third-party access request: sets &#x60;status: DENIED&#x60; and records &#x60;decidedAt&#x60;/&#x60;decidedBy&#x60;. No token is ever minted for a denied request, and no e-mail is sent to the requester. The decision is audited as &#x60;grant.denied&#x60;. The request body, if any, is ignored.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The access-request (AccessGrant) id. (required)
   * @return GrantDecisionResponse
   * @throws ApiException if fails to make API call
   */
  public GrantDecisionResponse denyGrantRequest(String id) throws ApiException {
    ApiResponse<GrantDecisionResponse> localVarResponse = denyGrantRequestWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Deny a pending access request
   * Denies a &#x60;PENDING&#x60; third-party access request: sets &#x60;status: DENIED&#x60; and records &#x60;decidedAt&#x60;/&#x60;decidedBy&#x60;. No token is ever minted for a denied request, and no e-mail is sent to the requester. The decision is audited as &#x60;grant.denied&#x60;. The request body, if any, is ignored.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The access-request (AccessGrant) id. (required)
   * @return ApiResponse&lt;GrantDecisionResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GrantDecisionResponse> denyGrantRequestWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = denyGrantRequestRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("denyGrantRequest", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<GrantDecisionResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<GrantDecisionResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<GrantDecisionResponse>() {})
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

  private HttpRequest.Builder denyGrantRequestRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling denyGrantRequest");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/grants/{id}/deny"
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
   * List access grants and pending access requests
   * Lists the workspace&#39;s access grants — capability-token grants for the Battery Regulation&#39;s restricted data tiers (Reg. (EU) 2023/1542 Art. 77(9), Annex XIII(2)–(4)) — including undecided third-party access **requests** (&#x60;status: PENDING&#x60;, &#x60;issuerType: REQUEST&#x60;) submitted via the hosted request-access page.  **Permission:** &#x60;grant:read&#x60;. **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  Paginated with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200), grouped by &#x60;status&#x60; ascending (alphabetical: &#x60;ACTIVE&#x60;, &#x60;DENIED&#x60;, &#x60;PENDING&#x60;, &#x60;REVOKED&#x60;) and newest-first within each group. &#x60;AUTHORITY&#x60; grants (platform-issued market-surveillance access) are listed for transparency but are not tenant-revocable (&#x60;revocable: false&#x60;). Raw capability tokens are never included — only issuance/approval responses contain them, once.  **Pagination:** results are paged with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200). The response now also carries &#x60;success&#x60;, &#x60;count&#x60;, &#x60;total&#x60; and &#x60;totalPages&#x60; alongside &#x60;grants&#x60;.
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return GrantListResponse
   * @throws ApiException if fails to make API call
   */
  public GrantListResponse listGrants(Integer page, Integer limit) throws ApiException {
    ApiResponse<GrantListResponse> localVarResponse = listGrantsWithHttpInfo(page, limit);
    return localVarResponse.getData();
  }

  /**
   * List access grants and pending access requests
   * Lists the workspace&#39;s access grants — capability-token grants for the Battery Regulation&#39;s restricted data tiers (Reg. (EU) 2023/1542 Art. 77(9), Annex XIII(2)–(4)) — including undecided third-party access **requests** (&#x60;status: PENDING&#x60;, &#x60;issuerType: REQUEST&#x60;) submitted via the hosted request-access page.  **Permission:** &#x60;grant:read&#x60;. **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  Paginated with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200), grouped by &#x60;status&#x60; ascending (alphabetical: &#x60;ACTIVE&#x60;, &#x60;DENIED&#x60;, &#x60;PENDING&#x60;, &#x60;REVOKED&#x60;) and newest-first within each group. &#x60;AUTHORITY&#x60; grants (platform-issued market-surveillance access) are listed for transparency but are not tenant-revocable (&#x60;revocable: false&#x60;). Raw capability tokens are never included — only issuance/approval responses contain them, once.  **Pagination:** results are paged with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200). The response now also carries &#x60;success&#x60;, &#x60;count&#x60;, &#x60;total&#x60; and &#x60;totalPages&#x60; alongside &#x60;grants&#x60;.
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return ApiResponse&lt;GrantListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GrantListResponse> listGrantsWithHttpInfo(Integer page, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listGrantsRequestBuilder(page, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listGrants", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<GrantListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<GrantListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<GrantListResponse>() {})
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

  private HttpRequest.Builder listGrantsRequestBuilder(Integer page, Integer limit) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/grants";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "page";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("page", page));
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));

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
   * Revoke an access grant (soft revocation)
   * Soft-revokes a grant: sets &#x60;status: REVOKED&#x60; and &#x60;revokedAt&#x60; (the row is retained for audit; the public resolvers reject the token from then on). Audited as &#x60;grant.revoked&#x60;.  Behavioral caveats (no status precondition — only the kind is checked): - Works on a grant in **any** status: revoking a &#x60;PENDING&#x60; request withdraws it; revoking a &#x60;DENIED&#x60; grant flips it to &#x60;REVOKED&#x60;. - Re-revoking an already-&#x60;REVOKED&#x60; grant returns 200 again and preserves the original &#x60;revokedAt&#x60;. - &#x60;AUTHORITY&#x60; grants (&#x60;kind: AUTHORITY&#x60;, platform-issued market-surveillance access) are **not tenant-revocable** — 403. Battery Reg. Art. 77 market-surveillance access must not depend on manufacturer consent; platform admins manage those.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The grant (AccessGrant) id. (required)
   * @return GrantDecisionResponse
   * @throws ApiException if fails to make API call
   */
  public GrantDecisionResponse revokeGrant(String id) throws ApiException {
    ApiResponse<GrantDecisionResponse> localVarResponse = revokeGrantWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Revoke an access grant (soft revocation)
   * Soft-revokes a grant: sets &#x60;status: REVOKED&#x60; and &#x60;revokedAt&#x60; (the row is retained for audit; the public resolvers reject the token from then on). Audited as &#x60;grant.revoked&#x60;.  Behavioral caveats (no status precondition — only the kind is checked): - Works on a grant in **any** status: revoking a &#x60;PENDING&#x60; request withdraws it; revoking a &#x60;DENIED&#x60; grant flips it to &#x60;REVOKED&#x60;. - Re-revoking an already-&#x60;REVOKED&#x60; grant returns 200 again and preserves the original &#x60;revokedAt&#x60;. - &#x60;AUTHORITY&#x60; grants (&#x60;kind: AUTHORITY&#x60;, platform-issued market-surveillance access) are **not tenant-revocable** — 403. Battery Reg. Art. 77 market-surveillance access must not depend on manufacturer consent; platform admins manage those.  **Permission:** &#x60;grant:write&#x60; (subscription gating ⇒ 402 possible; cookie sessions need &#x60;X-CSRF-Token&#x60;; on workspaces enforcing multi-factor authentication, user sessions without a second factor get 403 — API-key clients exempt). **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id The grant (AccessGrant) id. (required)
   * @return ApiResponse&lt;GrantDecisionResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GrantDecisionResponse> revokeGrantWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = revokeGrantRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("revokeGrant", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<GrantDecisionResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<GrantDecisionResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<GrantDecisionResponse>() {})
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

  private HttpRequest.Builder revokeGrantRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling revokeGrant");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/grants/{id}"
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

}
