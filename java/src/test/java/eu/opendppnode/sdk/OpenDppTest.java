/*
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: Apache-2.0
 */

package eu.opendppnode.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.opendppnode.sdk.api.PassportsApi;
import eu.opendppnode.sdk.api.PublicResolutionApi;
import eu.opendppnode.sdk.api.ServiceApi;
import eu.opendppnode.sdk.api.WebhooksApi;
import eu.opendppnode.sdk.invoker.ApiClient;
import org.junit.jupiter.api.Test;

/** Offline smoke tests for the ergonomic factory and the generated client wiring (no network). */
class OpenDppTest {

    @Test
    void defaultsToPublicNodeWithoutAuth() {
        ApiClient client = OpenDpp.client();
        assertEquals("https://opendpp-node.eu", client.getBaseUri());
        assertNull(client.getRequestInterceptor(), "no auth interceptor without an API key");
    }

    @Test
    void appliesBearerAuthWhenKeyProvided() {
        ApiClient client = OpenDpp.client("op_dpp_token_example");
        assertEquals("https://opendpp-node.eu", client.getBaseUri());
        assertNotNull(client.getRequestInterceptor(), "auth interceptor present with an API key");
    }

    @Test
    void honoursCustomBaseUrl() {
        ApiClient client = OpenDpp.client("https://demo.opendpp-node.eu", null);
        assertEquals("https://demo.opendpp-node.eu", client.getBaseUri());
    }

    @Test
    void wiresEveryGeneratedApi() {
        ApiClient client = OpenDpp.client();
        // Construction proves the generated surface links against the shared client — including the
        // Webhooks management API that the 3.1 codegen collision had silently dropped.
        assertNotNull(new ServiceApi(client));
        assertNotNull(new PassportsApi(client));
        assertNotNull(new PublicResolutionApi(client));
        assertNotNull(new WebhooksApi(client));
    }
}
