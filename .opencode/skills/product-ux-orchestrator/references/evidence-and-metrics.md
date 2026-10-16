# Evidence And Metrics

## Evidence Labels

Use the strongest label actually earned:

- `Observed (rendered)`: visible in the running product.
- `Observed (interaction)`: happened while exercising the flow.
- `Observed (code)`: present in source; rendering remains unverified.
- `Measured`: directly timed or counted during a repeatable run.
- `Computed`: derived from stated inputs and a documented rubric.
- `Inferred`: strongly implied by available evidence but not observed.
- `Predicted preference`: a persona-based AI judgment, not user research.
- `Hypothesis`: plausible and unverified.
- `Analytics required`: behavior or preference cannot be settled without data.
- `User research required`: a real-user preference or mental model must be
  tested rather than predicted.

Never write "users prefer" from a predicted preference. Write which simulated
perspective would likely prefer an option, why, and with what confidence.

## Flow Metrics

Count only what the defined task actually requires:

- screens or materially distinct states;
- click/tap/keyboard actions;
- decisions the user must make;
- manual fields or values;
- navigation depth and context switches;
- waits and waits without feedback;
- scroll needed to reach required actions;
- backtracking;
- error opportunities and recovery actions;
- time to value, only when measured or explicitly marked estimated.

Do not optimize click count in isolation. Three obvious actions can cost less
than one ambiguous action.

## Interaction Cost Rubric

Use this only to compare alternatives for the same goal. It is not a scientific
UX score and must not be compared across unrelated products.

| Event | Cost |
|---|---:|
| Obvious action with immediate feedback | 1 |
| Obvious navigation step | 1 |
| Simple decision with clear consequences | 2 |
| Necessary manual input | 2 |
| Input the system could already know | 3 |
| Context switch or hidden-menu navigation | 3 |
| Ambiguous decision | 4 |
| Wait without useful feedback | 4 |
| Backtracking | 5 |
| Error recovery | 5 |

Record irreversible or high-stakes risk separately; never trade safety for a
lower score. State each counted event so another reviewer can reproduce the
total. Avoid percentages unless both baseline and result were measured with the
same method.

## Confidence

- `High`: direct runtime evidence supports the conclusion.
- `Medium`: evidence is partial or the conclusion includes a prediction.
- `Low`: description-only or major states were unavailable.

Split confidence when needed: high confidence in measured cost can coexist with
medium confidence in predicted preference.
