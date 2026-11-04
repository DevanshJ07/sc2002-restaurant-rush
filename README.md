# SC2002 Restaurant Rush

Group 4's offline Java CLI restaurant-management game, following the approved Refined Class Design v3 and assignment brief. This snapshot is **Stage 2**.

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

## Play

The menu is Salad ($9, 1 preparation unit), Burger ($15, 1), Pasta ($18, 2). Customers choose food by `(customerId - 1) mod menu size`. The manager chooses priorities, not dishes.

At each turn enter one Waiter command: `seat <customerId> <tableId>`, `order <tableId>`, `serve <tableId>`, or `wait`. Then enter `cook <tableId>` or `wait` for Chef. Invalid actions explain the problem and let you retry. Customer/table IDs are displayed. Cashier acts automatically.

There are two tables and six arrivals on turns 1, 4, 7, 10, 13 and 16. The game always runs 18 turns. Victory requires at least 4 paid customers AND $45 revenue AND average paid satisfaction at least 60. Regular customers lose 8 satisfaction each unserved waiting turn. Unpaid customers leave at zero. Serving, eating and payment happen on separate turns; tables free only at payment or abandonment.

## Reproduce results

After building, replay the checked-in inputs using PowerShell:

```powershell
Get-Content examples/victory.txt | & 'C:/Program Files/BlueJ/jdk/bin/java.exe' -cp out restaurantrush.Main
Get-Content examples/defeat.txt | & 'C:/Program Files/BlueJ/jdk/bin/java.exe' -cp out restaurantrush.Main
```

Replace the Java executable path with your JDK's path as needed. On macOS/Linux: `java -cp out restaurantrush.Main < examples/victory.txt` (likewise for defeat).

Verified Stage 2 victory: 5 paid, $73.50, average 75.60, 18 turns. Defeat (Wait throughout): 0 paid, $0.00, average N/A, 18 turns. `IntegrationChecks` replays both through the actual CLI. Tests also check the six-turn Regular Pasta example (payment satisfaction 76), invalid choices, timing, abandonment and victory boundaries.

## Ownership and stage history

ZN supplied the domain/staff classes and domain checks in a ZIP handoff. Devansh's implementation adds controller, CLI, setup, arrivals and integration checks. See [CODING_PLAN.md](CODING_PLAN.md) for the agreed split and [docs/STAGE_CHANGES.md](docs/STAGE_CHANGES.md) for transition evidence.

Each stage tag refers to independently runnable source. Do not use a Stage 3 feature switch to represent Stage 1.

- [stage-1](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-1): Regular customers and core game.
- [stage-2](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-2): VIP (turn 4, 10% discount/loss 5) and Critic (turn 10, most expensive item/loss 12).

The report, diagrams, recorded video timestamps and course declarations are maintained by the team separately; this repository does not claim those submission items are complete.

