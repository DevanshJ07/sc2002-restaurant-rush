# Restaurant Rush coding plan for Devansh and ZN

This handoff is intended for both coders and their AI assistants. Build the Java CLI game required by the assignment brief, following the approved Refined Class Design v3. The brief governs mandatory behaviour; v3 governs the agreed design. Earlier Google Doc/v2 material is historical where it disagrees.

This plan specifies future work. The repository currently contains planning files, not a game, tests, stubs or stage releases.

## 1. Working model: parallel implementation, Devansh integrates

Devansh builds orchestration and the CLI. ZN builds domain objects and staff behaviour. Both work from the fixed API contract below, so neither needs to wait for the other's implementation to begin.

Work through Stage 1, Stage 2 and Stage 3 in order. Work in parallel within each stage, then integrate and tag that stage before beginning the next. Do not build Stage 3 first and reconstruct earlier versions by disabling features.

ZN's production files must not depend on GameController, GameCLI, Main or ArrivalSchedule. ZN can compile and test the domain package independently.

While ZN works, Devansh may generate disposable domain stubs from this contract to compile controller/CLI wiring. Put these outside production source in .local-stubs/restaurantrush/, which is ignored by Git. Compile Devansh's files explicitly with those stubs into a separate temporary output directory. Never compile real and stub versions of the same class together, merge stubs, or treat stub runs as game-rule evidence. The real integrated code must pass all behavioural checks.

There are three planned handoffs, one per stage. Each coder delivers their whole stage slice with checks, rather than repeatedly asking the other for individual classes. Only API conflicts or actual defects should require coordination between handoffs.

Devansh owns final integration, small wiring corrections and release tags. ZN fixes real domain defects and reviews integration changes affecting domain behaviour.

## 2. Game specification both AI assistants must follow

The player manages staff priorities. Customers choose their own food. Required features run offline with a text interface.

- Exactly 18 turns, with no early victory or defeat ending.
- Two tables, one Waiter, one Chef and one Cashier. Stage 3 adds one Host.
- Six arrivals: turns 1, 4, 7, 10, 13 and 16; customer IDs 1 through 6; initial satisfaction 100.
- Base menu in this order: Salad, 900 cents, 1 prep unit; Burger, 1500 cents, 1 unit; Pasta, 1800 cents, 2 units.
- Victory after turn 18 requires all three: at least 4 payments, at least 4500 cents revenue, paid-customer average satisfaction at least 60.
- Revenue includes payments only. Record satisfaction at payment and average those recorded values. Display N/A before any payment.
- Store all money in integer cents; display dollars with two decimals. Discounts round to nearest cent, not truncate.
- Regular/VIP food choice: index (customerId - 1) modulo menu size. Critic chooses the most expensive item, first on ties.
- Stage 1: all Regular. Stage 2: customer 2 arriving turn 4 is VIP; customer 4 arriving turn 10 is Critic. Other arrivals remain Regular.
- Stage 3: append Salad + Pasta ComboMeal, price 2430 cents, prep units 3. Selection rules remain unchanged; customer 4 is the Critic and chooses the combo.
- Regular/VIP/Critic waiting loss: 8/5/12. VIP bill: 90% of base price. Other types pay full price.
- Happy Hour: activate once at the start of a turn; lasts that turn and the next; orders placed then get 20% off after VIP discount. Eligible waiting customers recover 15 at each active turn start, capped at 100. Existing orders retain their locked bills.

## 3. File ownership and deliverables

### Devansh: orchestration, UI and integration

Own GameController.java, GameCLI.java, Main.java and ArrivalSchedule.java across all stages.

Own turn sequencing, staff action routing, arrivals, revenue/counters, victory checks, abandonment coordination, input parsing/retry, displays, application wiring and final integration. Own build/run instructions and full-game reproducible input scripts/checks. Provide coding evidence to the documentation members; they retain ownership of the final report and diagrams.

