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

import eu.opendppnode.sdk.model.DidWebDocument;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;

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
public class VerifiableCredentialsApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public VerifiableCredentialsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public VerifiableCredentialsApi(ApiClient apiClient) {
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
   * Resolve a tenant&#39;s did:web DID document
   * Resolves the issuing workspace&#39;s &#x60;did:web&#x60; DID document (&#x60;application/did+json&#x60;). The DID is &#x60;did:web:opendpp-node.eu:tenants:{tenantId}&#x60;, which per the did:web method dereferences here. The document exposes **only public key material** — the workspace&#39;s eIDAS public key(s) as &#x60;JsonWebKey2020&#x60; verification methods, each with a stable &#x60;#key-&lt;index&gt;&#x60; id matching the &#x60;kid&#x60; of the credentials it signs.  Use it to verify any OpenDPP-issued Verifiable Credential (&#x60;Accept: application/vc+jwt&#x60;, &#x60;application/vc+ld+json&#x60;, or &#x60;application/dc+sd-jwt&#x60; on the public resolution endpoints) without out-of-band key exchange. Both current and retired keys are published and listed in &#x60;assertionMethod&#x60;/&#x60;authentication&#x60;, so credentials issued before a key rotation still verify; new credentials always use the current key. The optional &#x60;name&#x60; is the issuer&#39;s authoritative legal name (the same value used in every credential&#39;s &#x60;issuer.name&#x60;).  No authentication, no permission (public endpoint). Subject only to the global platform rate limit (100 req/min/IP). A workspace that has never provisioned a signing key returns 404. Never returns private key material.
   * @param tenantId The issuing workspace (tenant) id — the &#x60;{tenantId}&#x60; of its &#x60;did:web:opendpp-node.eu:tenants:{tenantId}&#x60; DID. (required)
   * @return DidWebDocument
   * @throws ApiException if fails to make API call
   */
  public DidWebDocument getTenantDidDocument(String tenantId) throws ApiException {
    ApiResponse<DidWebDocument> localVarResponse = getTenantDidDocumentWithHttpInfo(tenantId);
    return localVarResponse.getData();
  }

  /**
   * Resolve a tenant&#39;s did:web DID document
   * Resolves the issuing workspace&#39;s &#x60;did:web&#x60; DID document (&#x60;application/did+json&#x60;). The DID is &#x60;did:web:opendpp-node.eu:tenants:{tenantId}&#x60;, which per the did:web method dereferences here. The document exposes **only public key material** — the workspace&#39;s eIDAS public key(s) as &#x60;JsonWebKey2020&#x60; verification methods, each with a stable &#x60;#key-&lt;index&gt;&#x60; id matching the &#x60;kid&#x60; of the credentials it signs.  Use it to verify any OpenDPP-issued Verifiable Credential (&#x60;Accept: application/vc+jwt&#x60;, &#x60;application/vc+ld+json&#x60;, or &#x60;application/dc+sd-jwt&#x60; on the public resolution endpoints) without out-of-band key exchange. Both current and retired keys are published and listed in &#x60;assertionMethod&#x60;/&#x60;authentication&#x60;, so credentials issued before a key rotation still verify; new credentials always use the current key. The optional &#x60;name&#x60; is the issuer&#39;s authoritative legal name (the same value used in every credential&#39;s &#x60;issuer.name&#x60;).  No authentication, no permission (public endpoint). Subject only to the global platform rate limit (100 req/min/IP). A workspace that has never provisioned a signing key returns 404. Never returns private key material.
   * @param tenantId The issuing workspace (tenant) id — the &#x60;{tenantId}&#x60; of its &#x60;did:web:opendpp-node.eu:tenants:{tenantId}&#x60; DID. (required)
   * @return ApiResponse&lt;DidWebDocument&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DidWebDocument> getTenantDidDocumentWithHttpInfo(String tenantId) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getTenantDidDocumentRequestBuilder(tenantId);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getTenantDidDocument", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DidWebDocument>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DidWebDocument>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DidWebDocument>() {})
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

  private HttpRequest.Builder getTenantDidDocumentRequestBuilder(String tenantId) throws ApiException {
    // verify the required parameter 'tenantId' is set
    if (tenantId == null) {
      throw new ApiException(400, "Missing the required parameter 'tenantId' when calling getTenantDidDocument");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/tenants/{tenantId}/did.json"
        .replace("{tenantId}", ApiClient.urlEncode(tenantId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/did+json, application/json");

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
   * Tenant revocation status list (W3C Bitstring Status List)
   * Serves the workspace&#39;s W3C **Bitstring Status List** as a signed enveloping &#x60;vc+jwt&#x60; (&#x60;application/vc+jwt&#x60;) — a &#x60;BitstringStatusListCredential&#x60; whose GZIP+base64 encoded bitstring sets the bit of every passport whose status is RECALLED or DECOMMISSIONED. An OpenDPP-issued DPP credential&#39;s &#x60;credentialStatus.statusListCredential&#x60; points here; a verifier fetches this list and checks the bit at the credential&#39;s &#x60;statusListIndex&#x60; to determine whether it has been revoked. Signed by the workspace&#39;s current key (stable &#x60;kid&#x60;), verifiable via the DID document at &#x60;/tenants/{tenantId}/did.json&#x60;.  No authentication, no permission (public endpoint). Subject only to the global platform rate limit (100 req/min/IP). A workspace with no signing key returns 404.
   * @param tenantId The issuing workspace (tenant) id. (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String getTenantRevocationStatusList(String tenantId) throws ApiException {
    ApiResponse<String> localVarResponse = getTenantRevocationStatusListWithHttpInfo(tenantId);
    return localVarResponse.getData();
  }

  /**
   * Tenant revocation status list (W3C Bitstring Status List)
   * Serves the workspace&#39;s W3C **Bitstring Status List** as a signed enveloping &#x60;vc+jwt&#x60; (&#x60;application/vc+jwt&#x60;) — a &#x60;BitstringStatusListCredential&#x60; whose GZIP+base64 encoded bitstring sets the bit of every passport whose status is RECALLED or DECOMMISSIONED. An OpenDPP-issued DPP credential&#39;s &#x60;credentialStatus.statusListCredential&#x60; points here; a verifier fetches this list and checks the bit at the credential&#39;s &#x60;statusListIndex&#x60; to determine whether it has been revoked. Signed by the workspace&#39;s current key (stable &#x60;kid&#x60;), verifiable via the DID document at &#x60;/tenants/{tenantId}/did.json&#x60;.  No authentication, no permission (public endpoint). Subject only to the global platform rate limit (100 req/min/IP). A workspace with no signing key returns 404.
   * @param tenantId The issuing workspace (tenant) id. (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> getTenantRevocationStatusListWithHttpInfo(String tenantId) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getTenantRevocationStatusListRequestBuilder(tenantId);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getTenantRevocationStatusList", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<String>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<String>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<String>() {})
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

  private HttpRequest.Builder getTenantRevocationStatusListRequestBuilder(String tenantId) throws ApiException {
    // verify the required parameter 'tenantId' is set
    if (tenantId == null) {
      throw new ApiException(400, "Missing the required parameter 'tenantId' when calling getTenantRevocationStatusList");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/tenants/{tenantId}/status/revocation"
        .replace("{tenantId}", ApiClient.urlEncode(tenantId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/vc+jwt, application/json");

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
