// Regenerates the Kotlin OpenCode API client in android/core/api from api/openapi.json.
//
// Usage (from tools/api-gen):
//   bun run generate   regenerate from the committed api/openapi.json
//   bun run refresh    download openapi.json from the local OpenCode service, pin its version, regenerate
//   bun run check      regenerate into a temporary directory and fail if the committed client differs
//
// Pipeline: api/openapi.json + @opencode/schema events (augment-events.ts) -> generator
// workarounds (prepare-spec.ts) -> openapi-generator (Java; the pinned jar is downloaded once)
// -> android/core/api (Kotlin Multiplatform, Ktor client).
import { $ } from "bun"
import { cp, mkdir, mkdtemp, readdir, rm } from "node:fs/promises"
import { homedir, tmpdir } from "node:os"
import { join, relative, resolve } from "node:path"
import { prepareSpec } from "./prepare-spec"

const ROOT = resolve(import.meta.dir, "../../..")
const GEN_DIR = resolve(import.meta.dir, "..")
const OUT_DIR = join(ROOT, "android/core/api/src/commonMain/kotlin")
const PACKAGE = "io.github.beppe2880.opencodemobile.api"
const GENERATOR_VERSION = "7.22.0"
const GENERATOR_JAR = join(
  process.env.XDG_CACHE_HOME ?? join(homedir(), ".cache"),
  "opencode-mobile",
  `openapi-generator-cli-${GENERATOR_VERSION}.jar`,
)

const mode = process.argv[2] ?? "generate"
if (!["generate", "refresh", "check"].includes(mode)) {
  console.error(`Unknown mode "${mode}": use generate, refresh or check`)
  process.exit(1)
}

const work = await mkdtemp(join(tmpdir(), "opencode-api-gen-"))
try {
  if (mode === "refresh") await refresh()
  await generate()
} finally {
  await rm(work, { recursive: true, force: true })
}

/** Pins api/openapi.json, api/OPENCODE_VERSION and @opencode/schema to the local server. */
async function refresh() {
  console.log("Downloading the OpenAPI document from the local OpenCode service...")
  await Bun.write(join(ROOT, "api/openapi.json"), await $`opencode api get /openapi.json`.text())
  const info = JSON.parse(await $`opencode api get /api/info`.text())
  const build = String(info.version) // for example 2.0.27+1f4d07a
  const version = build.split("+")[0]
  await Bun.write(join(ROOT, "api/OPENCODE_VERSION"), `${build}\n`)
  await $`bun add @opencode/schema@${version}`.cwd(GEN_DIR).quiet()
  console.log(`Pinned OpenCode ${version}`)
}

