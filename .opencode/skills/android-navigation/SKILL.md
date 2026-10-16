---
name: android-navigation
description: Use when adding or changing opencode-mobile navigation: the connect/main graph, type-safe @Serializable routes, Chat(sessionId) arguments, back handling, navigation effects, or adaptive list-detail layout.
---

# Android Navigation

The app uses Navigation Compose 2.9 with `@Serializable` route types. Do not
migrate to Navigation 3 during feature work.

## Navigation Owners

- `:app` owns the root `NavHost`, the route types and the start decision:
  no active server -> Connect, otherwise Sessions.
- Feature modules expose `XxxRoute(...)` composables with navigation callbacks;
  they never receive a `NavController`.
- Screen composables emit `onEvent` only; navigation is a ViewModel Effect or a
  route callback.

## Route Wiring

- Resolve route ViewModels with `koinViewModel()`; pass arguments such as the
  session ID with `parametersOf(...)`.
- Collect State lifecycle-aware and Effects with `CollectEffect` in the route.
- Make "screen shown" events idempotent; a destination may re-enter.

## Back Stack Decisions

- Successful connect replaces Connect with Sessions (pop Connect inclusive).
- Removing or switching the active server to none clears the stack back to Connect.
- Opening a session pushes Chat; creating a session navigates to its Chat and
  removes the creation sheet.
- Settings is pushed from Sessions; Back returns to Sessions.
- Do not use `popUpTo(0)`, `launchSingleTop` or state restoration without naming
  the intended history behavior.

## Adaptive Layout

On expanded widths a list-detail layout may show Sessions and Chat together.
Keep the same route types so phone and wide layouts share ViewModels and back
semantics.

## Verification

Test entry, back from Chat and Settings, connect -> sessions replacement,
server removal, process recreation where relevant, and the wide layout when
touched.
