---
name: android-runtime-verification
description: Mandatory runtime verification after behavior or UI changes under opencode-mobile/android; builds and installs the debug app on an emulator, exercises the exact screen against a real OpenCode server, inspects rendering and Logcat, and reports concrete evidence.
---

# Android Runtime Verification

A successful build is necessary but not sufficient for behavior or visual
changes. Match verification depth to risk.

## Workflow

1. Run the module tests required by `android/AGENTS.md`.
2. Use a running emulator (`adb devices` reports its serial) and set
   `adb -s <serial> reverse tcp:4096 tcp:4096`.
3. Make sure an OpenCode server listens on host port 4096
   (`opencode api get /api/info`).
4. From `android/`: `ANDROID_SERIAL=<serial> ./gradlew :app:installDebug`
   (application ID `io.github.beppe2880.opencodemobile.debug`).
5. Bring the existing task forward without force-stopping:
   `adb -s <serial> shell am start -n io.github.beppe2880.opencodemobile.debug/io.github.beppe2880.opencodemobile.MainActivity`.
6. If no server is saved, connect to `http://localhost:4096` by pairing: create a
   code with `opencode pair --url http://localhost:4096` or `opencode api post /api/pair`,
   and type it with `adb shell input text` — never print it in reports or
   screenshots.
7. Navigate by stable testTag/resource ID with Mobile MCP, Maestro or ADB.
8. Exercise the changed interaction and inspect the rendered result in light and
   dark theme; check font scale 1.3 and keyboard-open states for layout changes.
9. Check Logcat after the interaction:
   `adb -s <serial> logcat -d -v threadtime AndroidRuntime:E '*:S'`.

Do not send prompts in real user sessions. To test prompting, create a session in a
disposable scratch directory with a cheap model (for example
`anthropic/claude-haiku-5-5`) and a trivial prompt.

## Verification Depth

- Visual token/icon/spacing: load and inspect the exact state.
- Interaction/state: perform the action and verify resulting State/UI.
- Navigation: traverse entry and return paths.
- Streaming: watch text, reasoning and tool rows update live, then reconcile.
- Connection: pairing, wrong password, server stopped, reconnect.
- Insets/layout: system bars, scrolling, keyboard open, sheet/dialog.

If verification is impossible, name the blocker and separate verified facts from
assumptions.

## Final Evidence

Report the exact checks, emulator serial, server version, screen, interaction,
screenshots taken (without secrets) and any state not verified.
