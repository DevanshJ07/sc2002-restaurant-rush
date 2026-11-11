package restaurantrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Control class: runs the turn phases in the required order and records what happened.
 * The rules themselves stay inside the staff and domain objects; this class only decides
 * who acts when. It never reads or prints console text, so it can be tested without the CLI.
 */
public class GameController {
    public static final int MAX_TURNS = 18;
    private enum Phase { BETWEEN_TURNS, HOST, WAITER, CHEF }
    private Phase phase = Phase.BETWEEN_TURNS;
    private int currentTurn = 1;
    private final GameStats stats = new GameStats();
    private final List<Table> tables;
    private final WaitingQueue waitingQueue;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final Menu menu;
    private final ArrivalSchedule arrivalSchedule;
    private final Host host;
    private final PolicyManager policyManager;
    private final List<String> turnEvents = new ArrayList<>();

    /** Core game without a Host or Happy Hour (the Stage 1 and Stage 2 setup). */
    public GameController(List<Table> tables, WaitingQueue waitingQueue, Waiter waiter,
                          Chef chef, Cashier cashier, Menu menu, ArrivalSchedule arrivalSchedule) {
        this(tables, waitingQueue, waiter, chef, cashier, menu, arrivalSchedule, null, null);
    }

    public GameController(List<Table> tables, WaitingQueue waitingQueue, Waiter waiter,
                          Chef chef, Cashier cashier, Menu menu, ArrivalSchedule arrivalSchedule,
                          Host host, PolicyManager policyManager) {
        // host and policyManager may be null: the game then simply skips the Host phase and Happy Hour.
        this.tables = List.copyOf(tables);
        this.waitingQueue = Objects.requireNonNull(waitingQueue);
        this.waiter = Objects.requireNonNull(waiter);
        this.chef = Objects.requireNonNull(chef);
        this.cashier = Objects.requireNonNull(cashier);
        this.menu = Objects.requireNonNull(menu);
        this.arrivalSchedule = Objects.requireNonNull(arrivalSchedule);
        this.host = host;
        this.policyManager = policyManager;
        if (this.tables.isEmpty() || menu.getItems().isEmpty()) {
            throw new IllegalArgumentException("Need a nonempty menu and uniquely identified tables");
        }
        for (int i = 0; i < this.tables.size(); i++) {
            for (int j = i + 1; j < this.tables.size(); j++) {
                if (this.tables.get(i).getTableId() == this.tables.get(j).getTableId()) {
                    throw new IllegalArgumentException("Table IDs must be unique");
                }
            }
        }
    }

    public void beginTurn() {
        if (isGameOver() || phase != Phase.BETWEEN_TURNS) {
            throw new IllegalStateException("Cannot begin this turn again or after the game ends");
        }
        turnEvents.clear();
        waiter.resetForNewTurn();
        chef.resetForNewTurn();
        cashier.resetForNewTurn();
        if (host != null) {
            host.resetForNewTurn();
        }
        Customer arrival = arrivalSchedule.getArrivalFor(currentTurn);
        if (arrival != null) {
            waitingQueue.addCustomer(arrival);
            turnEvents.add(arrival + " arrived.");
        }
        phase = host == null ? Phase.WAITER : Phase.HOST;
        if (isHappyHourActive()) {
            recoverWaitingCustomers();
        }
    }

    public void hostSeat() throws InvalidActionException {
        requirePhase(Phase.HOST);
        if (host.hasActedThisTurn()) {
            throw new InvalidActionException("The Host has already seated a customer this turn.");
        }
        Customer seated = host.seatNextCustomer(waitingQueue, tables);
        if (seated == null) {
            throw new InvalidActionException(waitingQueue.isEmpty()
                    ? "No customers are waiting in the queue." : "No tables are free.");
        }
        turnEvents.add("Host seated " + seated + ".");
    }

    public void finishHostPhase() throws InvalidActionException {
        requirePhase(Phase.HOST);
        phase = Phase.WAITER;
    }

    public void activateHappyHour() throws InvalidActionException {
        requirePhase(host == null ? Phase.WAITER : Phase.HOST);
        boolean staffHaveActed = waiter.hasActedThisTurn() || (host != null && host.hasActedThisTurn());
        if (policyManager == null || staffHaveActed) {
            throw new InvalidActionException(
                    "Happy Hour is available only before this turn's staff actions.");
        }
        policyManager.activateHappyHour();
        recoverWaitingCustomers();
        turnEvents.add("Happy Hour activated for this turn and the next.");
    }

    private void recoverWaitingCustomers() {
        List<Customer> customers = new ArrayList<>(waitingQueue.getCustomers());
        for (Table table : tables) {
            if (table.isOccupied()) {
                customers.add(table.getCurrentCustomer());
            }
        }
        for (Customer c : customers) {
            if (c.isWaiting()) {
                c.recoverSatisfaction(policyManager.getSatisfactionRecovery());
            }
        }
        turnEvents.add("Happy Hour recovered waiting customers' satisfaction (maximum 100).");
    }

