// Content-negotiation guards — offline, against a local echo server (Apache-2.0, (c) Opendpp UAB).
// Proves the two silent-failure fixes: (1) resolver requests without a caller Accept are pinned to
// application/ld+json (the representation the generated types describe); (2) the *As helpers parse
// vc+jwt / dc+sd-jwt as text (the raw client would hand back a Blob) and honour the chosen Accept.
import test from "node:test";
import assert from "node:assert/strict";
import { createServer, type Server } from "node:http";
import {
  createOpenDppClient,
  client as defaultClient,
  getHealth,
  resolvePublicPassport,
  resolvePublicPassportAs,
  resolvePublicBatteryUnitAs,
} from "../src/index.ts";

const JWS = "eyJhbGciOiJFUzI1NiJ9.eyJhIjoxfQ.c2ln";

// Serves a canned body per representation and records the Accept header of every request.
function startEchoServer(seenAccepts: Array<string | undefined>): Promise<{ server: Server; baseUrl: string }> {
  const server = createServer((req, res) => {
    seenAccepts.push(req.headers.accept);
    const accept = req.headers.accept ?? "";
    if (accept.includes("application/vc+jwt")) {
      res.writeHead(200, { "Content-Type": "application/vc+jwt" });
      res.end(JWS);
    } else if (accept.includes("text/html")) {
      res.writeHead(200, { "Content-Type": "text/html" });
      res.end("<html><body>unit</body></html>");
    } else if (accept.includes("application/aas+json")) {
      res.writeHead(200, { "Content-Type": "application/aas+json" });
      res.end(JSON.stringify({ assetAdministrationShells: [] }));
    } else {
      res.writeHead(200, { "Content-Type": "application/ld+json" });
      res.end(JSON.stringify({ id: "x", productId: "p", status: "ACTIVE" }));
    }
  });
  return new Promise((resolve) => {
    server.listen(0, "127.0.0.1", () => {
      const { port } = server.address() as { port: number };
      resolve({ server, baseUrl: `http://127.0.0.1:${port}` });
    });
  });
}

test("content negotiation guards", async (t) => {
  const seenAccepts: Array<string | undefined> = [];
  const { server, baseUrl } = await startEchoServer(seenAccepts);
  const client = createOpenDppClient({ baseUrl });
  t.after(() => server.close());

  await t.test("resolver without a caller Accept is pinned to application/ld+json", async () => {
    const { data } = await resolvePublicPassport({ client, path: { id: "any" } });
    assert.equal(seenAccepts.at(-1), "application/ld+json");
    assert.equal((data as { id?: string } | undefined)?.id, "x", "JSON-LD body parses as JSON");
  });

  await t.test("the default exported client pins the same way", async () => {
    defaultClient.setConfig({ baseUrl });
    try {
      await resolvePublicPassport({ path: { id: "any" } });
    } finally {
      defaultClient.setConfig({ baseUrl: "https://opendpp-node.eu" });
    }
    assert.equal(seenAccepts.at(-1), "application/ld+json");
  });

  await t.test("a caller-supplied Accept is not overridden", async () => {
    await resolvePublicPassport({ client, path: { id: "any" }, headers: { Accept: "application/aas+json" } });
    assert.equal(seenAccepts.at(-1), "application/aas+json");
  });

  await t.test("non-resolver paths are not pinned", async () => {
    await getHealth({ client });
    assert.notEqual(seenAccepts.at(-1), "application/ld+json");
  });

  await t.test("resolvePublicPassportAs vc+jwt yields the compact JWS as a string, not a Blob", async () => {
    const { data } = await resolvePublicPassportAs({ client, path: { id: "any" }, accept: "application/vc+jwt" });
    assert.equal(seenAccepts.at(-1), "application/vc+jwt");
    assert.equal(typeof data, "string");
    assert.equal(data, JWS);
  });

  await t.test("resolvePublicBatteryUnitAs text/html yields the page as a string", async () => {
    const { data } = await resolvePublicBatteryUnitAs({ client, path: { id: "any" }, accept: "text/html" });
    assert.equal(seenAccepts.at(-1), "text/html");
    assert.equal(typeof data, "string");
    assert.match(data as string, /<html>/);
  });
});
