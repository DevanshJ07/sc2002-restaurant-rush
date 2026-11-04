# Implementation handoff

## Entry point and call direction

Main builds the deterministic configuration and injects it into GameController. GameCLI owns the input/display loop. Calls go CLI -> controller -> staff/domain objects. No domain class reads input or calls the controller. Customer-specific rules dispatch through Customer; no controller type-check chain is used.

ZN's domain production source is imported from the supplied three-stage ZIP without behavioural changes. The handoff is credited here rather than inventing a Git identity for ZN. Domain tests are imported too; Stage 2's DomainChecks adds its shared summary helper as supplied. No unneeded rewrites of ZN's code were made.

## Small API completions beyond the v3 PDF

- ArrivalSchedule has a map constructor for controlled tests, alongside the required base factory.
- GameController.finishWaiterPhase explicitly closes the Waiter's opportunity even when the player chooses Wait. Only then can Chef work. This also prevents serving a dish after Chef finishes it in the same turn.
- GameController's internal phase guards reject duplicate begin/finish calls and post-game actions. Staff flags enforce their own one-action limits.
- Controller exposes a copied event list, menu items and policy-presence/display getters for the CLI. Collections are read-only; staff/domain objects still own their behaviour.
- Stage 3 retains the Stage 1 constructor for focused core-flow tests, delegating to the extended constructor with no Host/policy. Production Main always supplies both. This is not used to represent the earlier stage releases; their actual source is preserved at tags.
- PolicyManager derives active status from remaining duration, as supplied by ZN. Controller records successful activation for the display getter.

Update the final class/sequence diagrams to match these public operations and Main's actual setup wiring. Objects are constructed in Main and handed to GameController for the game lifetime; do not claim that GameController constructs every part itself.

## Timing and pricing

Waiter runs before Chef. beginTurn handles arrivals, ongoing recovery and automatic Host. New Happy Hour activation happens after the initial display and before manager staff decisions; its recovery applies immediately. Recovery only targets isWaiting customers. finishTurn advances eating, collects one previously eligible payment, applies waiting loss/abandonment, then decrements policy duration and advances the turn.

Waiter.takeOrder asks the Customer to choose and calculates that customer's bill. Controller immediately calls PolicyManager.applyDiscount on the returned order once. It never discounts an order during later turns, even if it remains IN_PROGRESS. Payment reads the stored bill rather than recomputing a price. No timer or recovery loop lives in the CLI.

## Verification scope

The scripts compile with --release 17; local execution used BlueJ's bundled OpenJDK 21.0.6. A separate JDK 17 runtime was not used locally. No online dependency is required. Replay files exercise the actual CLI and real domain classes. Domain checks alone are not presented as full-game evidence.

Run scripts/build.ps1 -Test on Windows, or sh build-and-check.sh with a JDK on PATH. Tests fail with a nonzero process status. Stage-specific check output and replay results are recorded in STAGE_CHANGES.md.

Final report, diagram export, video timestamps and signed declarations remain the team's separate submission responsibilities.

## Readability follow-up

After checking the supplied SC2002 syllabus outline, controller customer/table searches and duplicate-ID checks, plus arrival-turn validation, use ordinary for loops instead of stream/lambda chains. Existing OOP responsibilities, public APIs, game behaviour and stage release snapshots are preserved. No extra framework or design pattern was introduced. This refactoring applies to the current main branch; the stage tags remain the original verified development snapshots.