Stage 1: complete the four files and full game flow. Stage 2: update ArrivalSchedule and integration checks; controller/CLI should stay unchanged through polymorphic Customer behaviour. Stage 3: integrate Host and pricing policy, add Happy Hour prompts/displays, register combo in Main.

### ZN: domain objects, staff and focused verification

Stage 1 files: Customer, RegularCustomer, CustomerState, Staff, Waiter, Chef, Cashier, Table, WaitingQueue, Order, OrderStatus, Menu, MenuItem and InvalidActionException, each in its own .java file.

Stage 2: add VIPCustomer and CriticCustomer; add Menu.getMostExpensiveItem().

Stage 3: add Host, ComboMeal and PolicyManager; add Customer.recoverSatisfaction() and Order.applyDiscount().

Own domain invariants, staff action limits, atomic validation, queue/table operations, order transitions, food selection, customer behaviour, cashier selection, pricing and focused real-domain checks. Do not implement a second controller or CLI.

### Why this split is reasonably balanced

Devansh has fewer classes but owns the largest coordination and integration workload. ZN has more smaller classes and most extension behaviours, plus the larger domain test workload. Both implement, verify and review. Reassess effort after Stage 1; transfer a self-contained verification task if actual effort is uneven.

## 4. Fixed technical defaults

These defaults remove setup decisions before parallel work. If the course requires another JDK/layout, agree one change before coding.

