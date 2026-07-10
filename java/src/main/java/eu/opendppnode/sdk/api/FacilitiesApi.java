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
import eu.opendppnode.sdk.model.FacilityCreateRequest;
import eu.opendppnode.sdk.model.FacilityCreatedEnvelope;
import eu.opendppnode.sdk.model.FacilityDeletedEnvelope;
import eu.opendppnode.sdk.model.FacilityEnvelope;
import eu.opendppnode.sdk.model.FacilityListEnvelope;
import eu.opendppnode.sdk.model.FacilityUpdateRequest;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.PassportQuotaError;

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
public class FacilitiesApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public FacilitiesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FacilitiesApi(ApiClient apiClient) {
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
   * Register a facility (GS1 GLN)
   * Registers a manufacturing/processing facility as tenant-scoped master data, backing the Unique Facility Identifier (UFI, EN 18219). Passports reference facilities via &#x60;facilityId&#x60;.  **Permission:** &#x60;facility:write&#x60;. Authenticate with a Bearer API key (&#x60;op_dpp_token_…&#x60;) or a session JWT; cookie-authenticated sessions must also send the &#x60;X-CSRF-Token&#x60; header (double-submit against the &#x60;opendpp_csrf&#x60; cookie) — Bearer clients are exempt. Write permissions are subscription-gated: a lapsed workspace subscription returns **402**.  **GLN validation:** &#x60;gln&#x60; is trimmed, then must be exactly 13 digits with a valid GS1 modulo-10 check digit (same weighting algorithm as GTIN). The GLN is unique **platform-wide** (database unique constraint), so a duplicate returns **409** even if the existing facility belongs to another tenant.  **Country:** &#x60;country&#x60; must match &#x60;^[A-Za-z]{2}$&#x60; after trimming and is stored uppercased.  **Operator binding:** if &#x60;operatorId&#x60; is supplied (non-empty), that Economic Operator must be bound to your tenant workspace, otherwise **403**. An empty/whitespace &#x60;operatorId&#x60; is stored as &#x60;null&#x60;. Requests authenticated with an **operator-scoped API key** may only attach facilities to their own operator: a mismatched &#x60;operatorId&#x60; returns **403**, and when omitted the key&#39;s operator id is applied automatically.  &#x60;activity&#x60;, &#x60;streetAddress&#x60;, &#x60;city&#x60; and &#x60;postalCode&#x60; are trimmed; empty/whitespace values are stored as &#x60;null&#x60;.  **Public/privileged field split:** the public **JSON-LD** passport document exposes &#x60;id&#x60;, &#x60;gln&#x60;, &#x60;name&#x60;, &#x60;activity&#x60; and &#x60;country&#x60; of a linked facility; the public **AAS** export emits only the GLN, name and country (&#x60;manufacturingFacilityGln&#x60; / &#x60;manufacturingFacilityName&#x60; / &#x60;manufacturingFacilityCountry&#x60;, plus the GLN as a &#x60;urn:gs1:gln:&#x60; global asset reference) — the facility &#x60;id&#x60; and &#x60;activity&#x60; are never emitted in AAS. &#x60;streetAddress&#x60;, &#x60;city&#x60; and &#x60;postalCode&#x60; are owner-only in both formats. This endpoint returns the full row to you as the owner.  Emits a &#x60;facility.created&#x60; audit event and an in-app notification.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param facilityCreateRequest  (required)
   * @return FacilityCreatedEnvelope
   * @throws ApiException if fails to make API call
   */
  public FacilityCreatedEnvelope createFacility(FacilityCreateRequest facilityCreateRequest) throws ApiException {
    ApiResponse<FacilityCreatedEnvelope> localVarResponse = createFacilityWithHttpInfo(facilityCreateRequest);
    return localVarResponse.getData();
  }

