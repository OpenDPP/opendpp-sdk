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

import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.WebhookDeliveriesResponse;
import eu.opendppnode.sdk.model.WebhookSecretRotateResponse;
import eu.opendppnode.sdk.model.WebhookSubscriptionCreateRequest;
import eu.opendppnode.sdk.model.WebhookSubscriptionCreateResponse;
import eu.opendppnode.sdk.model.WebhookSubscriptionDeleteResponse;
import eu.opendppnode.sdk.model.WebhookSubscriptionListResponse;
import eu.opendppnode.sdk.model.WebhookSubscriptionUpdateRequest;
import eu.opendppnode.sdk.model.WebhookSubscriptionUpdateResponse;
import eu.opendppnode.sdk.model.WebhookTestResult;

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
public class WebhooksApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public WebhooksApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WebhooksApi(ApiClient apiClient) {
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
   * Register a webhook subscription (signing secret returned once)
   * Registers an endpoint to receive passport lifecycle webhooks for the calling workspace.  **Permission:** &#x60;webhook:write&#x60;. Cookie-session callers must send the &#x60;X-CSRF-Token&#x60; header (double-submit); Bearer API-key/JWT clients are exempt. Write permissions are additionally gated on an active workspace subscription (&#x60;402&#x60;).  **URL validation (SSRF guard):** &#x60;url&#x60; must be an absolute &#x60;http(s)&#x60; URL. At registration the hostname is DNS-resolved and the request is rejected with &#x60;400&#x60; if any resolved A/AAAA record is loopback, RFC 1918/CGNAT private, link-local / cloud-metadata (&#x60;169.254.0.0/16&#x60;), multicast, or an equivalent IPv6 range. At delivery time the socket is pinned to the validated IP and redirects are never followed.  **Event filters:** &#x60;events&#x60; must be a non-empty array drawn from &#x60;passport.ingested&#x60;, &#x60;passport.updated&#x60;, &#x60;passport.sealed&#x60;, &#x60;passport.recalled&#x60;, &#x60;passport.status_updated&#x60;, &#x60;*&#x60;. The &#x60;*&#x60; wildcard matches every emitted event.  **Signing secret — shown once:** the &#x60;201&#x60; response contains the full subscription row **including** the HMAC-SHA256 signing secret (&#x60;whsec_&#x60; + 32 lowercase hex chars, server-generated, never client-supplied). This is the only time the secret is ever returned: the list endpoint strips it and there is no rotation or update endpoint — delete and re-create to rotate.  **Limits:** maximum **25 subscriptions per workspace** (&#x60;409 Conflict&#x60;). Global rate limit 100 requests/min/IP (&#x60;429&#x60; with &#x60;x-ratelimit-*&#x60; headers). Unknown request-body fields are ignored.
   * @param webhookSubscriptionCreateRequest  (required)
   * @return WebhookSubscriptionCreateResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookSubscriptionCreateResponse createWebhookSubscription(WebhookSubscriptionCreateRequest webhookSubscriptionCreateRequest) throws ApiException {
    ApiResponse<WebhookSubscriptionCreateResponse> localVarResponse = createWebhookSubscriptionWithHttpInfo(webhookSubscriptionCreateRequest);
    return localVarResponse.getData();
  }

