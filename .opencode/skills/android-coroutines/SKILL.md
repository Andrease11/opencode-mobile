---
name: android-coroutines
description: Use for opencode-mobile coroutine scope ownership, the shared /api/event stream and reconnection, suspend APIs, Flow/StateFlow/SharedFlow/Channel, cancellation, dispatchers, background work, runBlocking, or asynchronous error handling.
---

# Android Coroutines And Flow

Async work has one explicit lifecycle owner. Prefer structured concurrency and
suspending APIs over hidden fire-and-forget work.

## Scope Ownership

- `viewModelScope` translates non-suspending UI events into async work.
- `rememberCoroutineScope` is valid for UI work owned by a composable.
- Repositories and data sources normally expose `suspend` or cold `Flow` APIs;
  the caller owns cancellation.
- A stored `CoroutineScope` is a review signal. Keep it only when the class has
  a documented lifecycle, cancellation method, error policy, and restart rules.
- Android services may own a service scope, but cancel it in the corresponding
  lifecycle callback.
- The app-scoped `CoroutineScope` registered in `dataModule` owns the shared
  event stream (`stateIn`/`shareIn` with `WhileSubscribed`). It pauses while the
  app is in the background and restarts when the active server changes.
- Reconnection uses bounded exponential backoff and stops when there are no
  collectors; never reconnect in a tight loop.
- Use WorkManager or a platform service for work that must outlive the current
  UI/process contract; do not simulate durability with an ad-hoc scope.

Never use `GlobalScope`. Never create a new scope per call.

## Cancellation And Errors

- Never swallow `CancellationException`.
- Broad catches around suspend calls must rethrow cancellation before mapping or
  logging other failures.
- Do not use `runCatching` around suspend work unless cancellation is explicitly
  preserved.
- Callbacks from HTTP engines or platform APIs may run in another coroutine or
  thread: bridge them with `channelFlow { send() }` or `callbackFlow`, never
  `flow { emit() }` (Ktor's Darwin engine runs `execute {}` in another coroutine).
- In common code import `kotlinx.coroutines.IO` for `Dispatchers.IO`; Objective-C
  callbacks hop to the main queue before touching UI state.
- Catch at the layer that can add meaning: retry, map to a domain result, update
  UI state, or log with useful non-sensitive context.
- Avoid silently converting unexpected failures to `null` or an empty list.
- Log a failure once at the owning boundary. Preserve the throwable/cause for
  diagnostics but expose a typed, localized-safe message to UI.
- Never log passwords, pairing tokens or codes, Authorization headers, prompts,
  transcripts, tool output, file contents, or raw server bodies.

## Dispatchers

- Inject dispatchers only where tests or ownership genuinely benefit.
- Use `Dispatchers.IO` for blocking file, socket, archive, or legacy API work.
- Use `Dispatchers.Default` for genuinely CPU-heavy work.
- Do not wrap already non-blocking/main-safe suspend APIs in IO reflexively.
- Production `runBlocking`, especially during application startup, requires an
  explicit blocking contract and performance evidence; prefer suspendable or
  asynchronous initialization.

## Flow Modeling

- Never expose `MutableStateFlow` or `MutableSharedFlow` outside its owner.
- Use `StateFlow` for durable observable state.
- Use Flow for streams; use suspend functions for one-shot work.
- Use the project `Channel`-backed Effect only for transient UI commands that
  may be lost when no UI collector exists.
- Durable results and business truth belong in State or persistent storage.
- Collect Android UI state with `collectAsStateWithLifecycle()`.
- Document sharing start/stop behavior when using `stateIn` or `shareIn`.

## Review Checklist

- Who cancels every launched job?
- Can a newer request supersede an older one, and is the old job cancelled?
- Does a stop/release method cancel pending network/file work too?
- Can cancellation be mistaken for a user-visible error?
- Is shared mutable state confined or synchronized without blocking threads?
- Can tests advance virtual time deterministically?