  /**
   * Register a facility (GS1 GLN)
   * Registers a manufacturing/processing facility as tenant-scoped master data, backing the Unique Facility Identifier (UFI, EN 18219). Passports reference facilities via &#x60;facilityId&#x60;.  **Permission:** &#x60;facility:write&#x60;. Authenticate with a Bearer API key (&#x60;op_dpp_token_…&#x60;) or a session JWT; cookie-authenticated sessions must also send the &#x60;X-CSRF-Token&#x60; header (double-submit against the &#x60;opendpp_csrf&#x60; cookie) — Bearer clients are exempt. Write permissions are subscription-gated: a lapsed workspace subscription returns **402**.  **GLN validation:** &#x60;gln&#x60; is trimmed, then must be exactly 13 digits with a valid GS1 modulo-10 check digit (same weighting algorithm as GTIN). The GLN is unique **platform-wide** (database unique constraint), so a duplicate returns **409** even if the existing facility belongs to another tenant.  **Country:** &#x60;country&#x60; must match &#x60;^[A-Za-z]{2}$&#x60; after trimming and is stored uppercased.  **Operator binding:** if &#x60;operatorId&#x60; is supplied (non-empty), that Economic Operator must be bound to your tenant workspace, otherwise **403**. An empty/whitespace &#x60;operatorId&#x60; is stored as &#x60;null&#x60;. Requests authenticated with an **operator-scoped API key** may only attach facilities to their own operator: a mismatched &#x60;operatorId&#x60; returns **403**, and when omitted the key&#39;s operator id is applied automatically.  &#x60;activity&#x60;, &#x60;streetAddress&#x60;, &#x60;city&#x60; and &#x60;postalCode&#x60; are trimmed; empty/whitespace values are stored as &#x60;null&#x60;.  **Public/privileged field split:** the public **JSON-LD** passport document exposes &#x60;id&#x60;, &#x60;gln&#x60;, &#x60;name&#x60;, &#x60;activity&#x60; and &#x60;country&#x60; of a linked facility; the public **AAS** export emits only the GLN, name and country (&#x60;manufacturingFacilityGln&#x60; / &#x60;manufacturingFacilityName&#x60; / &#x60;manufacturingFacilityCountry&#x60;, plus the GLN as a &#x60;urn:gs1:gln:&#x60; global asset reference) — the facility &#x60;id&#x60; and &#x60;activity&#x60; are never emitted in AAS. &#x60;streetAddress&#x60;, &#x60;city&#x60; and &#x60;postalCode&#x60; are owner-only in both formats. This endpoint returns the full row to you as the owner.  Emits a &#x60;facility.created&#x60; audit event and an in-app notification.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param facilityCreateRequest  (required)
   * @return ApiResponse&lt;FacilityCreatedEnvelope&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FacilityCreatedEnvelope> createFacilityWithHttpInfo(FacilityCreateRequest facilityCreateRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = createFacilityRequestBuilder(facilityCreateRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("createFacility", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<FacilityCreatedEnvelope>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<FacilityCreatedEnvelope>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<FacilityCreatedEnvelope>() {})
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

  private HttpRequest.Builder createFacilityRequestBuilder(FacilityCreateRequest facilityCreateRequest) throws ApiException {
    // verify the required parameter 'facilityCreateRequest' is set
    if (facilityCreateRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'facilityCreateRequest' when calling createFacility");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/facilities";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(facilityCreateRequest);
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
   * Delete a facility (passports are unlinked, never deleted)
   * Removes the facility master-data row. **Passports are never deleted by this operation**: &#x60;Passport.facilityId&#x60; is a &#x60;SET NULL&#x60; foreign key, so any passports referencing the facility simply lose their UFI link (&#x60;facilityId&#x60; becomes &#x60;null&#x60;) and remain fully intact and publicly resolvable.  **Permission:** &#x60;facility:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Operator-scoped keys:** deleting a facility that belongs to a different Economic Operator returns **403**; facilities with &#x60;operatorId: null&#x60; are deletable.  Emits a &#x60;facility.deleted&#x60; audit event and an in-app notification. **404 body:** standard envelope with message &#x60;Facility &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @return FacilityDeletedEnvelope
   * @throws ApiException if fails to make API call
   */
  public FacilityDeletedEnvelope deleteFacility(String id) throws ApiException {
    ApiResponse<FacilityDeletedEnvelope> localVarResponse = deleteFacilityWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Delete a facility (passports are unlinked, never deleted)
   * Removes the facility master-data row. **Passports are never deleted by this operation**: &#x60;Passport.facilityId&#x60; is a &#x60;SET NULL&#x60; foreign key, so any passports referencing the facility simply lose their UFI link (&#x60;facilityId&#x60; becomes &#x60;null&#x60;) and remain fully intact and publicly resolvable.  **Permission:** &#x60;facility:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Operator-scoped keys:** deleting a facility that belongs to a different Economic Operator returns **403**; facilities with &#x60;operatorId: null&#x60; are deletable.  Emits a &#x60;facility.deleted&#x60; audit event and an in-app notification. **404 body:** standard envelope with message &#x60;Facility &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @return ApiResponse&lt;FacilityDeletedEnvelope&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FacilityDeletedEnvelope> deleteFacilityWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteFacilityRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteFacility", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<FacilityDeletedEnvelope>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<FacilityDeletedEnvelope>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<FacilityDeletedEnvelope>() {})
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

  private HttpRequest.Builder deleteFacilityRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteFacility");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/facilities/{id}"
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
   * Get a single facility
   * Fetches one facility by id, scoped to your tenant workspace.  **Permission:** &#x60;facility:read&#x60;.  **Operator-scoped keys:** if the facility belongs to a *different* Economic Operator than the key&#39;s scope, the response is **403**. Facilities with no operator (&#x60;operatorId: null&#x60;) **are** readable by operator-scoped keys here, even though they are excluded from the list endpoint.  Returns the full row including the privileged address fields (&#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60;) that public passport documents never expose. (Public exposure of a linked facility: the JSON-LD document shows &#x60;id&#x60;/&#x60;gln&#x60;/&#x60;name&#x60;/&#x60;activity&#x60;/&#x60;country&#x60;; the AAS export only the GLN, name and country.)  **404 body:** standard envelope with message &#x60;Facility &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @return FacilityEnvelope
   * @throws ApiException if fails to make API call
   */
  public FacilityEnvelope getFacility(String id) throws ApiException {
    ApiResponse<FacilityEnvelope> localVarResponse = getFacilityWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Get a single facility
   * Fetches one facility by id, scoped to your tenant workspace.  **Permission:** &#x60;facility:read&#x60;.  **Operator-scoped keys:** if the facility belongs to a *different* Economic Operator than the key&#39;s scope, the response is **403**. Facilities with no operator (&#x60;operatorId: null&#x60;) **are** readable by operator-scoped keys here, even though they are excluded from the list endpoint.  Returns the full row including the privileged address fields (&#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60;) that public passport documents never expose. (Public exposure of a linked facility: the JSON-LD document shows &#x60;id&#x60;/&#x60;gln&#x60;/&#x60;name&#x60;/&#x60;activity&#x60;/&#x60;country&#x60;; the AAS export only the GLN, name and country.)  **404 body:** standard envelope with message &#x60;Facility &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @return ApiResponse&lt;FacilityEnvelope&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FacilityEnvelope> getFacilityWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getFacilityRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getFacility", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<FacilityEnvelope>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<FacilityEnvelope>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<FacilityEnvelope>() {})
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

  private HttpRequest.Builder getFacilityRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getFacility");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/facilities/{id}"
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
   * List facilities in the tenant workspace
   * Lists all facilities registered under your tenant workspace, sorted by &#x60;createdAt&#x60; descending. **Paginated** with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200); &#x60;count&#x60; is this page&#39;s size, &#x60;total&#x60;/&#x60;totalPages&#x60; describe the full set. A non-numeric &#x60;page&#x60;/&#x60;limit&#x60; falls back to its default.  **Permission:** &#x60;facility:read&#x60; (Bearer API key or session JWT/cookie).  **Operator-scoped keys:** when authenticated with an API key scoped to an Economic Operator, the list contains only facilities whose &#x60;operatorId&#x60; equals the key&#39;s operator — facilities with no operator (&#x60;operatorId: null&#x60;) are **excluded** from the list (they remain readable individually via &#x60;GET /api/v1/facilities/{id}&#x60;).  The full row is returned to the owner, including the privileged address fields (&#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60;) that public passport documents never expose (owner-only in JSON-LD; never emitted in AAS).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return FacilityListEnvelope
   * @throws ApiException if fails to make API call
   */
  public FacilityListEnvelope listFacilities(Integer page, Integer limit) throws ApiException {
    ApiResponse<FacilityListEnvelope> localVarResponse = listFacilitiesWithHttpInfo(page, limit);
    return localVarResponse.getData();
  }

  /**
   * List facilities in the tenant workspace
   * Lists all facilities registered under your tenant workspace, sorted by &#x60;createdAt&#x60; descending. **Paginated** with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200); &#x60;count&#x60; is this page&#39;s size, &#x60;total&#x60;/&#x60;totalPages&#x60; describe the full set. A non-numeric &#x60;page&#x60;/&#x60;limit&#x60; falls back to its default.  **Permission:** &#x60;facility:read&#x60; (Bearer API key or session JWT/cookie).  **Operator-scoped keys:** when authenticated with an API key scoped to an Economic Operator, the list contains only facilities whose &#x60;operatorId&#x60; equals the key&#39;s operator — facilities with no operator (&#x60;operatorId: null&#x60;) are **excluded** from the list (they remain readable individually via &#x60;GET /api/v1/facilities/{id}&#x60;).  The full row is returned to the owner, including the privileged address fields (&#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60;) that public passport documents never expose (owner-only in JSON-LD; never emitted in AAS).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return ApiResponse&lt;FacilityListEnvelope&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FacilityListEnvelope> listFacilitiesWithHttpInfo(Integer page, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listFacilitiesRequestBuilder(page, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listFacilities", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<FacilityListEnvelope>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<FacilityListEnvelope>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<FacilityListEnvelope>() {})
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

  private HttpRequest.Builder listFacilitiesRequestBuilder(Integer page, Integer limit) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/facilities";

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
   * Update facility master data (GLN is immutable)
   * Partially updates a facility&#39;s master data. **The GLN itself is immutable** — it is the resolvable UFI identifier; a &#x60;gln&#x60; key in the body is silently ignored (as is &#x60;operatorId&#x60; — the operator binding cannot be changed here).  **Permission:** &#x60;facility:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Field semantics (all optional):** - &#x60;name&#x60; — applied only when a non-empty string; an empty/whitespace or non-string value is silently ignored (&#x60;name&#x60; can never be cleared). - &#x60;activity&#x60;, &#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60; — applied whenever the key is *present* in the body: the value is stringified and trimmed; anything that trims to empty (&#x60;null&#x60;, &#x60;\&quot;\&quot;&#x60;, or a whitespace-only string) **clears the field to null** — the same normalization as POST. - &#x60;country&#x60; — when present as a string it must match &#x60;^[A-Za-z]{2}$&#x60; (else **400**) and is stored uppercased; a non-string value is silently ignored.  An empty body (or one with no recognized fields) is accepted: the response is **200** with the otherwise-unchanged row, though &#x60;updatedAt&#x60; is still bumped.  **Operator-scoped keys:** updating a facility that belongs to a different Economic Operator returns **403**; facilities with &#x60;operatorId: null&#x60; are updatable.  Emits a &#x60;facility.updated&#x60; audit event recording the changed fields.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @param facilityUpdateRequest  (optional)
   * @return FacilityEnvelope
   * @throws ApiException if fails to make API call
   */
  public FacilityEnvelope updateFacility(String id, FacilityUpdateRequest facilityUpdateRequest) throws ApiException {
    ApiResponse<FacilityEnvelope> localVarResponse = updateFacilityWithHttpInfo(id, facilityUpdateRequest);
    return localVarResponse.getData();
  }

  /**
   * Update facility master data (GLN is immutable)
   * Partially updates a facility&#39;s master data. **The GLN itself is immutable** — it is the resolvable UFI identifier; a &#x60;gln&#x60; key in the body is silently ignored (as is &#x60;operatorId&#x60; — the operator binding cannot be changed here).  **Permission:** &#x60;facility:write&#x60;. Cookie sessions must send &#x60;X-CSRF-Token&#x60;; write permissions are subscription-gated (**402** when lapsed).  **Field semantics (all optional):** - &#x60;name&#x60; — applied only when a non-empty string; an empty/whitespace or non-string value is silently ignored (&#x60;name&#x60; can never be cleared). - &#x60;activity&#x60;, &#x60;streetAddress&#x60;, &#x60;city&#x60;, &#x60;postalCode&#x60; — applied whenever the key is *present* in the body: the value is stringified and trimmed; anything that trims to empty (&#x60;null&#x60;, &#x60;\&quot;\&quot;&#x60;, or a whitespace-only string) **clears the field to null** — the same normalization as POST. - &#x60;country&#x60; — when present as a string it must match &#x60;^[A-Za-z]{2}$&#x60; (else **400**) and is stored uppercased; a non-string value is silently ignored.  An empty body (or one with no recognized fields) is accepted: the response is **200** with the otherwise-unchanged row, though &#x60;updatedAt&#x60; is still bumped.  **Operator-scoped keys:** updating a facility that belongs to a different Economic Operator returns **403**; facilities with &#x60;operatorId: null&#x60; are updatable.  Emits a &#x60;facility.updated&#x60; audit event recording the changed fields.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param id Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
   * @param facilityUpdateRequest  (optional)
   * @return ApiResponse&lt;FacilityEnvelope&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FacilityEnvelope> updateFacilityWithHttpInfo(String id, FacilityUpdateRequest facilityUpdateRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updateFacilityRequestBuilder(id, facilityUpdateRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updateFacility", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<FacilityEnvelope>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<FacilityEnvelope>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<FacilityEnvelope>() {})
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

  private HttpRequest.Builder updateFacilityRequestBuilder(String id, FacilityUpdateRequest facilityUpdateRequest) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updateFacility");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/facilities/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(facilityUpdateRequest);
      localVarRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
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