  /**
   * Register a webhook subscription (signing secret returned once)
   * Registers an endpoint to receive passport lifecycle webhooks for the calling workspace.  **Permission:** &#x60;webhook:write&#x60;. Cookie-session callers must send the &#x60;X-CSRF-Token&#x60; header (double-submit); Bearer API-key/JWT clients are exempt. Write permissions are additionally gated on an active workspace subscription (&#x60;402&#x60;).  **URL validation (SSRF guard):** &#x60;url&#x60; must be an absolute &#x60;http(s)&#x60; URL. At registration the hostname is DNS-resolved and the request is rejected with &#x60;400&#x60; if any resolved A/AAAA record is loopback, RFC 1918/CGNAT private, link-local / cloud-metadata (&#x60;169.254.0.0/16&#x60;), multicast, or an equivalent IPv6 range. At delivery time the socket is pinned to the validated IP and redirects are never followed.  **Event filters:** &#x60;events&#x60; must be a non-empty array drawn from &#x60;passport.ingested&#x60;, &#x60;passport.updated&#x60;, &#x60;passport.sealed&#x60;, &#x60;passport.recalled&#x60;, &#x60;passport.status_updated&#x60;, &#x60;*&#x60;. The &#x60;*&#x60; wildcard matches every emitted event.  **Signing secret — shown once:** the &#x60;201&#x60; response contains the full subscription row **including** the HMAC-SHA256 signing secret (&#x60;whsec_&#x60; + 32 lowercase hex chars, server-generated, never client-supplied). This is the only time the secret is ever returned: the list endpoint strips it and there is no rotation or update endpoint — delete and re-create to rotate.  **Limits:** maximum **25 subscriptions per workspace** (&#x60;409 Conflict&#x60;). Global rate limit 100 requests/min/IP (&#x60;429&#x60; with &#x60;x-ratelimit-*&#x60; headers). Unknown request-body fields are ignored.
   * @param webhookSubscriptionCreateRequest  (required)
   * @return ApiResponse&lt;WebhookSubscriptionCreateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookSubscriptionCreateResponse> createWebhookSubscriptionWithHttpInfo(WebhookSubscriptionCreateRequest webhookSubscriptionCreateRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = createWebhookSubscriptionRequestBuilder(webhookSubscriptionCreateRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("createWebhookSubscription", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookSubscriptionCreateResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookSubscriptionCreateResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookSubscriptionCreateResponse>() {})
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

  private HttpRequest.Builder createWebhookSubscriptionRequestBuilder(WebhookSubscriptionCreateRequest webhookSubscriptionCreateRequest) throws ApiException {
    // verify the required parameter 'webhookSubscriptionCreateRequest' is set
    if (webhookSubscriptionCreateRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookSubscriptionCreateRequest' when calling createWebhookSubscription");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(webhookSubscriptionCreateRequest);
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
   * Delete a webhook subscription
   * Deletes a webhook subscription, stopping future deliveries to its endpoint.  **Permission:** &#x60;webhook:write&#x60; (cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated, &#x60;402&#x60;).  The lookup is tenant-scoped: an &#x60;id&#x60; that exists but belongs to another workspace returns the same &#x60;404&#x60; with message &#x60;\&quot;Webhook subscription not found under your tenant\&quot;&#x60;. Deleting and re-creating is the only way to rotate a signing secret. Global rate limit 100 requests/min/IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return WebhookSubscriptionDeleteResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookSubscriptionDeleteResponse deleteWebhookSubscription(String id) throws ApiException {
    ApiResponse<WebhookSubscriptionDeleteResponse> localVarResponse = deleteWebhookSubscriptionWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Delete a webhook subscription
   * Deletes a webhook subscription, stopping future deliveries to its endpoint.  **Permission:** &#x60;webhook:write&#x60; (cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated, &#x60;402&#x60;).  The lookup is tenant-scoped: an &#x60;id&#x60; that exists but belongs to another workspace returns the same &#x60;404&#x60; with message &#x60;\&quot;Webhook subscription not found under your tenant\&quot;&#x60;. Deleting and re-creating is the only way to rotate a signing secret. Global rate limit 100 requests/min/IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return ApiResponse&lt;WebhookSubscriptionDeleteResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookSubscriptionDeleteResponse> deleteWebhookSubscriptionWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteWebhookSubscriptionRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteWebhookSubscription", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookSubscriptionDeleteResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookSubscriptionDeleteResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookSubscriptionDeleteResponse>() {})
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

  private HttpRequest.Builder deleteWebhookSubscriptionRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteWebhookSubscription");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions/{id}"
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
   * List recent webhook delivery attempts (the outbox)
   * Returns recent delivery records (the outbox), newest first, for debugging endpoint failures. Records are **event-level** (one per emitted event, fanned out to all matching subscriptions), not per-subscription — &#x60;status&#x60; reflects the event&#39;s overall delivery state and &#x60;errorMessage&#x60; joins per-endpoint errors. Payloads are **not** included.  Filter with &#x60;?status&#x3D;PENDING|DELIVERED|FAILED&#x60; and cap with &#x60;?limit&#x3D;&#x60; (1–200, default 50; a non-numeric value falls back to the default).  **Permission:** &#x60;webhook:read&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param status Filter by delivery state. Unrecognized values are ignored (no filter applied). (optional)
   * @param limit Max records to return. (optional, default to 50)
   * @return WebhookDeliveriesResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookDeliveriesResponse listWebhookDeliveries(String status, Integer limit) throws ApiException {
    ApiResponse<WebhookDeliveriesResponse> localVarResponse = listWebhookDeliveriesWithHttpInfo(status, limit);
    return localVarResponse.getData();
  }

  /**
   * List recent webhook delivery attempts (the outbox)
   * Returns recent delivery records (the outbox), newest first, for debugging endpoint failures. Records are **event-level** (one per emitted event, fanned out to all matching subscriptions), not per-subscription — &#x60;status&#x60; reflects the event&#39;s overall delivery state and &#x60;errorMessage&#x60; joins per-endpoint errors. Payloads are **not** included.  Filter with &#x60;?status&#x3D;PENDING|DELIVERED|FAILED&#x60; and cap with &#x60;?limit&#x3D;&#x60; (1–200, default 50; a non-numeric value falls back to the default).  **Permission:** &#x60;webhook:read&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param status Filter by delivery state. Unrecognized values are ignored (no filter applied). (optional)
   * @param limit Max records to return. (optional, default to 50)
   * @return ApiResponse&lt;WebhookDeliveriesResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookDeliveriesResponse> listWebhookDeliveriesWithHttpInfo(String status, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listWebhookDeliveriesRequestBuilder(status, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listWebhookDeliveries", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookDeliveriesResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookDeliveriesResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookDeliveriesResponse>() {})
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

  private HttpRequest.Builder listWebhookDeliveriesRequestBuilder(String status, Integer limit) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/deliveries";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "status";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("status", status));
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
   * List webhook subscriptions (signing secrets stripped)
   * Lists all webhook subscriptions of the calling workspace. Unpaginated (the per-workspace cap is 25).  **Permission:** &#x60;webhook:read&#x60; (read permissions are not subscription-gated, so no &#x60;402&#x60;).  The HMAC signing &#x60;secret&#x60; is **stripped from every row** — it is returned once by the create endpoint and once by &#x60;rotate-secret&#x60;. &#x60;isActive&#x60; reflects whether the subscription receives deliveries; toggle it with &#x60;PATCH /api/v1/webhooks/subscriptions/{id}&#x60;. Global rate limit 100 requests/min/IP.
   * @return WebhookSubscriptionListResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookSubscriptionListResponse listWebhookSubscriptions() throws ApiException {
    ApiResponse<WebhookSubscriptionListResponse> localVarResponse = listWebhookSubscriptionsWithHttpInfo();
    return localVarResponse.getData();
  }

  /**
   * List webhook subscriptions (signing secrets stripped)
   * Lists all webhook subscriptions of the calling workspace. Unpaginated (the per-workspace cap is 25).  **Permission:** &#x60;webhook:read&#x60; (read permissions are not subscription-gated, so no &#x60;402&#x60;).  The HMAC signing &#x60;secret&#x60; is **stripped from every row** — it is returned once by the create endpoint and once by &#x60;rotate-secret&#x60;. &#x60;isActive&#x60; reflects whether the subscription receives deliveries; toggle it with &#x60;PATCH /api/v1/webhooks/subscriptions/{id}&#x60;. Global rate limit 100 requests/min/IP.
   * @return ApiResponse&lt;WebhookSubscriptionListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookSubscriptionListResponse> listWebhookSubscriptionsWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listWebhookSubscriptionsRequestBuilder();
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listWebhookSubscriptions", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookSubscriptionListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookSubscriptionListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookSubscriptionListResponse>() {})
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

  private HttpRequest.Builder listWebhookSubscriptionsRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions";

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
   * Rotate a webhook subscription&#39;s signing secret
   * Mints a fresh HMAC-SHA256 signing secret for the subscription and returns it **once** (the old secret stops validating immediately). Use this after a suspected secret leak, or on a rotation schedule. There is no request body.  Update your receiver to verify signatures with the new secret as soon as you rotate — deliveries in flight use whichever secret was current when signed.  **Permission:** &#x60;webhook:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return WebhookSecretRotateResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookSecretRotateResponse rotateWebhookSecret(String id) throws ApiException {
    ApiResponse<WebhookSecretRotateResponse> localVarResponse = rotateWebhookSecretWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Rotate a webhook subscription&#39;s signing secret
   * Mints a fresh HMAC-SHA256 signing secret for the subscription and returns it **once** (the old secret stops validating immediately). Use this after a suspected secret leak, or on a rotation schedule. There is no request body.  Update your receiver to verify signatures with the new secret as soon as you rotate — deliveries in flight use whichever secret was current when signed.  **Permission:** &#x60;webhook:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return ApiResponse&lt;WebhookSecretRotateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookSecretRotateResponse> rotateWebhookSecretWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = rotateWebhookSecretRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("rotateWebhookSecret", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookSecretRotateResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookSecretRotateResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookSecretRotateResponse>() {})
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

  private HttpRequest.Builder rotateWebhookSecretRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling rotateWebhookSecret");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions/{id}/rotate-secret"
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
   * Send a signed test event to a subscription
   * Delivers a single **signed sample** event to the subscription&#39;s URL right now and reports the outcome — use it to confirm your endpoint is reachable and that your signature verification works, without waiting for a real passport event. The payload is a representative public JSON-LD passport document marked &#x60;_test: true&#x60;; it is signed exactly like a production delivery (HMAC-SHA256 over &#x60;${timestamp}.${body}&#x60;). The event type is a concrete value from the subscription&#39;s filter (the &#x60;*&#x60; wildcard is skipped; defaults to &#x60;passport.sealed&#x60;).  **Permission:** &#x60;webhook:write&#x60;. **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return WebhookTestResult
   * @throws ApiException if fails to make API call
   */
  public WebhookTestResult testWebhookSubscription(String id) throws ApiException {
    ApiResponse<WebhookTestResult> localVarResponse = testWebhookSubscriptionWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Send a signed test event to a subscription
   * Delivers a single **signed sample** event to the subscription&#39;s URL right now and reports the outcome — use it to confirm your endpoint is reachable and that your signature verification works, without waiting for a real passport event. The payload is a representative public JSON-LD passport document marked &#x60;_test: true&#x60;; it is signed exactly like a production delivery (HMAC-SHA256 over &#x60;${timestamp}.${body}&#x60;). The event type is a concrete value from the subscription&#39;s filter (the &#x60;*&#x60; wildcard is skipped; defaults to &#x60;passport.sealed&#x60;).  **Permission:** &#x60;webhook:write&#x60;. **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @return ApiResponse&lt;WebhookTestResult&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookTestResult> testWebhookSubscriptionWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = testWebhookSubscriptionRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("testWebhookSubscription", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookTestResult>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookTestResult>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookTestResult>() {})
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

  private HttpRequest.Builder testWebhookSubscriptionRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling testWebhookSubscription");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions/{id}/test"
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
   * Update a webhook subscription (url / events / active)
   * Partially updates a subscription: any of &#x60;url&#x60;, &#x60;events&#x60;, &#x60;isActive&#x60; (key present &#x3D; set, omitted &#x3D; unchanged). A new &#x60;url&#x60; is re-validated by the DNS-resolving SSRF guard (same rules as creation); &#x60;events&#x60; must be a non-empty subset of the allowed filters. The signing &#x60;secret&#x60; is **not** editable here — use &#x60;rotate-secret&#x60;. An empty body returns the current subscription unchanged. The response **strips the secret**.  **Permission:** &#x60;webhook:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @param webhookSubscriptionUpdateRequest  (optional)
   * @return WebhookSubscriptionUpdateResponse
   * @throws ApiException if fails to make API call
   */
  public WebhookSubscriptionUpdateResponse updateWebhookSubscription(String id, WebhookSubscriptionUpdateRequest webhookSubscriptionUpdateRequest) throws ApiException {
    ApiResponse<WebhookSubscriptionUpdateResponse> localVarResponse = updateWebhookSubscriptionWithHttpInfo(id, webhookSubscriptionUpdateRequest);
    return localVarResponse.getData();
  }

  /**
   * Update a webhook subscription (url / events / active)
   * Partially updates a subscription: any of &#x60;url&#x60;, &#x60;events&#x60;, &#x60;isActive&#x60; (key present &#x3D; set, omitted &#x3D; unchanged). A new &#x60;url&#x60; is re-validated by the DNS-resolving SSRF guard (same rules as creation); &#x60;events&#x60; must be a non-empty subset of the allowed filters. The signing &#x60;secret&#x60; is **not** editable here — use &#x60;rotate-secret&#x60;. An empty body returns the current subscription unchanged. The response **strips the secret**.  **Permission:** &#x60;webhook:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Rate limit:** global limiter, 100 requests/min per IP.
   * @param id Webhook subscription UUID (as returned at creation / by the list endpoint). (required)
   * @param webhookSubscriptionUpdateRequest  (optional)
   * @return ApiResponse&lt;WebhookSubscriptionUpdateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookSubscriptionUpdateResponse> updateWebhookSubscriptionWithHttpInfo(String id, WebhookSubscriptionUpdateRequest webhookSubscriptionUpdateRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updateWebhookSubscriptionRequestBuilder(id, webhookSubscriptionUpdateRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updateWebhookSubscription", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<WebhookSubscriptionUpdateResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<WebhookSubscriptionUpdateResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<WebhookSubscriptionUpdateResponse>() {})
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

  private HttpRequest.Builder updateWebhookSubscriptionRequestBuilder(String id, WebhookSubscriptionUpdateRequest webhookSubscriptionUpdateRequest) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updateWebhookSubscription");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/webhooks/subscriptions/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(webhookSubscriptionUpdateRequest);
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
