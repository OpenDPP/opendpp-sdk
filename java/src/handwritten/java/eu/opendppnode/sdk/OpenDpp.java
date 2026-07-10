/*
 * OpenDPP Java SDK — ergonomic entry point.
 *
 * A fully-typed client for the OpenDPP public API, mechanically generated from the public OpenAPI
 * contract (openapi.json) and version-locked to it. Every class under `eu.opendppnode.sdk.api` /
 * `.model` / `.invoker` is generated; the only hand-written code is this thin factory.
 *
 * Boundary: this SDK is ergonomics only. It embeds no tier/masking logic and no restricted-key
 * knowledge — every privileged operation is simply a typed call to the hosted node behind your
 * Developer-Plan key. It leaks nothing the public spec doesn't already.
 *
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations.
 *
 * "OpenDPP" is a trademark of Opendpp UAB; the Apache-2.0 license grants no rights to the marks.
 */

package eu.opendppnode.sdk;

import eu.opendppnode.sdk.invoker.ApiClient;
import java.net.http.HttpRequest;
import java.util.function.Consumer;

/**
 * Factory for a configured {@link ApiClient}. Pass the returned client to any generated {@code *Api}
 * constructor — e.g. {@code new PassportsApi(OpenDpp.client(apiKey))} — then call its typed operations.
 *
 * <pre>{@code
 * ApiClient client = OpenDpp.client(System.getenv("OPENDPP_API_KEY"));
 * HealthStatus health = new ServiceApi(client).getHealth();          // public, no key needed
 * PassportResponse created = new PassportsApi(client).createPassport(request);
 * }</pre>
 *
 * Ergonomics only: this wires the base URL and bearer auth. Every privileged operation remains a call
 * to the hosted node behind your Developer-Plan key.
 */
public final class OpenDpp {

    /** The public hosted OpenDPP node — the default base URL. */
    public static final String DEFAULT_BASE_URL = "https://opendpp-node.eu";

    private OpenDpp() {
    }

    /** A client for the public hosted node with no credentials (public operations only). */
    public static ApiClient client() {
        return client(DEFAULT_BASE_URL, null);
    }

    /** A client for the public hosted node, authenticated with a Developer-Plan API key. */
    public static ApiClient client(String apiKey) {
        return client(DEFAULT_BASE_URL, apiKey);
    }

    /**
     * A client for an explicit base URL, optionally authenticated with a Developer-Plan API key.
     *
     * @param baseUrl base URL of the OpenDPP node; a blank/null value falls back to {@link #DEFAULT_BASE_URL}
     * @param apiKey  Developer-Plan API key; when non-blank it is sent as {@code Authorization: Bearer <apiKey>}
     *                on every request. Public operations work without it.
     */
    public static ApiClient client(String baseUrl, String apiKey) {
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            final String authorization = "Bearer " + apiKey;
            Consumer<HttpRequest.Builder> interceptor = builder -> builder.header("Authorization", authorization);
            apiClient.setRequestInterceptor(interceptor);
        }
        return apiClient;
    }
}
