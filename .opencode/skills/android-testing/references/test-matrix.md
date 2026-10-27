# Android Test Matrix

| Change | Minimum focused evidence |
|---|---|
| Generated API regeneration | `bun run check` (tools/api-gen), `:core:network` fixture tests |
| Pure logic (reducer, parser, formatting) | `commonTest` with `kotlin.test` |
| Mapping / error mapping | `core:network` unit test with fixture or MockWebServer |
| Event handling / transcript | `TranscriptReducer` replay of a recorded `.sse` log |
| Repository behavior | `core:data` test with fake client or MockWebServer |
| ViewModel / business rule | deterministic test with fake repositories |
| Coroutine race / reconnect | `runTest` with virtual time and cancellation assertion |
| Navigation / back | route behavior test or runtime traversal |
| Koin wiring | `:app:assembleDebug` + runtime start; module verification when configured |
| Semantics / accessibility | Compose behavior test plus runtime inspection |
| Camera / clipboard / permission | device verification on the emulator and the iOS simulator |
| iOS-only actual | `compileKotlinIosSimulatorArm64` plus simulator run (`kmp-ios`) |

Commands (from `android/`):

```bash
./gradlew :core:network:testDebugUnitTest
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:chat:testDebugUnitTest
./gradlew testDebugUnitTest            # all modules (KMP: alias of testAndroidHostTest)
./gradlew :core:domain:iosSimulatorArm64Test   # commonTest on the iOS simulator, macOS only
```

CI runs all unit tests, `:app:lintDebug`, `:app:assembleDebug`, the iOS klib compilation,
an Xcode simulator build on macOS (pull requests only when they touch `iosApp/`, `iosMain`
or the Gradle setup; always on `main`), plus the API freshness check (`bun run check` in
`tools/api-gen`). `commonTest` runs on the JVM in CI; run it on the simulator locally after
iOS-specific changes.
