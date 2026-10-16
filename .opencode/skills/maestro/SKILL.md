---
name: maestro
description: Use when writing, reviewing, or running black-box Maestro E2E test flows under opencode-mobile/.maestro for the Android app.
---

# Maestro Android E2E

Use Maestro for complete user journeys, not as a substitute for unit or Compose
behavior tests. Read `.maestro/AGENTS.md`, the affected UI testTags, and one
comparable flow before editing.

## Selectors

- Interact through stable Compose `testTag` values exposed as resource IDs.
- Use visible text for assertions only when text content is the behavior under
  test.
- Do not use localized or dynamic text as an interaction selector.
- Do not use coordinates unless there is no semantic/tagged target and the
  limitation is documented.
- When a required interactive control has no testTag, add the tag in the app
  rather than weakening the flow.

## Credentials And State

- Never hardcode server passwords or pairing codes in YAML, scripts, docs,
  screenshots, logs, or responses.
- Connect flows receive a fresh pairing code via a CLI parameter, e.g.
  `-e OC_PAIR_CODE="$(opencode api post /api/pair --data '{}' | jq -r .code)"`,
  and reference `${OC_PAIR_CODE}`; codes are single-use and short-lived.
- Reuse an installed app with a saved server when the journey does not test
  connecting.
- Clear app state only when isolation requires it; it removes saved servers.
- Never capture a screenshot while a password or code field is populated.
- Do not send prompts in real sessions; prompting flows create a session in a
  disposable scratch directory.

## Flow Design

- One flow covers one coherent user journey.
- Extract repeated setup into a small `runFlow` subflow only when it retains
  clear failure assertions.
- Use `appId: io.github.beppe2880.opencodemobile.debug` for debug builds.
- Prefer deterministic setup and cleanup. Existing shared test data may persist,
  so create unique entities or remove only entities created by the flow.
- Use `extendedWaitUntil` for a named observable state after network/async work.
- Never use arbitrary sleeps.
- Hide the keyboard or press back before tapping covered controls.
- Replace existing field content before typing when append behavior is possible.
- Capture screenshots or hierarchy only at useful checkpoints/failure evidence.

## Assertions

Assert user-observable outcomes: destination, changed value, success feedback,
refreshed list, or durable state after relaunch. Avoid implementation-detail
assertions that belong in unit/Compose tests.

## Execution

Use the emulator serial reported by ADB or repository scripts; never hardcode a
serial-to-AVD mapping. Run the smallest relevant flow and keep output under the
gitignored `.maestro/output/` directory.

After failures, inspect the hierarchy, screenshots, server health
(`opencode api get /api/info`), and Logcat
before increasing timeouts.
