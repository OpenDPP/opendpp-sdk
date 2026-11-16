// Type-checks every ```ts block in README.md against the committed client (src/), so a
// quickstart that stops compiling fails CI instead of reaching npm. Each block is checked on its
// own, with `id` (and `client`, when a block does not create or import one) declared as the free
// variables the README leaves to the reader.
import { execFileSync } from "node:child_process";
import { mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";

const dir = ".readme-check";
const blocks = [...readFileSync("README.md", "utf8").matchAll(/```ts\n([\s\S]*?)```/g)].map((m) => m[1]);
if (blocks.length === 0) throw new Error("no ts blocks found in README.md");

rmSync(dir, { recursive: true, force: true });
mkdirSync(dir);
try {
  blocks.forEach((code, i) => {
    const ownsClient = /createOpenDppClient\(|import \{[^}]*\bclient\b/.test(code);
    const prelude = `declare const id: string;\n${ownsClient ? "" : 'declare const client: ReturnType<typeof import("@opendpp/sdk").createOpenDppClient>;\n'}`;
    writeFileSync(`${dir}/snippet-${i}.ts`, `${prelude}${code}\nexport {};\n`);
  });
  writeFileSync(
    `${dir}/tsconfig.json`,
    JSON.stringify({
      extends: "../tsconfig.json",
      compilerOptions: { baseUrl: "..", paths: { "@opendpp/sdk": ["src/index.ts"] } },
      include: ["./*.ts"],
    }),
  );
  execFileSync("npx", ["tsc", "-p", `${dir}/tsconfig.json`], { stdio: "inherit" });
  console.log(`README snippets OK (${blocks.length} blocks)`);
} finally {
  rmSync(dir, { recursive: true, force: true });
}
