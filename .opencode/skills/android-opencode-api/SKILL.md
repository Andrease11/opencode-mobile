---
name: android-opencode-api
description: Use for the generated OpenCode V2 Kotlin client in opencode-mobile/android/core/api, the Ktor HTTP client, Basic auth transport, the /api/event SSE stream, mapping generated models to domain models, or regenerating the client with tools/api-gen (bun run generate).
---

# Android OpenCode API

The OpenCode server owns the contract. The app consumes a generated client and
maps it to domain models at one boundary.

## Ownership

```text
feature ViewModel
  -> repository interface (core:domain)
  -> repository implementation (core:data)
  -> OpenCode client + mappers (core:network)
  -> generated Ktor APIs and models (core:api, Kotlin Multiplatform)
```

- `core:api` is produced by openapi-generator 7.22 (`kotlin`, `kotlinx_serialization`)
  from `api/openapi.json` plus event schemas from npm `@opencode/schema`, in two passes:
  models from `jvm-retrofit2` (sealed union serializers, plain kotlinx-serialization code)
  and API classes plus their Ktor infrastructure from `multiplatform`. Everything lives in
  `commonMain`, is committed and never edited by hand.
- `core:network` (Kotlin Multiplatform) owns Ktor client construction (OkHttp engine on
  Android, Darwin on iOS), authentication headers, SSE reading, error mapping and
  generated-to-domain mappers.
- Features never see generated types, Ktor or HTTP status codes.

## Regenerating

```bash
cd tools/api-gen
bun run refresh   # new server version: download spec, pin @opencode/schema, regenerate
bun run generate  # regenerate from committed spec
bun run check     # CI/verification: committed output is current
```

- Need a new API area? Add its tag to `KEEP_TAGS` in
  `tools/api-gen/src/prepare-spec.ts` and regenerate.
- A generator limitation is fixed by a minimal, commented preprocessing step in
  `prepare-spec.ts`, never by editing generated files.
- After regenerating, build `:core:network` and `:core:data` and run their tests;
  fixture tests catch contract drift.

Read `references/generator.md` before changing preprocessing or generator flags.

## Generated Shapes

- Responses usually wrap payloads: `{ data: ... }`, lists add `cursor`.
- REST unions (for example `SessionMessageInfo`, assistant content, tool state)
  are sealed interfaces of `*Value` wrappers holding the concrete class.
- The event union is `OpenCodeEvent` with `*Wrapper` members keyed by the wire
  `type` (`session.text.delta` -> `SessiontextdeltaWrapper`).
- Location-scoped endpoints take `locationDirectory` (`location[directory]`).
- Message pages are limited to 200 items; follow `cursor.next`.
- Enums use `enumUnknownDefaultCase=false`: decoding fails loudly on unknown
  discriminator values, so union decoding stays unambiguous. Map defensively in
  `core:network` and drop unknown event types.

## Transport Rules

- One Ktor client per active server, derived from a shared base client (one engine and
  connection pool); authentication is HTTP Basic `opencode:<secret>` sent as a default
  header. Bearer is not accepted.
- Generated calls return `HttpResponse<T>` and never throw on HTTP status; wrap them in
  `awaitBody` / `awaitSuccess`. Message and session pages are decoded by
  `TolerantPagesConverter`, which skips items this client cannot decode.
- `/api/event` is a never-ending SSE stream. The server sends a `:` heartbeat comment
  every 15 s, so the stream client uses a 45 s read timeout to detect dead connections;
  the call ends with the collector and comments are ignored. A 401 sets
  `ConnectionState.Unauthorized` and stops retrying until the server or credentials change.
- Debug logging must redact `Authorization` and never log request bodies (they
  contain prompts) or pairing responses.
- Map failures to `OpenCodeError` at the network boundary: 401 -> Unauthorized,
  IO -> Unreachable, pairing 4xx -> PairingFailed, otherwise Server with `_tag`.

For credentials and pairing load `android-auth-session`; for stream lifecycle
load `android-coroutines`.
