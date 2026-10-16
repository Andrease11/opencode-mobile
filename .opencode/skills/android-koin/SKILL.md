---
name: android-koin
description: Use for opencode-mobile Koin 4.2 dependency registration, constructor injection, scopes, runtime parameters, exact binding types, dataModule and appModule wiring.
---

# Android Koin

Follow the project's Koin DSL. Do not migrate to annotations or a compiler plugin
during unrelated work.

## Registration Ownership

- `core/data/.../di/DataModule.kt` (`dataModule`) registers repositories, the
  network client cache, the shared event stream and the app-scoped
  `CoroutineScope`.
- `app/.../di/AppModule.kt` (`appModule`) registers ViewModels.
- `OpenCodeApplication` starts Koin with `dataModule + appModule`.
- Use `viewModelOf(::Type)` for ordinary constructor injection and
  `viewModel { params -> Type(params.get(), get()) }` for runtime parameters such
  as a session ID.
- Use `single { }` only for intentional app-scoped instances; `factory` for
  short-lived objects whose caller owns lifetime.

Bind implementations under the exact interface type consumers request, for
example `single<SessionRepository> { DefaultSessionRepository(get()) }`.

## Consumption

- Resolve route ViewModels with `koinViewModel()` and pass parameters with
  `parametersOf(...)`.
- Use `koinInject()` only for intentionally shared app-scoped infrastructure.
- Do not call `get()` from business code or pass Koin through feature layers.

Read `references/module-verification.md` when adding Koin verification tests.
