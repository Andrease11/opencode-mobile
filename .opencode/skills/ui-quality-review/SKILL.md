---
name: ui-quality-review
description: Post-flow visual and interaction-quality review for rendered opencode-mobile Android UI; checks hierarchy, typography, spacing, states, accessibility, responsiveness, platform fit, and design-system consistency without changing approved product behavior for taste.
---

# UI Quality Review

Review rendered quality after the flow is defined or when the user explicitly
requests a visual audit. Product behavior belongs to `ux-flow-audit`; this skill
must not reopen approved flow decisions merely to create a more fashionable UI.

## Evidence First

Use screenshots and the actual runtime when possible. Code can prove tokens,
semantics, and state branches, but not final rendering. Load
`android-runtime-verification`. Review phone and, when relevant, foldable or
tablet widths, in light and dark theme.

## Review Dimensions

- attention order and primary-action hierarchy;
- grouping, alignment, whitespace, density, and content scan path;
- typography scale, line length, weight, and truncation;
- semantic color roles, contrast, dark mode, and non-color cues;
- component consistency, radius, border, elevation, and token drift;
- default, hover/press, focus, selected, disabled, loading, empty, error, and
  success states;
- keyboard, screen-reader semantics, touch targets, reduced motion, font scale,
  RTL, and insets as relevant;
- responsive reflow rather than squeezed desktop layouts;
- visual trust, authentic content, and avoidance of generic card-heavy or
  decorative UI that obscures the task.

Preserve the established `Oc*` Compose design system, which follows the OpenCode
V2 visual language (`docs/design-system.md`), unless the user explicitly
requests a redesign.

## Finding Schema

```text
ID: UI-<number>
Severity: Blocker | High | Medium | Low
Confidence:
Rendered location:
Observation:
User consequence:
Recommendation:
System/token impact:
```

Taste disagreements cannot be Blocker or High. Accessibility, unreadable
content, hidden actions, broken responsive layouts, and missing task states can.

## Output

Lead with prioritized findings, then list strengths to preserve, responsive and
state coverage, design-system drift, and unverified scenarios. Do not claim a
visual fix until the changed runtime is rendered and reviewed again.
