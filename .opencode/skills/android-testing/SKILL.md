---
name: android-testing
description: Use when adding or reviewing opencode-mobile unit tests (commonTest with kotlin.test or JVM androidHostTest), MVI ViewModel tests, repository fakes, recorded API fixtures, SSE/transcript tests, coroutine/Flow tests, Compose behavior tests, or test infrastructure.
---

# Android Testing

Use the narrowest deterministic test that proves the behavior. Unit and Compose
tests cover logic and UI contracts; Maestro covers a few complete journeys.

## Where Tests Live

- **`src/commonTest`** with `kotlin.test` and `kotlinx-coroutines-test` is the default
  for pure logic (reducers, mappers, parsers, formatting, ViewModels with fakes). It
  runs on the JVM with the host tests and on iOS with
  `./gradlew :<module>:iosSimulatorArm64Test` (Mac only). `core:domain` and `core:mvi`
  already test there; add `commonTest.dependencies { implementation(libs.kotlin.test) }`
  to a module the first time.
- **`src/androidHostTest`** (JVM, JUnit 4) for tests that need JVM tooling: OkHttp
  `MockWebServer` transport tests in `core:network`/`core:data` (the Android Ktor engine
  is OkHttp), Robolectric/Room, `MainDispatcherRule` from `core:testing`, classpath
  fixtures. Move such a test to `commonTest` only when it no longer needs them (e.g.
  with Ktor `MockEngine` and inline fixtures); migrate incrementally, never in bulk.
- Recorded, sanitized server fixtures (JSON responses and an `.sse` event log) live in
  `core/testing/src/main/resources` and are read from the JVM classpath.
- Compose UI test APIs for instrumented tests; Maestro for black-box Android flows;
  iOS runtime behavior is verified on the simulator (`kmp-ios`).

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

Run the focused module test first (`./gradlew :<module>:testDebugUnitTest`, an alias of
`testAndroidHostTest`, which also runs `commonTest` on the JVM), then `./gradlew
spotlessCheck testDebugUnitTest :app:lintDebug :app:assembleDebug compileKotlinIosArm64
compileKotlinIosSimulatorArm64` from `android/`. See
`references/test-matrix.md` for risk-based evidence.
