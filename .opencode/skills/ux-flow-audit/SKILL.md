---
name: ux-flow-audit
description: Evidence-based audit of an existing or proposed opencode-mobile task flow; diagnoses interaction cost, cognitive load, forms, feedback, state coverage, errors, recovery, and unnecessary steps without assuming fewer clicks are always better.
---

# UX Flow Audit

Audit one defined task from entry to success. Existing flows require runtime
evidence from `ux-runtime-observer` when feasible. Proposed flows may be audited
as designs, with runtime claims clearly unavailable.

## Scope Gate

Confirm goal, entry, success, platform, audience mode, frequency, stakes, and
available evidence. Do not silently widen a flow review into a product-wide
audit. Audit before editing when the request asks for both review and fixes.

## Evaluation Spine

For every step assess:

- information scent and clarity of the next action;
- number and ambiguity of decisions;
- recognition versus recall;
- whether data can be prefilled, remembered, inferred, or removed;
- whether a separate screen or confirmation is justified;
- feedback, progress, success, and perceived waiting;
- error prevention, preservation of input, recovery, cancel, back, and undo;
- loading, empty, sparse, error, offline/interruption, and overflow states as
  relevant;
- first-time discoverability and repeat-use efficiency;
- consistency with platform and existing product conventions;
- accessibility as ability to complete the task, not a late checklist;
- trust and intentional friction for sensitive or irreversible actions.

Use Krug and Nielsen as diagnostic prompts, not as reasons by themselves. Every
finding must name the user consequence. Do not penalize expert density, required
regulatory input, or safety steps merely because they increase counts.

## Step Challenge

Ask of every step:

1. Must it exist?
2. Does the system already know the information?
3. Can it remain inline or in the current context?
4. Can a sensible default or a remembered choice remove work?
5. Can a single-choice state auto-select safely?
6. Is confirmation proportional to consequence?
7. Would recent items, favorites, autocomplete, bulk action, deep link, or a
   shortcut improve repeat use?
8. Does removing the step increase ambiguity, error risk, or cognitive load?

## Alternatives

When optimization is requested, produce only meaningful alternatives:

- current or baseline flow;
- safe optimization with minimal structural change;
- recommended flow balancing clarity and efficiency;
- power-user path when the task is frequent enough to justify one.

Compare alternatives with the shared Interaction Cost rubric. Show counted
events; do not present a bare score or fabricated completion-time percentage.

## Finding Schema

```text
ID: FLOW-<number>
Severity: Blocker | High | Medium | Low
Confidence:
Location/step:
Evidence:
User consequence:
Recommendation:
Tradeoff:
Effort: S | M | L
```

Blocker means the core task cannot be completed or risks irreversible loss.
High means significant failure or hesitation on the core path. Medium costs
time or quality but remains recoverable. Low is real polish, not taste disguised
as severity.

## Output

Provide scope/evidence, current flow map, metrics, highest-impact findings,
alternatives, recommended flow, preserved strengths, open hypotheses, and what
needs analytics or user research. For substantial implementation work, update
the existing `plan-<feature>-ux.md` rather than creating a competing report.

Read `.opencode/skills/product-ux-orchestrator/references/evidence-and-metrics.md`.
