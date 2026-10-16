# Compose Performance Diagnosis

Use one reproducible interaction and one primary hypothesis.

1. Confirm that visible data did not legitimately change.
2. Capture recomposition counts or Compose compiler reports for the shipped
   variant.
3. Classify the issue:
   - stable versus unstable parameter comparison;
   - new unstable instances created by the caller;
   - fast-changing State read in composition instead of layout/draw;
   - snapshot state written back across phases;
   - expensive work performed during composition;
   - lazy item identity/key problem.
4. Apply the smallest truthful fix.
5. Repeat the same measurement.

With Kotlin 2.2 strong skipping, unstable parameters do not automatically make a
restartable composable non-skippable. Inspect comparison semantics and instance
churn before adding annotations or memoization.

Do not call fewer recompositions an improvement when frame time, allocations,
or user-visible latency did not improve.
