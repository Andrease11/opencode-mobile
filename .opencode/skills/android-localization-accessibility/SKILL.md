---
name: android-localization-accessibility
description: Use when changing opencode-mobile user-visible strings (Compose Multiplatform resources, English default, Italian values-it), plurals, dates, numbers, units, content descriptions, semantics, TalkBack, focus, RTL, font scale, adaptive layout, window sizes, or accessibility tests.
---

# Android Localization, Accessibility, And Adaptive UI

User-visible copy and semantics are release contracts, not decoration. New UI
must work with locale changes, TalkBack, RTL, large fonts, keyboard/focus, and
the window sizes relevant to the feature.

## Localized Content

- Put new user-visible static strings in the owning module's Compose Multiplatform
  resources (`src/commonMain/composeResources/values*/strings.xml`) and use
  `org.jetbrains.compose.resources.stringResource`/`pluralStringResource` with the
  module's generated `Res` (`core:ui` strings via `...core.ui.resources.Res as UiRes`).
  Outside composition (effects, snackbars) use `UiText.Resource` and the suspend
  `asString()`/`getString`. Only `:app` notification text stays in Android `res/`.
- Compose resources differ from Android: placeholders must be positional (`%1$s`,
  `%2$d`; `$s` and `$d` are interchangeable and nothing else is substituted, so
  pre-format decimals in code); write `'`, `"`, `@`, `?` and `%` unescaped; `\n`,
  `\t` and `\uXXXX` still work. Locale folders keep Android names (`values-pt-rBR`,
  `values-b+zh+Hans`).
- Do not localize `testTag`, route identifiers, server enum values, agent or
  model IDs, tool names, storage keys, or protocol fields.
- Every new string gets an English default and an Italian translation.
- Map server/exception details to safe user-facing resource messages; keep the
  diagnostic cause separate.
- Use locale-aware date, time, number, decimal, percentage, currency, and unit
  formatting through the `core:ui` helpers (`formatInteger`, `relativeTime`, platform
  date formatting actuals). Do not concatenate units or implement plurals with
  string logic.
- Notification/channel text is user-visible and follows the same rules.
- Migrate existing hard-coded strings incrementally in the screen being changed;
  do not block a feature on a repository-wide translation migration.

## Semantics And Interaction

- Prefer native Material semantics, then add only missing role, selected/state
  description, heading, error, live-region, traversal, or custom-action data.
- Decorative icons/images use `contentDescription = null`; meaningful icon-only
  actions have a localized accessible name.
- Avoid nested independently clickable controls that announce or execute the
  same action twice.
- Keep touch targets at least 48 dp unless a documented platform exception
  applies.
- Error, loading, selected, expanded, checked, and progress states must be
  discoverable without color alone.
- Keep keyboard and D-pad focus order predictable; focus requests belong to the
  UI owner and use a justified Compose effect lifecycle.
- Editable multiline fields must use a finite viewport and remain internally
  scrollable so the cursor, selection, and newly entered text stay visible with
  the IME open. Test by entering more lines than the visible limit, not only by
  focusing a short value.

Automation identity and accessibility are separate: every interactive control
still receives a stable `testTag`, while tests assert semantics and behavior.

## Adaptive Layout

- Define what compact, medium, and expanded mean for the affected screen before
  adding raw width checks.
- Prefer constraints/window-size information at a layout owner; do not scatter
  device-model or orientation checks through leaves.
- Preserve readable content widths, reachable actions, list/detail ownership,
  system insets, and navigation behavior across resizing and rotation.
- Avoid adopting Navigation 3 or SDK 37 alpha APIs as an incidental adaptive fix.

## Verification

`bun test tools/locales.test.ts` (repository root) checks keys, placeholders and
escaping in every locale. On iOS, VoiceOver uses accessibility labels (test tags are
not exposed); check shared UI changes on the simulator as well (`kmp-ios`).

Check the affected UI with TalkBack semantics, 1.5x font scale, RTL where layout
direction matters, compact and expanded widths, keyboard/focus behavior, and
light/dark contrast when colors change. Add focused Compose tests for semantics
and callbacks; use screenshots only for visual contracts.
