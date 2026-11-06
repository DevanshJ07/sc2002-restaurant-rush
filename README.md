# Restaurant Rush

SC2002 Group 4’s offline, turn-based restaurant management game. The player manages staff priorities through a Java command-line interface. Customers choose their own food; the player decides whom to seat, whose order to take, what to cook, and when to serve.

**Run the current Stage 3 game from `main`.** The Host now takes a player command before the Waiter. Customers join the queue first and stay there until the Host or Waiter seats them.

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
- [stage-3-player-host](https://github.com/DevanshJ07/sc2002-restaurant-rush/tree/stage-3-player-host): revised Stage 3 snapshot with a player-controlled Host. Use `main` for that behaviour plus the later currency display fix.

[Stage 1 → Stage 2](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-1...stage-2) adds the VIP and Critic classes and tests, and updates the menu lookup and arrival schedule. [Stage 2 → current Stage 3](https://github.com/DevanshJ07/sc2002-restaurant-rush/compare/stage-2...main) adds Host, ComboMeal, PolicyManager and their tests, and updates setup, customer recovery, order pricing, turn coordination, prompts and replay inputs. No production classes were removed in either transition. See [stage change notes](docs/STAGE_CHANGES.md) for details.

## Source and demo notes

The Java source is in [src/main/java/restaurantrush](src/main/java/restaurantrush), with tests in [src/test/java/restaurantrush](src/test/java/restaurantrush). Devansh implemented the controller, CLI, setup and arrivals and integrated the stages. ZN supplied the domain and staff classes and their checks. See the [implementation notes](docs/IMPLEMENTATION_NOTES.md) for the code structure and diagram handoff.

For the recorded demo, use the current examples and show the Host choice separately from the Waiter action. The earlier Word guide was written for the automatic Host version, so its command list needs the extra Host input. Add the video link and actual segment timestamps here after recording.
