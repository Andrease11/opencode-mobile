---
name: product-ux-orchestrator
description: Entry skill for planning, optimizing, or reviewing user flows across the opencode-mobile Android app; asks how the user wants personas handled, routes to the smallest UX specialist set, and tracks plan versus implementation and runtime evidence.
---

# Product UX Orchestrator

Use this as the router for product-flow work. It owns scope, ordering, approval
gates, and completion status. It does not replace the specialist skills or the
subsystem implementation skills.

## Intake

Resolve these from the request and code before asking questions:

- feature and user goal;
- entry point and success state;
- phone, foldable/tablet, or both;
- task frequency and stakes;
- whether the user supplied a target flow or persona.

Ask only for information that changes the outcome. Batch ordinary scope gaps
into one concise question.

## User Perspective Gate

Never run a multi-persona simulation implicitly. If the request has not already
selected a perspective, ask exactly one question:

> How should I handle the user perspective: follow your decided direction,
> evaluate one target user, simulate multiple users, stay neutral and report
> observable friction only, or choose the smallest useful approach for you?

Interpret the answer as one of:

- `user-defined`: preserve the requested direction; identify blockers and
  tradeoffs without reopening product choices unnecessarily.
- `single-persona`: load `ux-user-panel` for one named or inferred persona.
- `multi-persona`: load `ux-user-panel` only after this explicit approval.
- `neutral`: do not predict preferences; report evidence and alternatives.
- `agent-selected`: choose the smallest useful mode and state why.

Do not ask when the prompt already answers the gate. Record the selected mode in
the plan so later verification uses the same criterion.

## Specialist Routing

- Load `ux-runtime-observer` to inspect an existing flow or verify a built flow.
- Load `ux-flow-audit` to diagnose interaction cost, feedback, recovery, state
  coverage, and unnecessary steps.
- Load `ux-user-panel` only for an approved single- or multi-persona prediction.
- Load `ux-information-architecture` only for findability, navigation, labels,
  grouping, hierarchy, or first-click questions.
- Load `ui-quality-review` after the flow is stable when rendered visual quality
  is in scope.
- Load `ux-implementation-review` during delivery and before completion.

For implementation, also load the owning project skills: `android-master` and its
specialists for `android/`. The OpenCode server is external; flows that need a
server change are out of scope and must be reported as constraints.

## Workflow For A New Flow

1. Resolve intake and the User Perspective Gate.
2. Inspect comparable product behavior and technical constraints.
3. Define current assumptions, non-goals, entry, success, cancellation, and
   recovery.
4. Run the approved user perspective analysis, if any.
5. Load the conditional IA review only when its trigger is present.
6. Produce current or baseline, safe, recommended, and power-user alternatives
   when those alternatives are meaningful.
7. Run `ux-flow-audit` on the proposed flow.
8. Stop for approval when the user is shaping the flow; do not hide a product
   decision inside implementation.
9. Create an implementation contract and hand off to subsystem skills.
10. Run implementation, runtime, and visual verification in that order.

## Workflow For An Existing Flow

1. Resolve intake and the User Perspective Gate.
2. Use `ux-runtime-observer` to exercise the current flow before judging it.
3. Use `ux-flow-audit` to produce evidence-backed findings.
4. Run only the approved persona analysis.
5. Compare alternatives and obtain approval before structural edits.
6. Implement through owning subsystem skills.
7. Re-run the same runtime scenario.
8. Use `ux-implementation-review` to compare plan, code, and observed result.

## Persistent Plan

For substantial work, write `plan-<feature>-ux.md` at the repository root when
the user asks for a plan, implementation begins, or a stable handoff is needed.
Use `references/plan-template.md`. Do not create a document for a quick verbal
review. Update the existing plan rather than creating competing reports.

## Completion Rule

`Complete` requires planned behavior implemented and runtime verified on every
in-scope platform. Compilation alone is `Statically verified`, not complete.
Report missing, blocked, intentionally divergent, and unverified items without
changing the plan to make delivery appear finished.

Read `references/evidence-and-metrics.md` whenever producing scores, preference
claims, before/after comparisons, or a completion verdict.

See `references/sources.md` for the external methods adapted into this project
and the constraints deliberately rejected.
