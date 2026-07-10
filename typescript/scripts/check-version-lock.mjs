// Version lock: @opendpp/sdk's MAJOR.MINOR MUST equal the vendored openapi.json's info.version
// (OPENAPI_VERSION) — the PATCH digit is the SDK's own lane, so a client-only fix can ship against
// the same contract (e.g. 1.11.1 targets contract 1.11.0). The SDK patch must never sit BELOW the
// contract's (that would claim a contract state older than the vendored spec). Run in CI and before
// publish. Exits non-zero on violation.
import { readFileSync } from "node:fs";

const pkg = JSON.parse(readFileSync(new URL("../package.json", import.meta.url), "utf8"));
const spec = JSON.parse(readFileSync(new URL("../openapi.json", import.meta.url), "utf8"));

const parse = (label, version) => {
  const m = /^(\d+)\.(\d+)\.(\d+)$/.exec(version);
  if (!m) {
    console.error(`✗ version lock FAILED: ${label} version ${JSON.stringify(version)} is not plain MAJOR.MINOR.PATCH`);
    process.exit(1);
  }
  return m.slice(1).map(Number);
};

const [pkgMajor, pkgMinor, pkgPatch] = parse("package.json", pkg.version);
const [specMajor, specMinor, specPatch] = parse("openapi.json info.version", spec.info.version);

if (pkgMajor !== specMajor || pkgMinor !== specMinor) {
  console.error(`✗ version lock FAILED: package.json ${pkg.version} != openapi.json info.version ${spec.info.version} (major.minor)`);
  console.error("  The SDK's major.minor is locked to the API contract — bump package.json to match the vendored openapi.json (and regenerate).");
  process.exit(1);
}
if (pkgPatch < specPatch) {
  console.error(`✗ version lock FAILED: package.json ${pkg.version} is BEHIND openapi.json info.version ${spec.info.version}`);
  console.error("  The SDK patch digit may exceed the contract's (SDK-only fixes) but never trail it.");
  process.exit(1);
}
console.log(`✓ version lock: @opendpp/sdk ${pkg.version} targets contract ${spec.info.version} (major.minor locked; patch is the SDK fix lane)`);
