package restaurantrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Coordinates turns; staff/domain objects retain their own rules. No console I/O. */
public class GameController {
    public static final int MAX_TURNS = 18;
    private enum Phase { BETWEEN_TURNS, WAITER, CHEF }
    private Phase phase = Phase.BETWEEN_TURNS;
    private int currentTurn = 1;
    private int paidCustomerCount;
    private int totalRevenueInCents;
    private int paidSatisfactionTotal;
    private int servedCount;
    private int abandonedCount;
    private final List<Table> tables;
    private final WaitingQueue waitingQueue;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final Menu menu;
    private final ArrivalSchedule arrivalSchedule;
    private final Host host;
    private final PolicyManager policyManager;
    private boolean happyHourUsed;
    private final List<String> turnEvents = new ArrayList<>();

    public GameController(List<Table> tables, WaitingQueue waitingQueue, Waiter waiter,
                          Chef chef, Cashier cashier, Menu menu, ArrivalSchedule arrivalSchedule) {
        this(tables, waitingQueue, waiter, chef, cashier, menu, arrivalSchedule, null, null);
    }

    public GameController(List<Table> tables, WaitingQueue waitingQueue, Waiter waiter,
                          Chef chef, Cashier cashier, Menu menu, ArrivalSchedule arrivalSchedule,
                          Host host, PolicyManager policyManager) {
        this.tables = List.copyOf(tables);
        this.waitingQueue = Objects.requireNonNull(waitingQueue);
        this.waiter = Objects.requireNonNull(waiter);
        this.chef = Objects.requireNonNull(chef);
        this.cashier = Objects.requireNonNull(cashier);
        this.menu = Objects.requireNonNull(menu);
        this.arrivalSchedule = Objects.requireNonNull(arrivalSchedule);
        this.host = host;
        this.policyManager = policyManager;
        if (this.tables.isEmpty() || menu.getItems().isEmpty()
                || this.tables.stream().map(Table::getTableId).distinct().count() != this.tables.size()) {
            throw new IllegalArgumentException("Need a nonempty menu and uniquely identified tables");
        }
    }

    public void beginTurn() {
        if (isGameOver() || phase != Phase.BETWEEN_TURNS) {
            throw new IllegalStateException("Cannot begin this turn again or after the game ends");
        }
        turnEvents.clear();
        waiter.resetForNewTurn(); chef.resetForNewTurn(); cashier.resetForNewTurn();
        if (host != null) { host.resetForNewTurn(); }
        Customer arrival = arrivalSchedule.getArrivalFor(currentTurn);
        if (arrival != null) {
            waitingQueue.addCustomer(arrival);
            turnEvents.add(arrival + " arrived.");
        }
        phase = Phase.WAITER;
        if (isHappyHourActive()) { recoverWaitingCustomers(); }
        if (host != null) {
            try {
                Customer seated = host.seatNextCustomer(waitingQueue, tables);
                if (seated != null) { turnEvents.add("Host seated " + seated + "."); }
            } catch (InvalidActionException e) {
                throw new IllegalStateException("Automatic Host encountered inconsistent state", e);
            }
        }
    }

    public void activateHappyHour() throws InvalidActionException {
        requirePhase(Phase.WAITER);
        if (policyManager == null || waiter.hasActedThisTurn()) {
            throw new InvalidActionException("Happy Hour is available only before this turn's staff actions.");
        }
        policyManager.activateHappyHour();
        happyHourUsed = true;
        recoverWaitingCustomers();
        turnEvents.add("Happy Hour activated for this turn and the next.");
    }

    private void recoverWaitingCustomers() {
        List<Customer> customers = new ArrayList<>(waitingQueue.getCustomers());
        for (Table table : tables) {
            if (table.isOccupied()) { customers.add(table.getCurrentCustomer()); }
        }
        for (Customer c : customers) {
            if (c.isWaiting()) { c.recoverSatisfaction(policyManager.getSatisfactionRecovery()); }
        }
        turnEvents.add("Happy Hour recovered waiting customers' satisfaction (maximum 100).");
    }

    public void waiterSeat(int customerId, int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = waitingQueue.getCustomers().stream()
                .filter(c -> c.getCustomerId() == customerId).findFirst()
                .orElseThrow(() -> new InvalidActionException("No queued customer #" + customerId));
        waiter.seatCustomer(waitingQueue, customer, table(tableId));
        turnEvents.add(customer + " seated at table " + tableId + ".");
    }

