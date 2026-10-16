// Adds the OpenCode server event schemas to the REST OpenAPI document.
//
// The server types `/api/event` as an opaque string, so event payloads come from the
// official `@opencode/schema` package (pinned to the server version in package.json),
// converted from Effect Schema to JSON Schema.
import { Schema } from "effect"
import { EventManifest } from "@opencode/schema/event-manifest"
import { ServerEvent } from "@opencode/schema/server-event"

/** Adds the event definitions to [spec] in place and returns how many were added. */
export function augmentEvents(spec: any): number {
  const components = spec.components.schemas

  const union = Schema.Union([...(EventManifest.ServerDefinitions as any), ServerEvent.Connected])
  const doc: any = Schema.toJsonSchemaDocument(union)

  // Event definitions are named by wire type ("session.text.delta"); give them class-friendly names.
  const pascal = (s: string) =>
    s.split(/[^A-Za-z0-9]+/).filter(Boolean).map((p) => p[0].toUpperCase() + p.slice(1)).join("")
  const rename = new Map<string, string>()
  for (const name of Object.keys(doc.definitions)) if (/^[a-z]/.test(name)) rename.set(name, `${pascal(name)}Event`)

  const relink = (node: unknown) =>
    JSON.parse(
      JSON.stringify(node).replace(/"#\/\$defs\/([^"]+)"/g, (_m, ref) => `"#/components/schemas/${rename.get(ref) ?? ref}"`),
    )

  let added = 0
  for (const [name, definition] of Object.entries(doc.definitions)) {
    const target = rename.get(name) ?? name
    if (target in components) continue
    components[target] = relink(definition)
    added++
  }
  components.OpenCodeEvent = relink(doc.schema)

  // The server sends `server.connected` without `created`, unlike its schema.
  const connected = components.ServerConnectedEvent
  if (connected?.required) connected.required = connected.required.filter((key: string) => key !== "created")

  spec.paths["/api/event"].get.responses["200"].content["text/event-stream"].schema = {
    $ref: "#/components/schemas/OpenCodeEvent",
  }

  // The server repeats the `session` tag, which fails generator validation.
  if (Array.isArray(spec.tags)) {
    const seen = new Set<string>()
    spec.tags = spec.tags.filter((tag: any) => !seen.has(tag.name) && seen.add(tag.name))
  }

  return added
}
