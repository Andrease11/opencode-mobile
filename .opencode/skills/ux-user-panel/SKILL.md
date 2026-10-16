---
name: ux-user-panel
description: Predicts and compares user preferences for a opencode-mobile flow from explicitly approved personas; use only after the user selects one target persona or approves multi-persona simulation, never as a substitute for real user research.
---

# UX User Panel

This skill predicts how defined user perspectives may interpret and prefer a
flow. It does not observe real human behavior.

## Activation Gate

Do not load or run a multi-persona panel unless the user explicitly selected it
or told `product-ux-orchestrator` to choose the approach. If the user already
specified the desired flow, preserve that direction and use a panel only when
asked to challenge or compare it.

For a single-persona request, evaluate only that persona. Do not add a standard
panel for completeness.

## Default Personas

- `first-time`: unfamiliar with product structure and terminology; values clear
  labels, visible next actions, and confidence.
- `returning`: knows the product broadly but not every location; values
  continuity, remembered choices, recent items, and predictable placement.
- `power-user`: repeats the task frequently; values shortcuts, bulk actions,
  keyboard paths, deep links, and minimal repetition.
- `cautious`: prioritizes consequences, reversibility, privacy, and recovery;
  accepts intentional friction when stakes justify it.

Replace these with domain-specific personas when the user supplies them. Do not
invent demographic attributes that do not change task behavior.

## Walkthrough Protocol

For each persona and each material step, answer before interaction:

1. What am I trying to achieve now?
2. Where would I look first?
3. What would I choose and why?
4. What do I expect to happen?
5. What makes me hesitate or requires interpretation?

After interaction or review of the proposed response:

1. Did the result match the expectation?
2. Is the next action clear?
3. Is feedback sufficient?
4. Would I prefer to remain in context or change screen?
5. Would repetition make this step annoying?
6. Which alternative would I prefer, and what tradeoff drives that choice?

Every claimed feeling must trace to an event, label, missing response, or
decision. Remove generic emotional narration.

## Comparison Output

For each persona provide:

```text
Persona:
Likely first action:
Preferred flow:
Reason:
Rejected alternative:
Main tension:
Evidence basis:
Claim type: Predicted preference
Confidence: High | Medium | Low
```

Then synthesize:

- agreement across perspectives;
- meaningful disagreements;
- recommended default;
- optional accelerator or safer path;
- what needs analytics or real user research to settle.

Never average conflicting personas into one vague "user". A valid result can be
a simple default for newcomers plus an accelerator for frequent users.

Use the evidence labels from
`.opencode/skills/product-ux-orchestrator/references/evidence-and-metrics.md`.
