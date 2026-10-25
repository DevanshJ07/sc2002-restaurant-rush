# Proposed coding responsibilities

Basis: approved Refined Class Design v3. Devansh prefers controller and CLI ownership. Teammate ownership below is proposed for team agreement.

## Devansh — orchestration and player interaction

Own GameController, GameCLI, Main and ArrivalSchedule across all stages.

Stage 1 responsibilities:
- Implement beginTurn, waiter action routing, chef action routing and finishTurn.
- Integrate arrivals, staff reset, eating and payment timing, satisfaction loss, abandonment and table cleanup.
- Track revenue, paid satisfaction, served and abandoned counts; calculate final victory after all 18 turns.
- Implement input parsing, retry loops, Wait actions, status displays and final results. Keep game rules in controller/domain objects.
- Wire the application and base setup. Add build/run instructions and reproducible victory/defeat input sequences.

Stage 2 responsibilities:
- Update ArrivalSchedule: turn 4 VIP and turn 10 Critic; preserve six arrivals.
- Verify integration without customer type checks or changes to the common service flow.

Stage 3 responsibilities:
- Integrate automatic Host seating, Happy Hour activation/recovery/timer and order-time pricing.
- Add Happy Hour prompts and displays; register Salad + Pasta combo in Main.
- Verify activation lasts the current and next turns, with recovery exactly once per active turn and no retroactive discounts.

Verification ownership: full-turn scenarios, robust CLI input, win/lose runs, staff limits across turns, release integration.

## Teammate — domain behaviour and staff actions

Own Customer, RegularCustomer, VIPCustomer, CriticCustomer, Staff, Waiter, Chef, Cashier, Host, Table, WaitingQueue, Order, Menu, MenuItem, ComboMeal, PolicyManager, CustomerState, OrderStatus and InvalidActionException.

Stage 1 responsibilities:
- Implement encapsulated customer state, deterministic item selection, satisfaction loss and service/payment timestamps.
- Implement staff action validation, successful-action flags and per-turn reset support.
- Implement queue/table operations, order preparation and guarded status transitions.
- Implement Cashier selection: earliest eligible readyToPayTurn, ties by customerId; mark paid and free the table.
- Implement integer-cent prices and read-only collection access needed by the controller/CLI.

Stage 2 responsibilities:
- Implement VIP discount and loss rate; Critic loss rate and most-expensive-item preference.
- Add Menu.getMostExpensiveItem; dispatch through Customer methods.

Stage 3 responsibilities:
- Implement Host, ComboMeal and PolicyManager.
- Add Customer.recoverSatisfaction and Order.applyDiscount only in Stage 3.
- Compute combo price/units in its constructor; preserve integer cents and rounding.

Verification ownership: invalid actions leave state and staff availability unchanged; order transition guards; money/discount calculations; customer behaviour; cashier eligibility and ordering; Host capacity; policy duration and reuse rejection.

## Why this is a fair starting split

Devansh owns fewer classes, but the controller and CLI carry most cross-object coordination, timing, input handling and integration work. The teammate owns more classes, each with a narrower responsibility, plus most Stage 2 and Stage 3 domain extensions. Both own implementation and verification. Reassess after the first integrated Stage 1 flow; move an isolated task if actual effort becomes uneven.

## Shared contract before parallel coding

1. Agree package layout, JDK/build choice and the v3 method signatures before writing dependent code. Do not redesign the approved public API independently.
2. Teammate supplies domain signatures first; Devansh supplies controller methods and the getters required by CLI. Confirm null-return and exception behaviour.
3. Waiter.takeOrder returns an Order priced with the customer's discount. In Stage 3, the controller immediately applies PolicyManager's discount before chef progression. Prevent later or duplicate repricing in the actual call flow.
4. Waiter acts before Chef. A dish completed by Chef is served next turn at the earliest.
5. servedTurn < currentTurn permits finishing eating; readyToPayTurn < currentTurn permits payment. Preserve both delays.
6. Happy Hour activation must recover eligible customers once on activation and once on the following turn, without double recovery. Automatic Host seating must not consume the Waiter's action.
7. InvalidActionException propagates to GameCLI for a helpful retry; markActed occurs only after success. Explain input parsing errors separately.

## Integration checkpoints

1. Agree contracts and build setup together.
2. Integrate one Regular customer from arrival through payment, including the Pasta six-turn example.
3. Complete Stage 1: six arrivals, two tables, 18 turns, abandonment, invalid inputs and reproducible victory/defeat. Review each other's work, then tag stage-1.
4. Add only Stage 2 changes; verify VIP and Critic behaviour and rerun core flow checks, then tag stage-2.
5. Add Stage 3 domain features and controller/CLI integration; verify stacked discounts, combo cooking, Host independence and Happy Hour boundaries, then tag stage-3.

## GitHub workflow

- Keep main runnable after the first playable integration.
- Use short feature branches and pull requests; the other coder reviews changes before merging.
- Keep file ownership above to reduce merge conflicts. Coordinate before touching the other's files.
- Each coder commits their own work. Do not create stage tags from an incomplete scaffold.
- Share final class/API changes and reproducible evidence with the diagram/documentation members; they retain ownership of their deliverables.
- Add all team members as collaborators after their GitHub usernames are supplied.
