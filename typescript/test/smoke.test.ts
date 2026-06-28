// @opendpp/sdk smoke test — proves the package works standalone (Apache-2.0, (c) Opendpp UAB).
// Runs via tsx against ../src; no network calls (it only checks wiring, not live responses).
import test from "node:test";
import assert from "node:assert/strict";
import {
  createOpenDppClient,
  client as defaultClient,
  getHealth,
  getApiVersion,
  createPassport,
  sealPassport,
  decodeGs1,
  whoami,
} from "../src/index.ts";

test("createOpenDppClient builds a configured client", () => {
  const client = createOpenDppClient({ apiKey: "test-key" });
  assert.ok(client, "a client instance is returned");
  assert.equal(typeof client.get, "function");
  assert.equal(typeof client.post, "function");
});

test("default client defaults to the public hosted node", () => {
  assert.ok(defaultClient, "a pre-configured default client is exported");
  assert.equal(typeof defaultClient.setConfig, "function");
  assert.equal(defaultClient.getConfig().baseUrl, "https://opendpp-node.eu");
});

test("operations are exported as callable functions", () => {
  for (const op of [getHealth, getApiVersion, createPassport, sealPassport, decodeGs1, whoami]) {
    assert.equal(typeof op, "function");
  }
});
