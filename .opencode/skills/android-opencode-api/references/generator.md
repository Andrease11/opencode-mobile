# Generator Pipeline

`tools/api-gen/src/generate.ts` (`bun run generate`) runs these steps in a temporary directory:

1. `tools/api-gen/src/augment-events.ts`
   - Converts `EventManifest.ServerDefinitions` + `ServerEvent.Connected` from
     `@opencode/schema` (Effect Schema) to JSON Schema with
     `Schema.toJsonSchemaDocument`.
   - Adds the definitions to `components.schemas`, renaming wire-named events
     (`session.text.delta`) to `SessionTextDeltaEvent`, and adds `OpenCodeEvent`.
   - Types the `/api/event` stream with `OpenCodeEvent`.
   - Patches known server/schema mismatches (`server.connected` has no `created`).
   - Deduplicates repeated tags (generator validation fails otherwise).
2. `tools/api-gen/src/prepare-spec.ts`
   - `anyOf` unions whose members share a constant (`type`, `status`, `_tag`,
     `kind`) become `oneOf` + `discriminator`, so the generator emits sealed
     interfaces instead of one flattened class with every field required.
   - Removes `additionalProperties: true` from structs (Effect default) so models
     are data classes, not `HashMap` subclasses.
   - Mixed primitive unions (`number | false`) become free-form JSON; the Kotlin
     generator cannot serialize them. Unions mixing primitives and primitive arrays
     count too, and already converted nested unions are marked `x-raw-json`, because
     the walk is post-order: without that, `Form.Value` silently lost `number`.
     Form answers are therefore `JsonElement` (mapped in `FormMapping.kt`).
   - Flattens deepObject query objects to `location[directory]` scalars.
   - Keeps only `KEEP_TAGS` operations and reachable schemas.
3. openapi-generator flags that matter:
   - `library=jvm-retrofit2` + `generateOneOfAnyOfWrappers=true`: only this
     combination generates kotlinx union serializers.
   - `enumPropertyNaming=UPPERCASE`: an enum constant named `context` breaks
     Kotlin 2.2 parsing (context parameters).
   - Type mappings: `integer=Long` (millisecond timestamps), `number=Double`,
     free-form `object=JsonObject`, `AnyType=JsonElement`.

Validated against OpenCode 2.0.27: every message type and 317/318 recorded events
decode; the remaining one was fixed by the `server.connected` patch.

When upgrading the generator, regenerate, compile `:core:api`, then run the
fixture tests in `core:network`.
