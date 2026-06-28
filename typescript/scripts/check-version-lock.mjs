// Version lock: @opendpp/sdk's version MUST equal the vendored openapi.json's info.version — the SDK is
// version-locked to OPENAPI_VERSION. Run in CI and before publish. Exits non-zero on mismatch.
import { readFileSync } from "node:fs";

const pkg = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8"));
const spec = JSON.parse(readFileSync(new URL("../openapi.json", import.meta.url), "utf8"));

if (pkg.version !== spec.info.version) {
  console.error(`✗ version lock FAILED: package.json ${pkg.version} != openapi.json info.version ${spec.info.version}`);
  console.error("  The SDK version is locked to the API contract — bump package.json to match the vendored openapi.json (and regenerate).");
  process.exit(1);
}
console.log(`✓ version lock: @opendpp/sdk ${pkg.version} == openapi.json info.version`);