    public void waiterSeat(int customerId, int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = null;
        for (Customer queuedCustomer : waitingQueue.getCustomers()) {
            if (queuedCustomer.getCustomerId() == customerId) {
                customer = queuedCustomer;
                break;
            }
        }
        if (customer == null) {
            throw new InvalidActionException("No queued customer #" + customerId);
        }
        waiter.seatCustomer(waitingQueue, customer, table(tableId));
        turnEvents.add(customer + " seated at table " + tableId + ".");
    }

    public void waiterTakeOrder(int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = occupant(tableId);
        Order order = waiter.takeOrder(customer, menu);
        if (policyManager != null) {
            policyManager.applyDiscount(order);
        }
        turnEvents.add(String.format(Locale.ROOT, "%s chose %s; locked bill $%.2f.",
                customer, order.getMenuItem().getName(), order.getBillInCents() / 100.0));
    }

    public void waiterServe(int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = occupant(tableId);
        waiter.serveOrder(customer, currentTurn);
        stats.recordServed();
        turnEvents.add(customer + " served; now eating.");
    }

    /** Explicitly ends the Waiter's opportunity, including when the player chooses Wait. */
    public void finishWaiterPhase() throws InvalidActionException {
        requirePhase(Phase.WAITER);
        phase = Phase.CHEF;
    }

    public void chefAdvance(int tableId) throws InvalidActionException {
        requirePhase(Phase.CHEF);
        Order order = occupant(tableId).getOrder();
        chef.advanceOrder(order);
        turnEvents.add("Chef advanced table " + tableId + ": " + order.getStatus() + ".");
    }

    public void finishTurn() throws InvalidActionException {
        requirePhase(Phase.CHEF);
        for (Table table : tables) {
            Customer c = table.getCurrentCustomer();
            if (c != null && c.getState() == CustomerState.EATING && c.getServedTurn() < currentTurn) {
                c.markReadyToPay(currentTurn);
                turnEvents.add(c + " finished eating; payment eligible next turn.");
            }
        }
        Customer paid = cashier.collectPayment(tables, currentTurn);
        if (paid != null) {
            stats.recordPayment(paid.getOrder().getBillInCents(), paid.getSatisfaction());
            turnEvents.add(String.format(Locale.ROOT, "%s paid $%.2f; table freed.",
                    paid, paid.getOrder().getBillInCents() / 100.0));
        }
        for (Customer c : new ArrayList<>(waitingQueue.getCustomers())) {
            if (losePatience(c)) {
                waitingQueue.removeCustomer(c);
            }
        }
        for (Table table : tables) {
            Customer c = table.getCurrentCustomer();
            if (c != null && losePatience(c)) {
                table.freeTable();
            }
        }
        if (policyManager != null) {
            policyManager.decrementHappyHourTimer();
        }
        currentTurn++;
        phase = Phase.BETWEEN_TURNS;
    }

    private boolean losePatience(Customer customer) throws InvalidActionException {
        if (!customer.isWaiting()) {
            return false;
        }
        customer.loseSatisfaction();
        if (customer.getSatisfaction() > 0) {
            return false;
        }
        if (customer.getOrder() != null) {
            customer.getOrder().cancel();
        }
        customer.abandon();
        stats.recordAbandoned();
        turnEvents.add(customer + " left unhappy without paying.");
        return true;
    }

    private void requirePhase(Phase expected) throws InvalidActionException {
        if (phase != expected || isGameOver()) {
            throw new InvalidActionException("That action is unavailable in the current turn phase.");
        }
    }

    private Table table(int id) throws InvalidActionException {
        for (Table table : tables) {
            if (table.getTableId() == id) {
                return table;
            }
        }
        throw new InvalidActionException("Unknown table #" + id);
    }

    private Customer occupant(int id) throws InvalidActionException {
        Customer customer = table(id).getCurrentCustomer();
        if (customer == null) {
            throw new InvalidActionException("Table " + id + " is empty.");
        }
        return customer;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getTurnsUsed() {
        return currentTurn - 1;
    }

    public int getRevenueInCents() {
        return stats.getRevenueInCents();
    }

    public int getPaidCustomerCount() {
        return stats.getPaidCustomerCount();
    }

    public int getServedCount() {
        return stats.getServedCount();
    }

    public int getAbandonedCount() {
        return stats.getAbandonedCount();
    }

    public double getAverageSatisfaction() {
        return stats.getAverageSatisfaction();
    }

    public List<Table> getTables() {
        return tables;
    }

    public WaitingQueue getWaitingQueue() {
        return waitingQueue;
    }

    public List<MenuItem> getMenuItems() {
        return menu.getItems();
    }

    public List<String> getTurnEvents() {
        return List.copyOf(turnEvents);
    }

    public boolean hasWaiterActedThisTurn() {
        return waiter.hasActedThisTurn();
    }

    public boolean hasPolicyManager() {
        return policyManager != null;
    }

    public boolean hasHost() {
        return host != null;
    }

    public boolean isHappyHourActive() {
        return policyManager != null && policyManager.isHappyHourActive();
    }

    public boolean hasHappyHourBeenUsed() {
        return policyManager != null && policyManager.hasBeenUsed();
    }

    public int getHappyHourRemainingTurns() {
        return policyManager == null ? 0 : policyManager.getRemainingTurns();
    }

    public boolean isGameOver() {
        return currentTurn > MAX_TURNS;
    }

    public boolean isVictory() {
        return isGameOver() && stats.meetsTargets();
    }
}
