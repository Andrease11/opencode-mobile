# Adapted Sources

This pack adapts methods rather than vendoring upstream skills wholesale.

- [paulunemoon/ux-audit-skill](https://github.com/paulunemoon/ux-audit-skill):
  evidence hierarchy, finding schema, severity/confidence separation, scoped
  flow audit, and explicit hypotheses.
- [andrew-shwetzer/human-experience](https://github.com/andrew-shwetzer/human-experience):
  expectation-versus-result walkthroughs, multi-viewport observation, friction
  inventory, and measured time to value. The Claude-specific model, paths,
  custom Playwright installation, composite perfection score, automatic git
  loop, and claim of replacing real user panels are not adopted.
- [WondelAI ux-heuristics](https://github.com/wondelai/skills/tree/main/plugins/ux-design/skills/ux-heuristics):
  Krug/Nielsen prompts, cognitive walkthrough, and the principle that obvious
  actions matter more than raw click count. The generic 10/10 score is not used.
- [Rampstack information-architecture](https://github.com/rampstackco/claude-skills/tree/main/skills/information-architecture):
  mental models, first-click prediction, tree structure, labeling, and
  taxonomy. Simulations are explicitly distinguished from real card sorting or
  tree testing.
- [WondelAI cro-methodology](https://github.com/wondelai/skills/tree/main/cro-methodology):
  funnel mapping, evidence-backed objections, guardrail metrics, and ethical
  experiments. It is conditional and never applied to ordinary product tasks.
- [Microsoft frontend-design-review](https://skills.sh/microsoft/agent-skills/frontend-design-review):
  post-flow visual quality, design-system consistency, accessibility, and
  anti-generic review.

Repository `AGENTS.md`, subsystem skills, runtime tools, security constraints,
and platform architecture override generic upstream advice.
