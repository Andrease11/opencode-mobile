# Koin Module Verification

Koin's JVM `Module.verify()` API can catch missing constructor definitions
without starting Android or creating every object. Verify the same composition
the application starts:

```text
dataModule + appModule
```

Do not verify each module only in isolation; cross-module bindings are the main
risk.

Declare runtime-provided types explicitly:

- Android `Context`/`Application`.
- ViewModel injected parameters (for example the chat session ID) through
  verification's injected-parameter declarations.

Keep a runtime smoke check too: verification cannot prove encrypted storage
initialization, camera availability or route-specific parameter values.
