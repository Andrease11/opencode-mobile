---
name: ux-implementation-review
description: Compares an approved Product UX plan with opencode-mobile code, tests and Android runtime evidence; tracks each requirement as implemented, statically verified, runtime verified, divergent, missing, or blocked before declaring completion.
---

# UX Implementation Review

Use during delivery and before completion. This skill audits conformance to the
approved flow and state contract; it does not rewrite the plan to match whatever
was built.

## Inputs

- the approved `plan-<feature>-ux.md` or explicit conversation plan;
- affected Android code;
- generated contracts where relevant;
- test and build results;
- runtime evidence from `ux-runtime-observer` and platform verification skills.

If no stable plan exists, reconstruct only explicit requirements and label the
rest `No approved baseline`; do not fabricate intended behavior.

## Requirement States

- `Not started`
- `Implemented`
- `Statically verified`
- `Runtime verified`
- `UX verified`
- `Diverged intentionally`
- `Missing`
- `Blocked`

`Complete` requires runtime evidence for every in-scope behavior whose outcome
is user-visible. Build success never upgrades an item beyond `Statically
verified`.

## Review

1. Expand the plan into atomic, testable requirements.
2. Trace each requirement to owning code and platform.
3. Check entry, happy path, loading, empty, validation, error, recovery,
   cancellation/back, success feedback, interruption, and power-user behavior
   that the plan includes.
4. Verify phone/wide-layout parity only where the plan requires it.
5. Match tests and runtime evidence to requirements, not merely files changed.
6. Record unplanned additions and whether they alter the approved flow.
7. Re-run the same baseline scenario after fixes so before/after metrics remain
   comparable.

## Output

Use a matrix:

| Requirement | Code | Static evidence | Runtime evidence | Status | Gap |
|---|---|---|---|---|---|

Then report planned count, implemented count, runtime-verified count, missing,
blocked, intentional divergence, and an overall verdict:

- `Complete`
- `Partial`
- `Blocked`
- `No approved baseline`

Update the existing plan checklist when one exists. Preserve failed or missing
items; never mark them complete based on intent.
