import { expect, test } from "bun:test"
import { catalogPath, generate } from "./ios-widget-strings"

test("the iOS widget String Catalog matches the Android widget strings", async () => {
  expect(await Bun.file(catalogPath).text(), "run bun tools/ios-widget-strings.ts").toBe(await generate())
})