    public void waiterTakeOrder(int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = occupant(tableId);
        Order order = waiter.takeOrder(customer, menu);
        if (policyManager != null) { policyManager.applyDiscount(order); }
        turnEvents.add(customer + " chose " + order.getMenuItem().getName()
                + "; locked bill " + order.getBillInCents() + " cents.");
    }

    public void waiterServe(int tableId) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Customer customer = occupant(tableId);
        waiter.serveOrder(customer, currentTurn);
        servedCount++;
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
            totalRevenueInCents += paid.getOrder().getBillInCents();
            paidSatisfactionTotal += paid.getSatisfaction();
            paidCustomerCount++;
            turnEvents.add(paid + " paid " + paid.getOrder().getBillInCents() + " cents; table freed.");
        }
        for (Customer c : new ArrayList<>(waitingQueue.getCustomers())) {
            if (losePatience(c)) { waitingQueue.removeCustomer(c); }
        }
        for (Table table : tables) {
            Customer c = table.getCurrentCustomer();
            if (c != null && losePatience(c)) { table.freeTable(); }
        }
        if (policyManager != null) { policyManager.decrementHappyHourTimer(); }
        currentTurn++;
        phase = Phase.BETWEEN_TURNS;
    }

    private boolean losePatience(Customer customer) throws InvalidActionException {
        if (!customer.isWaiting()) { return false; }
        customer.loseSatisfaction();
        if (customer.getSatisfaction() > 0) { return false; }
        if (customer.getOrder() != null) { customer.getOrder().cancel(); }
        customer.setState(CustomerState.ABANDONED);
        abandonedCount++;
        turnEvents.add(customer + " left unhappy without paying.");
        return true;
    }

    private void requirePhase(Phase expected) throws InvalidActionException {
        if (phase != expected || isGameOver()) {
            throw new InvalidActionException("That action is unavailable in the current turn phase.");
        }
    }

    private Table table(int id) throws InvalidActionException {
        return tables.stream().filter(t -> t.getTableId() == id).findFirst()
                .orElseThrow(() -> new InvalidActionException("Unknown table #" + id));
    }

    private Customer occupant(int id) throws InvalidActionException {
        Customer customer = table(id).getCurrentCustomer();
        if (customer == null) { throw new InvalidActionException("Table " + id + " is empty."); }
        return customer;
    }

    public int getCurrentTurn() { return currentTurn; }
    public int getTurnsUsed() { return currentTurn - 1; }
    public int getRevenueInCents() { return totalRevenueInCents; }
    public int getPaidCustomerCount() { return paidCustomerCount; }
    public int getServedCount() { return servedCount; }
    public int getAbandonedCount() { return abandonedCount; }
    public double getAverageSatisfaction() {
        return paidCustomerCount == 0 ? 0 : (double) paidSatisfactionTotal / paidCustomerCount;
    }
    public List<Table> getTables() { return tables; }
    public WaitingQueue getWaitingQueue() { return waitingQueue; }
    public List<MenuItem> getMenuItems() { return menu.getItems(); }
    public List<String> getTurnEvents() { return List.copyOf(turnEvents); }
    public boolean hasWaiterActedThisTurn() { return waiter.hasActedThisTurn(); }
    public boolean hasChefActedThisTurn() { return chef.hasActedThisTurn(); }
    public boolean hasPolicyManager() { return policyManager != null; }
    public boolean isHappyHourActive() { return policyManager != null && policyManager.isHappyHourActive(); }
    public boolean hasHappyHourBeenUsed() { return happyHourUsed; }
    public int getHappyHourRemainingTurns() { return policyManager == null ? 0 : policyManager.getRemainingTurns(); }
    public boolean isGameOver() { return currentTurn > MAX_TURNS; }
    public boolean isVictory() {
        return isGameOver() && meetsTargets(paidCustomerCount, totalRevenueInCents, paidSatisfactionTotal);
    }
    static boolean meetsTargets(int paid, int cents, int satisfactionTotal) {
        return paid >= 4 && cents >= 4500 && satisfactionTotal >= 60L * paid;
    }
}
