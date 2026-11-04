package restaurantrush;

/**
 * Abstract customer. Satisfaction is private and can only change through
 * loseSatisfaction() (uses the subclass's own loss rate). Subclasses supply the
 * loss rate, the bill rule and the label, so callers never need instanceof.
 */
public abstract class Customer {
    private static final int MAX_SATISFACTION = 100;

    private final int customerId;
    private int satisfaction;
    private CustomerState state;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    protected Customer(int customerId) {
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer id must be at least 1");
        }
        this.customerId = customerId;
        this.satisfaction = MAX_SATISFACTION;
        this.state = CustomerState.QUEUED;
        this.order = null;
        this.servedTurn = -1;
        this.readyToPayTurn = -1;
    }

    // ---- simple getters ----
    public int getCustomerId() {
        return customerId;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public Order getOrder() {
        return order;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    // ---- guarded state changes ----

    /**
     * Allowed here: QUEUED->SEATED, QUEUED/SEATED->ABANDONED, READY_TO_PAY->PAID.
     * EATING and READY_TO_PAY carry a turn stamp, so they only come from
     * markServed() / markReadyToPay().
     */
    public void setState(CustomerState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("State must not be null");
        }
        boolean legal;
        switch (newState) {
            case SEATED:
                legal = state == CustomerState.QUEUED;
                break;
            case ABANDONED:
                legal = state == CustomerState.QUEUED || state == CustomerState.SEATED;
                break;
            case PAID:
                legal = state == CustomerState.READY_TO_PAY;
                break;
            default:
                legal = false;
        }
        if (!legal) {
            throw new IllegalStateException("Illegal customer transition " + state + " -> " + newState);
        }
        state = newState;
    }

    /** Attach the order once. Only a seated customer can have one. */
    public void setOrder(Order newOrder) {
        if (newOrder == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order != null) {
            throw new IllegalStateException("Customer " + customerId + " already has an order");
        }
        if (state != CustomerState.SEATED) {
            throw new IllegalStateException("Only a seated customer can have an order (state " + state + ")");
        }
        order = newOrder;
    }

    /** QUEUED, or SEATED and still waiting for food (including a dish that is READY but not served). */
    public boolean isWaiting() {
        return state == CustomerState.QUEUED || state == CustomerState.SEATED;
    }

    /** Subtract this customer's own loss rate; never below 0. */
    public void loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossRate());
    }

    /** SEATED + SERVED order -> EATING. */
    public void markServed(int turn) {
        if (state != CustomerState.SEATED) {
            throw new IllegalStateException("Only a seated customer can be served (state " + state + ")");
        }
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("The order must be marked served first");
        }
        state = CustomerState.EATING;
        servedTurn = turn;
    }

    /** EATING -> READY_TO_PAY, only on a turn after the one in which the food was served. */
    public void markReadyToPay(int turn) {
        if (state != CustomerState.EATING) {
            throw new IllegalStateException("Only an eating customer can become ready to pay (state " + state + ")");
        }
        if (turn <= servedTurn) {
            throw new IllegalStateException("Customer must eat for at least one turn");
        }
        state = CustomerState.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    /**
     * Documented food rule: the item at index (customerId - 1) modulo menu size.
     * Stage 1 menu (Salad, Burger, Pasta): customers 1..6 order Salad, Burger, Pasta, Salad, Burger, Pasta.
     */
    public MenuItem chooseItem(Menu menu) {
        if (menu == null || menu.getItems().isEmpty()) {
            throw new IllegalArgumentException("Menu must contain at least one item");
        }
        int size = menu.getItems().size();
        return menu.getItems().get((customerId - 1) % size);
    }

    // ---- subclass hooks (polymorphism) ----
    public abstract int getSatisfactionLossRate();

    public abstract int calculateBill(int baseCents);

    public abstract String getTypeLabel();

    @Override
    public String toString() {
        return getTypeLabel() + " #" + customerId;
    }
}
