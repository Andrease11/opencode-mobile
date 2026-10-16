---
name: android-compose-mvi
description: Use when writing or reviewing opencode-mobile Jetpack Compose UI, MVI State/Event/Effect contracts, BaseViewModel, route/screen ownership, navigation effects, the Oc* design system and OcTheme tokens, streaming chat rendering, insets, or IME.
---

# Android Compose And MVI

Give each state value one lowest responsible owner. Keep application wiring at
the route/host boundary and rendering in plain, previewable composables.

## Route And Screen Split

A thin `XxxRoute` composable may:

- Resolve a ViewModel through Koin.
- Collect `state` with `collectAsStateWithLifecycle()`.
- Collect effects through `CollectEffect`.
- Translate navigation, permission, activity-result, and environment callbacks.

It must not contain business rules. A screen/content composable receives
immutable `State`, an `onEvent` callback, and UI/environment callbacks when
needed. It may own genuinely local framework state such as scroll, focus,
animation, sheet state, or an interaction source.

```kotlin
val viewModel: SessionsViewModel = koinViewModel()
val state by viewModel.state.collectAsStateWithLifecycle()

CollectEffect(viewModel.effect) { effect -> /* navigation or transient UI */ }
SessionsScreen(state = state, onEvent = viewModel::onEvent)
```

Do not pass ViewModels, repositories, network clients, NavController, or Koin
through the rendering tree.

## MVI Contract

- Define `Event`, immutable `State`, and one-shot `Effect` types.
- New ViewModels extend `BaseViewModel<State, Event, Effect>` and expose one
  `onEvent` entry point.
- Use `updateState`, `currentState`, `sendEffect`, and `viewModelScope`.
- Keep durable truth in State: data, validation, loading, saved outcomes, and
  errors that must survive recreation or recollection.
- Use Effect only for transient imperative commands: navigation, toast,
  snackbar, focus, permission, picker, or external activity launch.
- Never encode business truth exclusively as an Effect.
- Preserve existing content during refresh; reserve full-screen loading for the
  initial load.

## Compose Effects

- Use `LaunchedEffect` for suspend work tied to composition and semantic keys.
- Use `DisposableEffect` only when registering a resource that needs cleanup.
- Use `SideEffect` only to publish successful composition state externally.
- Use `rememberCoroutineScope` for UI-triggered work owned by that composable,
  not to move repository business work into UI.
- Use `rememberUpdatedState` only when a long-lived effect needs current values
  without restarting.
- Clean up listeners, players, sensors, launchers, and controllers at their
  actual lifecycle owner.

## Design System And Semantics

- Prefer existing `Oc*` components from `core:ui`; inspect their APIs before
  rebuilding a Material equivalent. `docs/design-system.md` lists tokens and
  components.
- Colors come from `OcTheme` (OpenCode V2 tokens: backgrounds, text, borders,
  state and agent colors) or the derived `MaterialTheme.colorScheme`; never
  hard-code colors in features.
- Typography: Inter for UI text, IBM Plex Mono for code, tool names, paths and
  commands.
- Use the `OcSpacing` scale. Hard-coded dimensions are acceptable for intrinsic
  icon sizes, touch targets and other values that are not spacing tokens.
- Use bottom sheets for pickers and create flows; reserve dialogs for
  confirmation or destructive actions.
- Give interactive controls stable `testTag` values. Use semantics for behavior
  and accessibility; do not rely on visible localized text as automation ID.
- Use stable keys in lazy collections.

## Edge To Edge And IME

- The root app shell owns system insets. A nested `Scaffold` sets
  `contentWindowInsets = WindowInsets(0)` unless it is deliberately the inset
  owner.
- Apply `innerPadding`, then consume it before applying child IME handling.
- Put `imePadding()` or equivalent handling at the lowest container that must
  move above the keyboard. Do not stack it at multiple ancestors.
- Apply list insets through `contentPadding` when content should scroll behind
  system bars rather than padding and clipping the whole list.
- Verify keyboard-open and system-bar states on a real runtime after changes.

## Text Input Visibility

- Use the `Oc*` text field components for single-line and multiline input; the
  chat composer is the reference multiline field.
- Every editable multiline field must have a finite visible height. Keep
  `maxLines` finite and greater than or equal to `minLines` so the field scrolls
  internally and Compose keeps the cursor and newly entered text visible.
- Do not use `Int.MAX_VALUE` for an editable text area's `maxLines` inside a
  scrolling form. An unbounded field grows with old content instead of providing
  a stable viewport for current input.
- Put long fields inside an IME-aware scroll owner. Rely on Compose's automatic
  nested scrolling between `TextField` and the parent; add custom
  `BringIntoViewRequester` logic only when runtime evidence shows the standard
  behavior is insufficient.
- When `minLines` exceeds the default maximum, set an explicit finite
  `maxLines` at least as large as `minLines`.

## Streaming Transcript

- Live events are applied to State with `TranscriptReducer`; composables only
  render the resulting `Message`/`Part` list.
- Key transcript items by message ID and parts by `Part.key`.
- Parse Markdown through the shared parse cache so finished messages are parsed
  once and only the streaming message re-parses.
- Auto-scroll only while the user is at the bottom; otherwise show a
  "jump to latest" control.

## Performance Handoff

Do not add `remember`, `derivedStateOf`, `@Stable`, `@Immutable`, or immutable
collection wrappers as generic cleanup. When recomposition, jank, or stability
is the actual task, load `android-performance`, gather evidence, and return here
only for the resulting UI ownership/edit.

## Verification

- Plain screen composables should be testable from supplied State.
- Verify loading, empty, error, content, and relevant form states.
- For text input changes, type beyond the visible line limit and verify that the
  cursor, latest text, selection, and relevant actions remain visible with the
  keyboard open.
- For other layout changes, inspect keyboard, scrolling, sheet/dialog, and
  affected adaptive states.
- Load `android-localization-accessibility` when copy, semantics, focus, RTL,
  font scale, or adaptive behavior changes.
