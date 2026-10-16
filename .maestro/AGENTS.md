# Maestro E2E Tests

Black-box Android UI tests run with Maestro, used for testing only. Keep changes here
limited to flows, helpers and artifacts; app-side `testTag` changes belong under
`android/`. Load the `maestro` skill before editing.

## Running

```bash
# Debug build installed on an emulator with adb reverse tcp:4096 tcp:4096.
maestro --device <serial> test .maestro/smoke/launch.yaml .maestro/sessions/open-session.yaml

# Connect flow: pass a fresh single-use pairing code, never commit it.
OC_PAIR_CODE="$(opencode api post /api/pair --data '{}' | python3 -c 'import json,sys; print(json.load(sys.stdin)["code"])')"
maestro --device <serial> test -e OC_PAIR_CODE="$OC_PAIR_CODE" .maestro/connect/pair-manual.yaml
```

Output goes to the gitignored `.maestro/output/`. Command reports there contain typed
values, including pairing codes: delete them after debugging.

If Maestro reports "Android driver did not start up in time", another UiAutomator client
(usually Mobile MCP's `com.mobilenext.mobilecli.DeviceServer`) holds the device. Stop it
with `adb shell pkill -f mobilecli` and retry; Mobile MCP restarts it on demand.

## Flows

| Flow | Journey |
|---|---|
| `smoke/launch.yaml` | App starts on Connect or Sessions |
| `connect/pair-manual.yaml` | Clears data, pairs with URL + code, lands on Sessions |
| `sessions/open-session.yaml` | Searches sessions, opens one, returns |

## Rules

- `appId: io.github.beppe2880.opencodemobile.debug`.
- Interact only through `id:` selectors (Compose `testTag` exposed as resource IDs).
  Use text only to assert visible content.
- One flow per user journey; use `extendedWaitUntil` instead of sleeps.
- Never type or screenshot passwords; pairing codes come only from `-e` parameters.
- Do not send prompts in real sessions. Prompting flows create a session in a disposable
  scratch directory and use a cheap model.
