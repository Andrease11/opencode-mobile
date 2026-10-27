---
name: ux-runtime-observer
description: Measures an existing or newly implemented opencode-mobile user flow in the real Android runtime, recording steps, expectations, actions, feedback, timing, failures, screenshots, and evidence without deciding the preferred design.
---

# UX Runtime Observer

Gather runtime evidence before UX judgment. This skill observes and measures; it
does not redesign the flow or claim user preferences.

## Platform Routing

- Android: read `android/AGENTS.md`, load `kmp-master` and
  `android-runtime-verification`, use an emulator connected to the local OpenCode
  server, and use
  Mobile MCP or Maestro.
- The official OpenCode web UI on the server port is a reference for comparison,
  not a target to measure.

Reuse the tools and credential workflow already documented by the project.

## Observation Contract

Define before starting:

- persona mode selected by the orchestrator;
- exact goal, entry point, and success state;
- starting account/data state;
- platform and viewport/device;
- states and metrics to capture;
- destructive actions that require safe test data or must not be executed.

## Per-Step Record

```text
Step:
Screen/state:
User intent:
Available choices:
Action:
Expected response:
Actual response:
Feedback:
Wait:
Backtracking:
Result:
Evidence:
```

Count screens, actions, decisions, manual inputs, context switches, waits,
scroll, backtracking, recovery, and time to value. Time only a repeatable run;
otherwise mark it estimated or unavailable. Use screenshots for material visual
states, not every pointer movement.

## Required Checks

- Navigate from the real entry point rather than opening only the destination
  unless direct-entry behavior is the thing under test.
- Exercise the happy path and one representative error or recovery path when
  safe and relevant.
- Inspect crashes and Logcat on Android.
- Check phone and wide layouts when layout can change.
- Record unavailable states instead of inventing them.

## Output

Return a neutral flow map, metric table, evidence paths, code-versus-runtime
discrepancies, and unobserved states. Do not assign preference or visual taste.
Hand the evidence to `ux-flow-audit`, `ux-user-panel`, or `ui-quality-review` as
appropriate.

Use `.opencode/skills/product-ux-orchestrator/references/evidence-and-metrics.md`
for labels and metric definitions.
