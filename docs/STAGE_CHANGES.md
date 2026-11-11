# Stage implementation evidence

## Stage 1

Imported ZN's 14 Stage 1 domain/staff files and DomainChecks from the supplied handoff. No behavioural fixes to ZN's implementation were needed for this release.

Added GameController, GameCLI, Main and ArrivalSchedule, IntegrationChecks, portable build/check support and reproducible input files. GameController uses explicit turn phases; finishWaiterPhase closes the Waiter's opportunity even on Wait. ArrivalSchedule accepts a map constructor for controlled scenario tests; the base factory retains the required six arrivals.

Validation: JDK 21.0.6 compilation with --release 17, 105 domain checks, 74 integration checks. CLI replays: victory 5 paid/$66.00/79.20 average; defeat 0 paid/$0.00/N/A; both 18 turns. Java 17 API/language compatibility is checked by --release 17; execution was on JDK 21, not a separate JDK 17 runtime.

Stage 1 source contains only Regular customers and core operation. Stage tags are created only after real integrated checks pass.

## Stage 1 to Stage 2

New: VIPCustomer, CriticCustomer, Stage2Checks, Stage2IntegrationChecks.
Changed: Menu adds most-expensive lookup; ArrivalSchedule replaces arrivals on turns 4/10 with VIP/Critic; DomainChecks exposes its summary helper for the new check runner. README and replay input files reflect the new stage. No production files deleted. GameController, GameCLI and Main are unchanged.

Validation: 105 core domain checks, 17 Stage 2 domain checks, 74 general integration checks and Stage 2 integration scenarios. CLI victory: 5 paid/$73.50/75.60 average, 18 turns; defeat: 0 paid/$0/N/A, 18 turns. Replays are generated against the real stage-specific schedule.

[Compare stage-1 to stage-2](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-1...stage-2).

## Stage 2 to Stage 3

New: Host, ComboMeal, PolicyManager, Stage3Checks, Stage3IntegrationChecks, implementation notes and text line-ending conventions.
Changed: Customer adds recovery; Order adds guarded discount (both imported from ZN's Stage 3 snapshot); GameController coordinates Host, activation/recovery/timer and order-time pricing; GameCLI adds activation prompt/duration display; Main registers combo/Host/policy. IntegrationChecks adapts replay commands to policy prompts and exercises actual Stage 3 runs. README/examples reflect Stage 3; run.ps1 stops on build errors. No production files deleted. ArrivalSchedule and customer subtype dispatch remain unchanged.

ZN's supplied production code required no behavioural fixes. Additional controller APIs and setup/diagram implications are explained in IMPLEMENTATION_NOTES.md. CODING_PLAN.md is retained as the original planning/handoff contract; this evidence file records what was actually delivered.

Validation: 105 core + 17 Stage 2 + 41 Stage 3 domain checks, 74 general integration checks, Stage 2 integration scenarios, 27 Stage 3 integration checks (with additional exception-boundary assertions). Actual CLI victory: 5 paid/$73.80/84.80 average, 18 turns, Happy Hour turn 5. Defeat: 0 paid/$0/N/A, 18 turns.

[Compare stage-2 to the original Stage 3 commit](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-2...e0ec925728901763da5cb6c593f530f57ec013fb).

## Stage 3 player controlled Host

The original automatic Host snapshot remains in Git history at commit e0ec925. The stage-3-player-host tag contains the revised Stage 3 game. The first status display precedes arrivals; after arrivals, customers remain queued until the player chooses to seat them. The Host prompt accepts seat or wait before the separate Waiter and Chef choices. Host still selects the queue head and the lowest-ID free table.

Diagram handoff: add GameCLI.promptHostAction(), GameController.hostSeat(), finishHostPhase() and hasHost() to the Stage 3 class diagram. In the sequence diagram show CLI prompting for seat or wait, calling hostSeat only on seat, then closing the Host phase before Waiter. The Host domain signature is unchanged.

The Stage 3 replay files include one additional Host input per turn. The earlier Word guide describes the original automatic Host replay; use the updated README and example inputs when demonstrating this version.

## Final refactor after Stage 3

Behaviour-preserving changes made after the stage tags, so the stage tags still show each stage as it was delivered.

Cleanup: consistent formatting and comments; Host and Cashier throw IllegalStateException for states that can only come from a programming error, while InvalidActionException stays for player mistakes; GameCLI uses GameController.MAX_TURNS instead of a hard-coded 18.
New: GameStats (score counting and the victory rule, previously inside GameController).
Changed: Customer.calculateBill() is a final template method that validates the price once and calls the subtype's applyPricing(); Customer.setState() is replaced by seat(), abandon() and pay(); Waiter, Host, Cashier and GameController call these instead. GameController no longer keeps its own happyHourUsed flag: PolicyManager.hasBeenUsed() is the single source of truth. The unused GameController.hasChefActedThisTurn() was removed. GameController's public methods are unchanged, so GameCLI needed no change for the refactor.
Diagrams: the Stage 3 class diagram and the sequence diagram are updated to this final code (GameStats class and lifeline, applyPricing(), seat()/pay()).

Validation: all existing checks still pass with the same counts; both replay files and 150 randomised games produce output identical to the stage-3-player-host build.
