---
name: android-opencode-api
description: Use for the generated OpenCode V2 Kotlin client in opencode-mobile/android/core/api, Retrofit services, Basic auth transport, the /api/event SSE stream, mapping generated models to domain models, or regenerating the client with tools/api-gen (bun run generate).
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
  -> generated Retrofit APIs and models (core:api)
```

- `core:api` is produced by openapi-generator 7.22 (`kotlin`, `jvm-retrofit2`,
  `kotlinx_serialization`) from `api/openapi.json` plus event schemas from npm
  `@opencode/schema`. It is committed and never edited by hand.
- `core:network` owns OkHttp/Retrofit construction, authentication headers, SSE
  reading, error mapping and generated-to-domain mappers.
- Features never see generated types, Retrofit, OkHttp or HTTP status codes.

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

- One OkHttp client per active server; authentication is HTTP Basic
  `opencode:<secret>` added by an interceptor. Bearer is not accepted.
- `/api/event` is a never-ending SSE stream. The server sends a `:` heartbeat comment
  every 15 s, so the stream client uses a 45 s read timeout to detect dead connections;
  cancel the call on collector cancellation and ignore the comments. A 401 sets
  `ConnectionState.Unauthorized` and stops retrying until the server or credentials change.
- Debug logging must redact `Authorization` and never log request bodies (they
  contain prompts) or pairing responses.
- Map failures to `OpenCodeError` at the network boundary: 401 -> Unauthorized,
  IO -> Unreachable, pairing 4xx -> PairingFailed, otherwise Server with `_tag`.

For credentials and pairing load `android-auth-session`; for stream lifecycle
load `android-coroutines`.
