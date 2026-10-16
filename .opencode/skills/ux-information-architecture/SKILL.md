---
name: ux-information-architecture
description: Conditional Product UX specialist for opencode-mobile navigation, sidebar placement, route hierarchy, labels, grouping, taxonomy, search, and first-click findability; use only when the problem is where users expect to find something.
---

# UX Information Architecture

Use this skill when the product question is where a capability belongs, what it
should be called, or how users navigate to it. Do not load it for an isolated
form or visual-polish task.

## Inputs

- target audience mode and goal;
- current route and navigation structure;
- product concepts and existing labels;
- frequency and importance of the destination;
- technical constraints that affect stable URLs or cross-platform exposure.

## Analysis

1. State the user's likely task-oriented mental model without inventing
   demographic detail.
2. Inventory current primary, secondary, utility, contextual, search, and direct
   entry paths.
3. Run a predicted first-click walkthrough for only the approved persona mode.
4. Apply the trunk test: identity, current location, major sections, available
   actions, hierarchy, and search must be recoverable from the screen.
5. Compare audience language with internal implementation terminology.
6. Check grouping overlap, catch-all categories, depth, route consistency,
   breadcrumbs where hierarchy is deep, and whether search is masking poor IA.
7. Preserve direct URLs and platform conventions when proposing changes.

## Honesty Rules

A predicted first click is not a real first-click test. A simulated grouping is
not card sorting. Label these as `Predicted preference` or `Hypothesis`; state
when tree testing, analytics, search logs, or real user research is required.

Do not impose generic limits such as seven navigation items without naming the
actual scanning or findability consequence in this product.

## Output

Provide current IA, target mental model, predicted first click, label findings,
recommended placement, alternative entry points, route/navigation implementation
notes, and validation gaps. A common valid result is one canonical management
location plus a contextual quick action, not moving the entire feature.

Use the shared evidence labels from
`.opencode/skills/product-ux-orchestrator/references/evidence-and-metrics.md`.
