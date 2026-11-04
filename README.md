# SC2002 Restaurant Rush

Group 4's offline Java CLI restaurant-management game, following the approved Refined Class Design v3 and assignment brief. The default branch is the complete **Stage 3** game.

## Run on Windows

Requires a JDK 17 or newer. Set JAVA_HOME to your JDK directory or put its bin directory on PATH. The PowerShell scripts also recognise BlueJ's bundled JDK on this machine.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run.ps1
```

Compile and run all checks:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build.ps1 -Test
```

On macOS/Linux or Git Bash with a JDK on PATH, run `sh build-and-check.sh`, then `java -cp out restaurantrush.Main`.

Manual build (all platforms):

```text
javac --release 17 -d out src/main/java/restaurantrush/*.java
java -cp out restaurantrush.Main
```

No Maven, Gradle, external dependency or network connection is required.

Core game logic uses ordinary loops and conditionals, classes/objects, abstract classes, inheritance, overriding, enums, collections and exception handling. The controller's small phase enum enforces turn order; it is not a separate framework. Read-only collection copies prevent callers from changing internal lists. Test helpers are separate from the game source.

## Play

The menu is Salad ($9, 1 preparation unit), Burger ($15, 1), Pasta ($18, 2). Customers choose food by `(customerId - 1) mod menu size`. The manager chooses priorities, not dishes.

At each turn enter one Waiter command: `seat <customerId> <tableId>`, `order <tableId>`, `serve <tableId>`, or `wait`. Then enter `cook <tableId>` or `wait` for Chef. Invalid actions explain the problem and let you retry. Customer/table IDs are displayed. Cashier acts automatically.

There are two tables and six arrivals on turns 1, 4, 7, 10, 13 and 16. The game always runs 18 turns. Victory requires at least 4 paid customers AND $45 revenue AND average paid satisfaction at least 60. Regular customers lose 8 satisfaction each unserved waiting turn. Unpaid customers leave at zero. Serving, eating and payment happen on separate turns; tables free only at payment or abandonment.

Stage 2 replaces turn-4's arrival with VIP (loss 5, 10% discount) and turn-10's with Critic (loss 12, chooses most expensive item). Stage 3 adds an automatic Host who seats one queue-head customer at the lowest-ID free table before manager choices, independently of Waiter. The menu appends Salad + Pasta Combo ($24.30, 3 units).

At turn start, answer `yes` or `no` to Happy Hour activation. It can activate once, lasts the current turn and next, recovers waiting satisfaction by 15 each active turn (maximum 100), and discounts orders taken during those turns by 20% after any VIP discount. Bills already placed never change. The remaining duration is displayed. Once activated, the activation question stops appearing.

## Reproduce results

After building, replay the checked-in inputs using PowerShell:

```powershell
Get-Content examples/victory.txt | & 'C:/Program Files/BlueJ/jdk/bin/java.exe' -cp out restaurantrush.Main
Get-Content examples/defeat.txt | & 'C:/Program Files/BlueJ/jdk/bin/java.exe' -cp out restaurantrush.Main
```

Replace the Java executable path with your JDK's path as needed. On macOS/Linux: `java -cp out restaurantrush.Main < examples/victory.txt` (likewise for defeat).

Verified Stage 3 victory: 5 paid, $73.80, average 84.80, 18 turns (Happy Hour activated on turn 5). Defeat (decline Happy Hour and Wait throughout): 0 paid, $0.00, average N/A, 18 turns. `IntegrationChecks` replays both through the actual CLI. The test strategy is a deterministic test helper; it is not an automatic player in production. Tests also check the six-turn Regular Pasta example (payment satisfaction 76), invalid choices, timing, abandonment, victory boundaries, VIP/Critic behaviour, independent Host actions and Happy Hour boundaries.

## Ownership and stage history

ZN supplied the domain/staff classes and domain checks in a ZIP handoff. Devansh's implementation adds controller, CLI, setup, arrivals and integration checks. See [CODING_PLAN.md](CODING_PLAN.md) for the agreed split and [docs/STAGE_CHANGES.md](docs/STAGE_CHANGES.md) for transition evidence.

Each stage tag refers to independently runnable source. Do not use a Stage 3 feature switch to represent Stage 1.

- [stage-1](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-1): Regular customers and core game.
- [stage-2](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-2): VIP (turn 4, 10% discount/loss 5) and Critic (turn 10, most expensive item/loss 12).
- [stage-3](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-3): automatic Host, ComboMeal, Happy Hour.

To inspect an earlier version without changing your current files, clone the repository separately with `git clone --branch stage-1 https://github.com/DevanshJ07/sc2002-restaurant-rush.git restaurant-rush-stage1` (substitute stage-2 as needed). Each tag includes its own appropriate replay inputs and check runners.

Stage changes: [1 to 2](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-1...stage-2), [2 to 3](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-2...stage-3). See [docs/IMPLEMENTATION_NOTES.md](docs/IMPLEMENTATION_NOTES.md) for integration details and additions to v3's API.

The report, diagrams, recorded video timestamps and course declarations are maintained by the team separately; this repository does not claim those submission items are complete.

