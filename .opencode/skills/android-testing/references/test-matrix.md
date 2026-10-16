# Android Test Matrix

| Change | Minimum focused evidence |
|---|---|
| Generated API regeneration | `bun run check` (tools/api-gen), `:core:network` fixture tests |
| Mapping / error mapping | `core:network` unit test with fixture or MockWebServer |
| Event handling / transcript | `TranscriptReducer` replay of a recorded `.sse` log |
| Repository behavior | `core:data` test with fake client or MockWebServer |
| ViewModel / business rule | deterministic test with fake repositories |
| Coroutine race / reconnect | `runTest` with virtual time and cancellation assertion |
| Navigation / back | route behavior test or runtime traversal |
| Koin wiring | `:app:assembleDebug` + runtime start; module verification when configured |
| Semantics / accessibility | Compose behavior test plus runtime inspection |
| Camera / clipboard / permission | device verification on the emulator |

Commands (from `android/`):

```bash
./gradlew :core:network:testDebugUnitTest
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:chat:testDebugUnitTest
./gradlew testDebugUnitTest            # all modules
```

CI runs all unit tests, `:app:lintDebug` and `:app:assembleDebug`, plus the API freshness
check (`bun run check` in `tools/api-gen`).