- JDK 17, standard Java libraries, no mandatory third-party dependency or online service.
- Package restaurantrush for all production classes. Directory src/main/java/restaurantrush/; one public class per file.
- Focused Java check runners under src/test/java/restaurantrush/. Use explicit assertions/check helpers; no required downloaded test framework.
- Production compile from repo root: javac -d out src/main/java/restaurantrush/*.java
- Launch: java -cp out restaurantrush.Main
- Main creates the base configuration; objects are passed into GameController. ZN objects never create the controller or read Scanner.
- Stable table IDs 1 and 2. Player selects customer/table IDs, never list offsets or menu items.
- Private fields, guarded transitions, unmodifiable collection views. No customer type-check chains; dispatch through Customer methods and getTypeLabel().
- InvalidActionException extends Exception. Expected invalid player actions give reasons and propagate to GameCLI for retry. Validate before mutation; mark staff acted only after success.
- Invalid configuration/constructor values use IllegalArgumentException; direct illegal domain misuse may use IllegalStateException where no checked exception is specified. Staff/controller prevalidate expected player errors into InvalidActionException.
- No GUI, database, persistence or extra difficulty is required.

## 5. Shared API contract

The signatures below make v3 implementable without ongoing coordination. Constructors and helper getters fill gaps in the PDF; they are implementation conventions, not claims that v3 printed every signature. Keep names/types consistent. Private helpers are unrestricted. If a contract must change, record the exact correction and notify the other coder once before depending on it.

### Customer and enums: ZN

CustomerState: QUEUED, SEATED, EATING, READY_TO_PAY, PAID, ABANDONED.
OrderStatus: IN_PROGRESS, READY, SERVED, PAID, CANCELLED.

Customer is abstract with protected Customer(int customerId). Concrete customer constructors are public and take int customerId. Start with QUEUED, satisfaction 100, no order, servedTurn/readyToPayTurn = -1.

Public methods:
- int getCustomerId(), int getSatisfaction(), CustomerState getState(), Order getOrder().
- void setState(CustomerState state), void setOrder(Order order), retained from v3; guard against illegal transitions/replacing an existing order. Null/invalid direct usage is a programmer error.
- boolean isWaiting(): QUEUED or SEATED awaiting service, including READY dishes.
- void loseSatisfaction(): subtract own loss rate, minimum 0. Controller decides when eligible.
- void markServed(int turn): require SEATED with SERVED order, set EATING and servedTurn.
- void markReadyToPay(int turn): require EATING and turn > servedTurn; set state and readyToPayTurn.
- int getServedTurn(), int getReadyToPayTurn().
- abstract int getSatisfactionLossRate(), abstract int calculateBill(int baseCents), abstract String getTypeLabel().
- MenuItem chooseItem(Menu menu): documented index rule; Critic overrides. Empty menu is invalid configuration.
- Stage 3 only: void recoverSatisfaction(int amount), capped at 100; controller decides eligibility.

Customer methods above have no checked exceptions. Labels: Regular, VIP, Critic. No recovery method in Stage 1/2. Do not add VIP/Critic classes early.

### Menu and Order: ZN

- MenuItem(String name, int priceInCents, int preparationUnits); String getName(), int getPriceInCents(), int getPreparationUnits(). Nonnegative price, positive units, no public setters.
- Menu(): initially empty. void addItem(MenuItem item), List<MenuItem> getItems() (unmodifiable), MenuItem findItemByName(String name) (case-insensitive, null if missing).
- Stage 2 only: MenuItem getMostExpensiveItem(), first on ties; empty menu is invalid configuration.
- Order(MenuItem item, int billInCents): IN_PROGRESS, remaining prep = item units; retain item reference and locked bill.
- MenuItem getMenuItem(), OrderStatus getStatus(), int getRemainingPrepUnits(), int getBillInCents(), boolean isReady().
- void decrementPrepUnit() throws InvalidActionException: require IN_PROGRESS, subtract one, automatically READY at zero, never negative.
- void markServed(), void markPaid(), void cancel(), all throwing InvalidActionException for an invalid source state. READY to SERVED; SERVED to PAID; IN_PROGRESS/READY to CANCELLED. Terminal states reject further processing.
- Stage 3 only: void applyDiscount(int percentOff) throws InvalidActionException. Validate percentage 0..100 and IN_PROGRESS. Controller invokes once immediately after order creation, before Chef action; never on later turns.
- Stage 3 ComboMeal(String name, MenuItem a, MenuItem b) extends MenuItem. Constructor calculates rounded 90% summed price and summed units once. MenuItem getComponentA()/getComponentB() return existing component references.

Use Math.round with floating-point arithmetic before int conversion; avoid integer truncation. VIP discount/rounding happens before Happy Hour discount/rounding. No general public setters for order status, remaining prep or bill.

### Queue and Table: ZN

- WaitingQueue(): void addCustomer(Customer c), boolean removeCustomer(Customer c), Customer getNextCustomer(), int getQueueSize(), boolean isEmpty(), List<Customer> getCustomers().
- getNextCustomer peeks, never removes, returns null if empty. getCustomers is unmodifiable. Prevent duplicates and invalid non-QUEUED additions.
- Table(int tableId): int getTableId(), boolean isOccupied(), Customer getCurrentCustomer(), void assignCustomer(Customer c) throws InvalidActionException, void freeTable().
- Empty table customer is null; occupancy derives from currentCustomer != null. assignCustomer validates free table/customer; seating staff updates customer state.
- Controller uses snapshots when removing customers during iteration, not mutation through exposed lists.

### Staff: ZN

Staff is abstract: protected Staff(String name); public String getName(), boolean hasActedThisTurn(), void resetForNewTurn(); protected void markActed(). Each concrete staff constructor takes String name. Flags initially false.

Waiter methods, all throwing InvalidActionException:
- void seatCustomer(WaitingQueue queue, Customer customer, Table table): require unused Waiter, queued member and free table; remove from queue, assign table, set SEATED, mark acted.
- Order takeOrder(Customer customer, Menu menu): require unused Waiter, SEATED and no order. Call customer.chooseItem(menu), compute customer.calculateBill(item price), create and attach Order, mark acted, return Order. Player does not choose item.
- void serveOrder(Customer customer, int turn): require unused Waiter, SEATED and READY order; mark order SERVED, customer EATING/servedTurn, mark acted.

Chef: void advanceOrder(Order order) throws InvalidActionException. Require unused Chef and non-null IN_PROGRESS order; advance one unit then mark acted.

Cashier: Customer collectPayment(List<Table> tables, int turn) throws InvalidActionException. Select READY_TO_PAY with readyToPayTurn < turn; earliest timestamp first, lower customerId on ties. Mark customer/order PAID, free table, mark acted, return Customer. Return null if already acted or nobody eligible. Returned customer retains paid Order and satisfaction for controller aggregation. Do not update revenue here. Exception indicates inconsistent domain state.

Stage 3 Host: Customer seatNextCustomer(WaitingQueue queue, List<Table> tables) throws InvalidActionException. Seat queue head at lowest-ID free table, set SEATED, remove from queue, mark Host acted, return customer. Return null if already acted, empty queue or no free table. Never consume Waiter action. Exception indicates inconsistent domain state.

All expected failures validate before mutation so partial actions cannot corrupt state. Wait is handled by CLI making no staff call and advancing to the next phase.

### PolicyManager: ZN, Stage 3 only

PolicyManager(); void activateHappyHour() throws InvalidActionException; void decrementHappyHourTimer(); boolean isHappyHourActive(); int getRemainingTurns(); void applyDiscount(Order order) throws InvalidActionException; int getSatisfactionRecovery().

Initial unused/inactive/0. Activate sets used and remaining 2. Repeat activation always fails, including after expiry. Active means remaining > 0. End-turn decrement never goes below 0. Recovery getter returns 15. applyDiscount does nothing when inactive, otherwise calls order.applyDiscount(20). No customer loops or recovery inside PolicyManager: controller owns coordination.

### Support and Devansh-owned API

- InvalidActionException(String message) extends Exception, owned by ZN.
- ArrivalSchedule.createBaseSchedule(): static factory for six stage-appropriate pre-created customers. Customer getArrivalFor(int turn): return customer or null, no mutation; controller calls once per turn.
- Stage 1 GameController constructor: (List<Table> tables, WaitingQueue waitingQueue, Waiter waiter, Chef chef, Cashier cashier, Menu menu, ArrivalSchedule arrivalSchedule). Stage 3 adds Host host, PolicyManager policyManager as final two parameters.
- void beginTurn(); void finishTurn() throws InvalidActionException.
- void waiterSeat(int customerId, int tableId), waiterTakeOrder(int tableId), waiterServe(int tableId), chefAdvance(int tableId), all throwing InvalidActionException. Resolve/validate IDs before staff calls.
- Stage 3 void activateHappyHour() throws InvalidActionException; reject activation outside start-of-turn activation phase.
- Getters: int getCurrentTurn(), getTurnsUsed(), getRevenueInCents(), getPaidCustomerCount(), getServedCount(), getAbandonedCount(); double getAverageSatisfaction(); List<Table> getTables(); WaitingQueue getWaitingQueue().
- Average getter may return 0 before payments; CLI uses paid count to display N/A. Current turn may become 19 after completion, but turns used stays 18.
- boolean hasWaiterActedThisTurn(), hasChefActedThisTurn(), isGameOver(), isVictory(). Victory is final only after turn 18 and uses unrounded paid average.
- Stage 3 display getters: boolean isHappyHourActive(), hasHappyHourBeenUsed(); int getHappyHourRemainingTurns(). Controller can track successful activation for the used getter.
- GameCLI(GameController controller, Scanner scanner); void run(), displayTurnStatus(), promptWaiterAction(), promptChefAction(), displayError(String message), displayTurnResults(), displayFinalResults(). Stage 3 adds promptHappyHour().
- Main.main(String[] args) constructs configuration and calls cli.run(). Controller never calls UI. Explain object construction/wiring to the diagram team when checking composition against actual code.

## 6. Exact turn sequence: Devansh implements

Start currentTurn at 1. GameCLI owns the loop and calls controller methods; game rules remain in controller/domain, not Scanner/printing code.

1. beginTurn: reset staff flags; add scheduled arrival. Stage 3: recover eligible customers if Happy Hour carried over from last turn, then Host auto-seats once. Guard duplicate beginTurn calls.
2. Display state. Stage 3 prompt activation before Waiter/Chef choices. Successful new activation immediately recovers eligible customers once. Never apply both carried-over and new-activation recovery to the same customer in one turn. Refresh policy display after activation.
3. Prompt one successful Waiter action or Wait. Waiter may select any queued customer for seating. Ordering/serving uses table ID. In Stage 3, waiterTakeOrder immediately passes the returned Order to policyManager.applyDiscount(order), after the customer's VIP discount and before Chef action. Increment servedCount only after successful serve.
4. Prompt one successful Chef action or Wait. A newly taken order can be advanced now. Waiter already acted, so a dish completed now is served next turn at the earliest.
5. finishTurn: for EATING customers with servedTurn < currentTurn, mark READY_TO_PAY with current turn. Cashier then pays at most one customer with readyToPayTurn < currentTurn. Aggregate returned customer's bill, satisfaction and paid count once. Cashier already freed the table.
6. Lose satisfaction for every still-waiting queued/seated customer. At 0, mark ABANDONED, cancel IN_PROGRESS/READY order if any, remove from queue or free table, increment abandonedCount once. No revenue for departures.
7. Decrement Happy Hour timer once; record turns used, increment turn, display end-turn results. After turn 18 display Victory/Defeat; never execute turn 19.

Recovery and Host order follows v3: on a new activation the Host may already have seated a customer, but that SEATED/unserved customer is still eligible. Arrivals are included, capped at 100.

Display turn, revenue, paid average, queue, tables, customer IDs/types/satisfaction, order status/prep, chosen item after ordering and available staff tasks. Display Happy Hour duration when active. Turn-end results include served count, unhappy departures, revenue, paid average and turns used.

Malformed input, missing IDs, occupied table, duplicate order and premature serve must explain failure and retry without using the staff task. After successful action or Wait, move to next phase. EOF exits cleanly as an interrupted session, without falsely declaring a completed game.

## 7. Stage deliveries and integration gates

### Stage 1

ZN delivers all Stage 1 domain files, independent check runner and compile/check commands. No imports of Devansh's files. Devansh delivers four orchestration files, build/run instructions, integration checks and reproducible input scripts; use local contract stubs temporarily if necessary.

Devansh merges ZN's real code, excludes all stubs from build paths, fixes wiring and runs both check suites. ZN fixes domain defects. Verify full game and reproducible victory/defeat, then tag stage-1. No VIP, Critic, Host, ComboMeal, PolicyManager, recovery or policy discount features in this release.

### Stage 2

Both start from verified stage-1. ZN adds VIPCustomer, CriticCustomer and Menu.getMostExpensiveItem. Devansh changes arrival factory and integration scenarios. Common controller/CLI should remain unchanged. Integrate, verify six arrivals and original rules, record changed/new files and compare link, then tag stage-2.

### Stage 3

Both start from verified stage-2. ZN adds Host, ComboMeal, PolicyManager and the Customer/Order extensions with focused checks. Devansh integrates policy/Host, CLI prompt/display and combo setup. Keep Stage 2 arrival schedule. Integrate, run Stage 3 acceptance cases, record transitions and tag stage-3. Share final API changes with diagram/documentation members.

## 8. Required verification

### ZN: domain checks

- Failed actions leave staff flag and every affected object unchanged; successful actions cannot repeat before reset.
- Prep never negative; only READY serves; terminal orders cannot process again.
- isWaiting covers QUEUED/SEATED, including READY dishes, but excludes EATING/READY_TO_PAY/PAID/ABANDONED.
- Satisfaction bounded 0..100; subtype losses 8/5/12.
- Cashier excludes newly-ready customers; timestamp ordering and ID tie-break work.
- VIP Burger = 1350 cents; during Happy Hour = 1080 cents. Combo = 2430 cents/3 units. Include fractional-cent rounding checks.
- Happy Hour duration 2 -> 1 -> 0; reactivation rejected; Host and Waiter independent.

### Devansh: integrated checks

- Two tables, six scheduled arrivals, all 18 turns, no early ending.
- Fixed Regular Pasta scenario: seat turn 1; order/Chef unit 1 turn 2; READY turn 3; serve turn 4; READY_TO_PAY turn 5; pay turn 6. Satisfaction at payment = 76. Use a controlled scenario with Pasta selection, not the default customer-1 index choice (which is Salad).
- One-unit dish: order/cook, later serve, next turn finish eating, following turn pay.
- Abandonment cleans queue/table/order with no revenue; paid totals remain correct after table reuse.
- Reproducible actual Stage 1 victory and defeat. Verify action sequences before calling them evidence.
- Check exact thresholds 4 paid/4500 cents/60 average and cases failing each target. Zero payments shows N/A. Turn-16 arrival cannot pay by 18 in Stage 1 base flow.
- Stage 2 contains VIP and Critic; Critic picks Pasta; VIP discount works through polymorphism.
- Stage 3 Host seats at most one; Waiter can independently seat another when capacity exists.
- Happy Hour recovers once on activation and once next turn, discounts only new active-turn orders, never reprices old orders; no recovery for departed/paid/eating customers. Include activation on turn 18.
- Input retry does not consume action. Verify invalid text, IDs, ordering and serving.
- Compile/run each release from a clean checkout without stubs or network dependencies.

Store input files, expected results and exact commands. Tests should check meaningful behaviour, not mirror getters. Stub results are not integrated game evidence.

## 9. GitHub workflow and handoff

Use separate owner/stage branches, e.g. devansh/stage-1 and zn/stage-1, then fresh branches from each integrated stage. These are intended names, not branches already created.

Each handoff includes owned source files, checks, run commands and explicit API deviations (ideally none). Devansh integrates, runs real checks, records stage changes and tags runnable commits. ZN reviews domain-related integration corrections. Each coder reviews/understands AI-generated code and commits their own implementation.

Keep generated binaries, credentials, course documents and temporary stubs out of the public repo. Add collaborators only using supplied GitHub usernames; do not infer ZN's account from the name.

## 10. Sequential fallback

If throwaway stubs feel inconvenient, use the same split sequentially within each stage:
1. ZN finishes/tests that stage's independent domain package and hands it over.
2. Devansh implements/integrates controller/CLI against the real objects, verifies full game and tags the stage.
3. Repeat for Stage 2, then Stage 3.

This is simpler but delays Devansh's integration coding. Devansh can prepare CLI design, test scenarios and scripted inputs while ZN codes. Do not wait for ZN to build all three stages first: integrate runnable stages in sequence.

## 11. Copyable prompts for AI coding sessions

### ZN's prompt

Read CODING_PLAN.md completely, the approved Refined v3 and assignment brief. I am ZN. Implement only my domain/staff files for the current stage, using section 5's exact API, validation, return and exception semantics. My production package must compile without Devansh's controller, CLI, Main or ArrivalSchedule. Do not add UI, global loop, revenue aggregation or arrival scheduling to domain classes. Implement focused checks in section 8. Do not add later-stage features early, rename shared signatures or edit Devansh's files. Identify any precise contract contradiction before changing it. Deliver source, check commands and deviations. Explain the implementation so I can understand and present it.

### Devansh's prompt

Read CODING_PLAN.md completely, the approved Refined v3 and assignment brief. I am Devansh. Implement only my current-stage controller, CLI, Main, ArrivalSchedule and integration checks. Use section 5's exact API and section 6's timing. Do not duplicate ZN's domain rules or silently change signatures. If domain code is unavailable, use isolated ignored contract stubs for wiring checks only. Keep production offline, customer food selection automatic and invalid choices retryable. Integrate ZN's real implementation, run both check suites and actual victory/defeat scenarios, and record evidence before creating a release tag. Explain the code and integration corrections so I can understand and present them.
