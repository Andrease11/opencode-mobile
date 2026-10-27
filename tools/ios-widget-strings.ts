// Generates the String Catalog of the iOS widget (iosApp/SessionsWidget/Localizable.xcstrings)
// from the Android widget strings, so translations live in one place.
//   bun tools/ios-widget-strings.ts          write the catalog
//   bun tools/ios-widget-strings.ts --check  fail when it is stale
import { resolve } from "node:path"

const root = resolve(import.meta.dir, "..")
const res = `${root}/android/app/src/main/res`
export const catalogPath = `${root}/iosApp/SessionsWidget/Localizable.xcstrings`

// Android resource folder suffix -> Apple language code.
const locales: Record<string, string> = {
  "": "en", it: "it", es: "es", fr: "fr", de: "de", "pt-rBR": "pt-BR", ru: "ru",
  "b+zh+Hans": "zh-Hans", ja: "ja", ko: "ko", ar: "ar",
}

const strings = [
  "widget_label", "widget_description", "widget_new_session", "widget_not_connected", "widget_no_server",
  "widget_empty", "widget_offline", "widget_session_running", "widget_untitled",
]
const plurals = ["widget_running"]

// Android escapes and positional placeholders -> Apple format specifiers.
function apple(text: string): string {
  return text
    .trim()
    .replace(/\\(['"@?])/g, "$1")
    .replace(/\\n/g, "\n")
    .replace(/%(\d+)\$s/g, "%$1$@")
    .replace(/%(\d+)\$d/g, "%$1$lld")
}

function parse(xml: string) {
  const values = new Map<string, string>()
  for (const [, name, body] of xml.matchAll(/<string\s+name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g)) values.set(name, body)
  const plural = new Map<string, Map<string, string>>()
  for (const [, name, body] of xml.matchAll(/<plurals\s+name="([^"]+)"[^>]*>([\s\S]*?)<\/plurals>/g)) {
    const items = new Map<string, string>()
    for (const [, quantity, text] of body.matchAll(/<item\s+quantity="([^"]+)">([\s\S]*?)<\/item>/g)) items.set(quantity, text)
    plural.set(name, items)
  }
  return { values, plural }
}

const unit = (value: string) => ({ stringUnit: { state: "translated", value } })

// "%1$s · %2$d running": the server name stays outside, the count varies by plural.
const PREFIX = "%1$s · "

function pluralLocalization(items: Map<string, string>) {
  const variations: Record<string, unknown> = {}
  for (const [quantity, text] of items) {
    if (!text.trim().startsWith(PREFIX)) throw new Error(`Plural item without "${PREFIX}": ${text}`)
    variations[quantity] = unit(apple(text.trim().slice(PREFIX.length)).replace("%2$lld", "%arg"))
  }
  return {
    ...unit("%1$@ · %#@count@"),
    substitutions: { count: { argNum: 2, formatSpecifier: "lld", variations: { plural: variations } } },
  }
}

export async function generate(): Promise<string> {
  const entries: Record<string, { extractionState: string; localizations: Record<string, unknown> }> = {}
  for (const key of [...strings, ...plurals]) entries[key] = { extractionState: "manual", localizations: {} }
  for (const [folder, language] of Object.entries(locales)) {
    const file = Bun.file(`${res}/values${folder ? `-${folder}` : ""}/strings.xml`)
    const { values, plural } = parse(await file.text())
    for (const key of strings) {
      const value = values.get(key)
      if (value !== undefined) entries[key].localizations[language] = unit(apple(value))
    }
    for (const key of plurals) {
      const items = plural.get(key)
      if (items) entries[key].localizations[language] = pluralLocalization(items)
    }
  }
  return JSON.stringify({ sourceLanguage: "en", strings: entries, version: "1.0" }, null, 2) + "\n"
}

if (import.meta.main) {
  const text = await generate()
  if (process.argv.includes("--check")) {
    const current = await Bun.file(catalogPath).text().catch(() => "")
    if (current !== text) {
      console.error("iosApp/SessionsWidget/Localizable.xcstrings is stale: run bun tools/ios-widget-strings.ts")
      process.exit(1)
    }
  } else {
    await Bun.write(catalogPath, text)
    console.log(`Wrote ${catalogPath}`)
  }
}
