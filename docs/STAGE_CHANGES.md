# Stage implementation evidence

## Stage 1

Imported ZN's 14 Stage 1 domain/staff files and DomainChecks from the supplied handoff. No behavioural fixes to ZN's implementation were needed for this release.

Added GameController, GameCLI, Main and ArrivalSchedule, IntegrationChecks, portable build/check support and reproducible input files. GameController uses explicit turn phases; finishWaiterPhase closes the Waiter's opportunity even on Wait. ArrivalSchedule accepts a map constructor for controlled scenario tests; the base factory retains the required six arrivals.

Validation: JDK 21.0.6 compilation with --release 17, 105 domain checks, 74 integration checks. CLI replays: victory 5 paid/$66.00/79.20 average; defeat 0 paid/$0.00/N/A; both 18 turns. Java 17 API/language compatibility is checked by --release 17; execution was on JDK 21, not a separate JDK 17 runtime.

Stage 1 source contains only Regular customers and core operation. Stage tags are created only after real integrated checks pass.
