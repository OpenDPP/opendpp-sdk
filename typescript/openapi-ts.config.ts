import { defineConfig } from "@hey-api/openapi-ts";

// Generates the typed client from the committed openapi.json — the OpenDPP public API contract,
// version-locked to OPENAPI_VERSION. The output under src/generated/ is COMMITTED and re-checked in
// CI: `npm run generate` must produce no diff (the drift guard). Regenerate after bumping openapi.json
// with `npm run generate`. The generated client is fully self-contained (bundled fetch, zero runtime
// deps); the only hand-written code is the thin ergonomic wrapper in src/index.ts.
export default defineConfig({
  input: "openapi.json",
  output: "src/generated",
});
