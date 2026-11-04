package restaurantrush;

/**
 * One customer's order. All fields are private and there are no setters for status,
 * prep units or bill: the order can only move through guarded transitions.
 *
 *   IN_PROGRESS --(prep reaches 0)--> READY --> SERVED --> PAID
 *   IN_PROGRESS / READY --> CANCELLED
 */
public class Order {
    private final MenuItem menuItem;
    private OrderStatus status;
    private int remainingPrepUnits;
    private int billInCents;

    public Order(MenuItem item, int billInCents) {
        if (item == null) {
            throw new IllegalArgumentException("Order needs a menu item");
        }
        if (billInCents < 0) {
            throw new IllegalArgumentException("Bill must not be negative");
        }
        this.menuItem = item;
        this.billInCents = billInCents;
        this.remainingPrepUnits = item.getPreparationUnits();
        this.status = OrderStatus.IN_PROGRESS;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getRemainingPrepUnits() {
        return remainingPrepUnits;
    }

    public int getBillInCents() {
        return billInCents;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** One unit of chef work. Becomes READY automatically when no units remain. */
    public void decrementPrepUnit() throws InvalidActionException {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new InvalidActionException("This order is not being cooked (it is " + describe() + ").");
        }
        remainingPrepUnits--;
        if (remainingPrepUnits == 0) {
            status = OrderStatus.READY;
        }
    }

    /**
     * Stage 3: reduce the locked bill by percentOff, rounded to the nearest cent.
     * Only while IN_PROGRESS, so it can never reprice an order that is already cooked or served.
     */
    public void applyDiscount(int percentOff) throws InvalidActionException {
        if (percentOff < 0 || percentOff > 100) {
            throw new InvalidActionException("Discount must be between 0 and 100 percent.");
        }
        if (status != OrderStatus.IN_PROGRESS) {
            throw new InvalidActionException("A discount can only be applied while the order is in progress.");
        }
        billInCents = (int) Math.round(billInCents * (100 - percentOff) / 100.0);
    }

    public void markServed() throws InvalidActionException {
        if (status != OrderStatus.READY) {
            throw new InvalidActionException("The order is not ready to be served (it is " + describe() + ").");
        }
        status = OrderStatus.SERVED;
    }

    public void markPaid() throws InvalidActionException {
        if (status != OrderStatus.SERVED) {
            throw new InvalidActionException("Only a served order can be paid (it is " + describe() + ").");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() throws InvalidActionException {
        if (status != OrderStatus.IN_PROGRESS && status != OrderStatus.READY) {
            throw new InvalidActionException("This order can no longer be cancelled (it is " + describe() + ").");
        }
        status = OrderStatus.CANCELLED;
    }

    private String describe() {
        return status.name().toLowerCase().replace('_', ' ');
    }
}