async function generate() {
  const opencodeVersion = (await Bun.file(join(ROOT, "api/OPENCODE_VERSION")).text()).trim().split("+")[0]
  // Imported here so that `refresh` can first install the matching @opencode/schema.
  const { augmentEvents } = await import("./augment-events")

  const spec = await Bun.file(join(ROOT, "api/openapi.json")).json()
  console.log(`events: added ${augmentEvents(spec)} definitions`)
  console.log(`spec: ${prepareSpec(spec)}`)
  const prepared = join(work, "prepared.json")
  await Bun.write(prepared, JSON.stringify(spec, null, 2))

  // Two passes: the jvm-retrofit2 library emits sealed interfaces for discriminated unions
  // but a JVM-only client, while the Ktor-based multiplatform library emits a multiplatform
  // client but flattens unions into `actualInstance: Any?`. Models come from the first, the
  // API classes and their Ktor infrastructure from the second; both share the model names.
  const common = [
    `packageName=${PACKAGE}`,
    "serializationLibrary=kotlinx_serialization",
    "dateLibrary=string",
    "useCoroutines=true",
    "generateOneOfAnyOfWrappers=true",
    "enumUnknownDefaultCase=false",
    "enumPropertyNaming=UPPERCASE",
    "omitGradleWrapper=true",
  ]
  const mappings = [
    "--type-mappings=integer=kotlin.Long,number=kotlin.Double,decimal=kotlin.Double,AnyType=JsonElement,object=JsonObject",
    "--import-mappings=JsonElement=kotlinx.serialization.json.JsonElement,JsonObject=kotlinx.serialization.json.JsonObject",
  ]
  const models = join(work, "models")
  await runGenerator([
    "generate", "-i", prepared, "-g", "kotlin", "-o", models,
    "--global-property=models,modelDocs=false,modelTests=false",
    `--additional-properties=${[...common, "library=jvm-retrofit2"].join(",")}`,
    ...mappings,
  ])
  const client = join(work, "client")
  await runGenerator([
    "generate", "-i", prepared, "-g", "kotlin", "-o", client,
    "--global-property=apis,supportingFiles,apiDocs=false,apiTests=false",
    `--additional-properties=${[...common, "library=multiplatform"].join(",")}`,
    ...mappings,
  ])

  const target = mode === "check" ? join(work, "check") : OUT_DIR
  await rm(target, { recursive: true, force: true })
  await mkdir(target, { recursive: true })
  await cp(join(models, "src/main/kotlin"), target, { recursive: true })
  for (const dir of ["apis", "auth", "infrastructure"]) {
    const path = join(PACKAGE.replaceAll(".", "/"), dir)
    await cp(join(client, "src/commonMain/kotlin", path), join(target, path), { recursive: true })
  }
  // jvm-retrofit2 models rely on the implicit `kotlin.jvm` import, which common code lacks.
  for (const file of await listFiles(target)) {
    const path = join(target, file)
    const text = await Bun.file(path).text()
    if (text.includes("@JvmInline") && !text.includes("import kotlin.jvm.JvmInline")) {
      await Bun.write(path, text.replace(/^package .*$/m, (line) => `${line}\n\nimport kotlin.jvm.JvmInline`))
    }
  }
  await Bun.write(
    join(target, PACKAGE.replaceAll(".", "/"), "OpenCodeApiVersion.kt"),
    `// Generated by tools/api-gen. Do not edit.
package ${PACKAGE}

/** OpenCode server version whose OpenAPI document and event schemas produced this client. */
object OpenCodeApiVersion {
    const val OPENCODE = "${opencodeVersion}"
    const val GENERATOR = "${GENERATOR_VERSION}"
}
`,
  )

  if (mode !== "check") {
    const files = (await listFiles(OUT_DIR)).filter((file) => file.endsWith(".kt"))
    console.log(`Generated ${files.length} Kotlin files in android/core/api for OpenCode ${opencodeVersion}`)
    return
  }
  const differences = await compareDirs(OUT_DIR, target)
  if (differences.length === 0) {
    console.log("Generated API is up to date.")
    return
  }
  console.error(differences.slice(0, 20).join("\n"))
  console.error("Generated API differs; run `bun run generate` in tools/api-gen")
  process.exit(1)
}

async function runGenerator(args: string[]) {
  if (!Bun.which("java")) throw new Error("openapi-generator needs Java (JDK 21 is used for the Android build)")
  if (!(await Bun.file(GENERATOR_JAR).exists())) {
    const url = `https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${GENERATOR_VERSION}/openapi-generator-cli-${GENERATOR_VERSION}.jar`
    console.log(`Downloading openapi-generator ${GENERATOR_VERSION}...`)
    const response = await fetch(url)
    if (!response.ok) throw new Error(`Download failed (${response.status}): ${url}`)
    await Bun.write(GENERATOR_JAR, response)
  }
  const result = await $`java -jar ${GENERATOR_JAR} ${args}`.nothrow().quiet()
  if (result.exitCode !== 0) {
    console.error(result.stdout.toString(), result.stderr.toString())
    throw new Error(`openapi-generator failed with exit code ${result.exitCode}`)
  }
}

async function listFiles(dir: string): Promise<string[]> {
  const entries = await readdir(dir, { recursive: true, withFileTypes: true }).catch(() => [])
  return entries.filter((entry) => entry.isFile()).map((entry) => relative(dir, join(entry.parentPath, entry.name))).sort()
}

async function compareDirs(expected: string, actual: string): Promise<string[]> {
  const [a, b] = [await listFiles(expected), await listFiles(actual)]
  const differences = [
    ...a.filter((file) => !b.includes(file)).map((file) => `stale: ${file}`),
    ...b.filter((file) => !a.includes(file)).map((file) => `missing: ${file}`),
  ]
  for (const file of a.filter((file) => b.includes(file))) {
    const [left, right] = await Promise.all([Bun.file(join(expected, file)).text(), Bun.file(join(actual, file)).text()])
    if (left !== right) differences.push(`changed: ${file}`)
  }
  return differences
}
