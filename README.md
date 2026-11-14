# Restaurant Rush

SC2002 Group 4’s offline, turn-based restaurant management game. The player manages staff priorities through a Java command-line interface. Customers choose their own food; the player decides whom to seat, whose order to take, what to cook, and when to serve.

**Run the current Stage 3 game from `main`.** The Host now takes a player command before the Waiter. Customers join the queue first and stay there until the Host or Waiter seats them.

**Demo video:** [https://www.youtube.com/watch?v=y8gwKpnKHsA](https://www.youtube.com/watch?v=y8gwKpnKHsA)

## Build and run

Use **JDK 17 or newer**. The game needs no external Java libraries, build framework, or network connection once the source is downloaded.

~~~bash
git clone https://github.com/DevanshJ07/sc2002-restaurant-rush.git
cd sc2002-restaurant-rush
~~~

From the repository folder on **Windows**, launch the game or run the checks:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/run.ps1
powershell -ExecutionPolicy Bypass -File scripts/build.ps1 -Test
~~~

The scripts look for a JDK through `JAVA_HOME` or `PATH` and can also use BlueJ’s bundled JDK at its default Windows location.

On **macOS or Linux**, with the JDK on `PATH`:

~~~sh
sh build-and-check.sh
java -cp out restaurantrush.Main
~~~

## How to play

The game runs for **18 turns** with two tables and arrivals on turns **1, 4, 7, 10, 13 and 16**. To win, finish with **at least 4 paid customers, $45.00 in revenue, and an average paid-customer satisfaction of 60 or more**. All three targets must be met. Revenue and the satisfaction average count payments only; the average shows `N/A` before the first payment.

### Turn order in the current build

1. Read the opening status. Then the scheduled customer arrives, and the game shows the updated queue and tables before asking for actions.
2. If Happy Hour is still available, enter `yes` to activate it or `no` to save it.
3. **Host:** enter `seat` to seat the first queued customer at the lowest-numbered free table, or `wait` to skip. This prompt does not take customer or table IDs.
4. **Waiter:** enter `seat <customerId> <tableId>`, `order <tableId>`, `serve <tableId>`, or `wait`. The Waiter gets a separate action even if the Host seats someone.
5. **Chef:** enter `cook <tableId>` to advance an order by one preparation unit, or `wait`.
6. Eating, eligible payments, satisfaction loss and departures are handled at the end of the turn. A dish finished by the Chef can be served on a later turn. A customer who finishes eating can pay on a later turn. The Cashier handles at most one eligible payment per turn, oldest first.

Invalid input is explained and you can retry without using up the action. Customers waiting for a table or food lose satisfaction each turn. At zero, they leave without paying, and any unfinished order is cancelled.

**Try turn 1:** type `no` at Happy Hour, `seat` at Host, `order 1` at Waiter, then `cook 1` at Chef. The Salad is now `READY`. On turn 2, choose `no` for Happy Hour, `wait` for Host, `serve 1` for Waiter, and `wait` for Chef.

### Menu and customer behaviour

The base menu is Salad (**$9.00**, 1 preparation unit), Burger (**$15.00**, 1 unit), and Pasta (**$18.00**, 2 units). Stage 3 adds a Salad + Pasta Combo (**$24.30**, 3 units). Regular and VIP customers choose the menu item at zero-based index `(customerId - 1) % menu size`. Critics choose the most expensive item, with the first item winning a tie. Stage 2 and later have a VIP on turn 4 and a Critic on turn 10.

Regular customers lose 8 satisfaction per waiting turn. VIP customers lose 5 and receive a 10% discount. Critics lose 12 and pay full price before any Happy Hour discount.

Happy Hour can be activated once per game, for the current turn and the next. Eligible waiting customers recover 15 satisfaction at the start of each active turn, capped at 100. New orders get 20% off **after** any VIP discount; earlier bills stay locked. A VIP Burger ordered during Happy Hour costs **$10.80**. Money is stored in integer cents and shown in dollars to two decimals.

## Replay and test

Use the replay file from the **same branch or tag** as the game. The current Stage 3 files include a Host decision every turn. In PowerShell, build first and then run:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/build.ps1 -Test
Get-Content examples/victory.txt | java -cp out restaurantrush.Main
Get-Content examples/defeat.txt | java -cp out restaurantrush.Main
~~~

If `java` is not on your `PATH`, replace it with `& 'C:/Program Files/BlueJ/jdk/bin/java.exe'` or the path to your JDK’s Java executable. On macOS or Linux, use `java -cp out restaurantrush.Main < examples/victory.txt` and substitute `defeat.txt` for the defeat replay.

- **Victory replay:** 5 paid customers, $73.80 revenue, 84.80 average satisfaction, 18 turns.
- **Defeat replay:** 0 paid customers, $0.00 revenue, N/A average satisfaction, 18 turns.

The repository checks cover invalid actions, staff limits, preparation and payment timing, abandonment, victory thresholds, customer types, Host choices, and Happy Hour. The current build also matched an independent rules model across 72 full games, 1,296 turns, and 15,857 rejected inputs.

## Stage history

Each tagged snapshot is independently runnable. To play a particular stage separately, use `git clone --branch <tag> https://github.com/DevanshJ07/sc2002-restaurant-rush.git <new-folder>`. Stage 1 and Stage 2 have no Host: use `seat 1 1` at the first Waiter prompt.

- [stage-1](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-1): Regular customers and the complete base game.
- [stage-2](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-2): adds VIP and Critic customers.
- [stage-3](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-3): Host controlled by the player, ComboMeal, and Happy Hour. Includes the corrected opening display and dollar formatting.

### Stage 1 to Stage 2

[Compare stage-1 to stage-2](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-1...stage-2)

| Change | Files | Reason |
| --- | --- | --- |
| Added | `VIPCustomer.java`, `CriticCustomer.java` | Add different satisfaction, pricing and food choice through customer subclasses. |
| Added | `Stage2Checks.java`, `Stage2IntegrationChecks.java` | Check the new customer rules in isolation and in the game. |
| Changed | `ArrivalSchedule.java` | Replace the arrivals on turns 4 and 10 with a VIP and Critic. |
| Changed | `Menu.java` | Give the Critic a lookup for the most expensive item. |
| Changed | `DomainChecks.java` | Share the check summary helper with the Stage 2 tests. |
| Changed | `examples/victory.txt` | Reproduce a win with the Stage 2 customer schedule. |
| Changed | `README.md`, `docs/STAGE_CHANGES.md` | Document Stage 2 and its replay results. |
| Deleted | None | |

### Stage 2 to Stage 3

[Compare stage-2 to stage-3](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-2...stage-3)

| Change | Files | Reason |
| --- | --- | --- |
| Added | `Host.java` | Seat one queued customer using the Host's own action. |
| Added | `ComboMeal.java` | Combine two menu items into one dish with its own price and preparation time. |
| Added | `PolicyManager.java` | Manage Happy Hour activation, duration and discount. |
| Added | `Stage3Checks.java`, `Stage3IntegrationChecks.java` | Check Host independence, combo pricing and Happy Hour timing. |
| Added | `.gitattributes`, `docs/IMPLEMENTATION_NOTES.md` | Set consistent line endings and document integration decisions, respectively. |
| Changed | `Main.java` | Register the combo, Host and policy in the game setup. |
| Changed | `GameController.java` | Coordinate the Host phase, recovery, the policy timer and discounts on new orders. |
| Changed | `GameCLI.java` | Show the opening state before arrivals and prompt for Happy Hour and the Host action. |
| Changed | `Customer.java` | Allow eligible customers to recover satisfaction. |
| Changed | `Order.java` | Allow the policy discount when a new order is placed. |
| Changed | `ArrivalSchedule.java` | Validate turn numbers with a plain loop; keep the same six arrivals. |
| Changed | `IntegrationChecks.java` | Exercise the Stage 3 prompts and replay flow. |
| Changed | `examples/victory.txt`, `examples/defeat.txt` | Supply the extra Host and Happy Hour inputs for both replays. |
| Changed | `scripts/run.ps1` | Stop the launcher if the build fails. |
| Changed | `README.md`, `docs/STAGE_CHANGES.md` | Document the Stage 3 rules, checks and replay results. |
| Deleted | None | |

### Refactor after Stage 3

[Compare stage-3 to main](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-3...main). After tagging Stage 3, the team moved scorekeeping into `GameStats`, made `calculateBill()` a template method, and replaced general customer state changes with guarded operations such as `seat()`, `abandon()` and `pay()`. The replay results stayed the same. See the [stage change notes](docs/STAGE_CHANGES.md) for details.

## Source and demo notes

The Java source is in [src/main/java/restaurantrush](src/main/java/restaurantrush), with tests in [src/test/java/restaurantrush](src/test/java/restaurantrush). Devansh implemented the controller, CLI, setup and arrivals and integrated the stages. ZN supplied the domain and staff classes and their checks. Braden drew the class and sequence diagrams (the `.drawio` files in the repository root). Minh wrote the documentation, reviewed the code against SOLID and made the final refactor. See the [implementation notes](docs/IMPLEMENTATION_NOTES.md) for the code structure and diagram handoff.

| # | Brief requirements | Start time | Presenter |
|:-:|-------------------------------|:----------:|-----------|
| 1 | The game launching from the submitted project | 4.06 | Devansh |
| 2 | Meaningful manager choices, including a decision where staff are constrained | 5.37 | Devansh |
| 3 | A dish going READY → SERVED → payment | 4.35 | Devansh |
| 4 | A VIP or Critic behaviour (VIP 10% pricing discount) | 5.15 | Devansh |
| 5 | A Stage 3 feature (Host action) | 4.22 | Devansh |
| 6 | One victory and one defeat | 9.37 (V), 10.00 (D) | Zheng Ning |
| 7 | The stage tag comparisons | 1.19 | Minh |
| 8 | The Diagrams | 12.00 | Braden |

Video link: https://www.youtube.com/watch?v=y8gwKpnKHsA
