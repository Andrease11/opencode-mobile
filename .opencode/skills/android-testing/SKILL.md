---
name: android-testing
description: Use when adding or reviewing opencode-mobile unit tests, MVI ViewModel tests, repository fakes, recorded API fixtures, SSE/transcript tests, coroutine/Flow tests, Compose behavior tests, or test infrastructure.
---

# Android Testing

Use the narrowest deterministic test that proves the behavior. Unit and Compose
tests cover logic and UI contracts; Maestro covers a few complete journeys.

## Existing Stack

- JUnit 4, `kotlinx-coroutines-test`, `MainDispatcherRule` from `core:testing`.
- OkHttp `MockWebServer` for transport tests in `core:network` (the Android Ktor engine is OkHttp).
- Recorded, sanitized server fixtures (JSON responses and an `.sse` event log)
  under `src/test/resources` of `core:network`/`core:data`.
- Compose UI test APIs for instrumented tests; Maestro for black-box flows.

Do not add a new framework automatically (Turbine, Robolectric, screenshot
testing, mocking libraries) without a concrete requirement.

## What To Test

- Mapping of generated API models to domain models against fixtures.
- Event decoding and `TranscriptReducer` replay of recorded streams.
- `ConnectLinkParser`, error mapping, auth headers and pairing requests.
- ViewModels: State transitions and Effects with fake repositories.
- Do not unit-test Compose layout declarations, Activities or DI registration.

Prefer fakes over mocks. Use `runTest`, virtual time and `advanceUntilIdle`;
never `Thread.sleep` or real delays.

```text
initial State -> onEvent -> fake response or failure -> State transition -> Effect if transient
```

## Fixtures

- Record fixtures only from scratch sessions in a disposable directory, then
  replace paths with neutral ones (`/home/user/project`), strip reasoning signatures and system
  instructions, and never include secrets, private paths or personal prompts.
- When the server version changes, refresh fixtures and keep the old ones only
  if they cover a compatibility case.

## Flow And Effects

- Assert ordered emissions when sequence matters.
- Start collecting Effects before triggering the event.

## Compose Behavior Tests

- Test stateless screens from supplied State without the Koin graph.
- Every interactive control keeps a stable `testTag`; prefer semantic matchers.
- Cover loading, empty, error, content, streaming and disabled states.

## Verification

Run the focused module test first, then `./gradlew spotlessCheck testDebugUnitTest :app:lintDebug
:app:assembleDebug` from `android/`. See
`references/test-matrix.md` for risk-based evidence.
