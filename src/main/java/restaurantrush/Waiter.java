package restaurantrush;

/**
 * Waiter: seats a queued customer, takes an order (the customer chooses the food) or serves a READY dish.
 * Every method validates first and mutates afterwards, and calls markActed() only on success.
 */
public class Waiter extends Staff {
    public Waiter(String name) {
        super(name);
    }

    public void seatCustomer(WaitingQueue queue, Customer customer, Table table) throws InvalidActionException {
        if (queue == null || customer == null || table == null) {
            throw new IllegalArgumentException("queue, customer and table must not be null");
        }
        requireNotActed();
        if (customer.getState() != CustomerState.QUEUED || !queue.getCustomers().contains(customer)) {
            throw new InvalidActionException("Customer " + customer.getCustomerId() + " is not waiting in the queue.");
        }
        if (table.isOccupied()) {
            throw new InvalidActionException("Table " + table.getTableId() + " is already occupied.");
        }
        // all checks passed: now change state
        queue.removeCustomer(customer);
        table.assignCustomer(customer);
        customer.setState(CustomerState.SEATED);
        markActed();
    }

    public Order takeOrder(Customer customer, Menu menu) throws InvalidActionException {
        if (customer == null || menu == null) {
            throw new IllegalArgumentException("customer and menu must not be null");
        }
        requireNotActed();
        if (customer.getState() != CustomerState.SEATED) {
            throw new InvalidActionException(
                    "Customer " + customer.getCustomerId() + " is not seated, so cannot order.");
        }
        if (customer.getOrder() != null) {
            throw new InvalidActionException("Customer " + customer.getCustomerId() + " has already ordered.");
        }
        MenuItem item = customer.chooseItem(menu);               // the customer chooses, not the player
        int bill = customer.calculateBill(item.getPriceInCents()); // pricing rule comes from the customer subtype
        Order order = new Order(item, bill);
        customer.setOrder(order);
        markActed();
        return order;
    }

    public void serveOrder(Customer customer, int turn) throws InvalidActionException {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        requireNotActed();
        if (customer.getState() != CustomerState.SEATED) {
            throw new InvalidActionException("Customer " + customer.getCustomerId() + " is not seated.");
        }
        Order order = customer.getOrder();
        if (order == null) {
            throw new InvalidActionException("Customer " + customer.getCustomerId() + " has not ordered yet.");
        }
        if (!order.isReady()) {
            throw new InvalidActionException("The order for customer " + customer.getCustomerId()
                    + " is not ready yet (" + order.getRemainingPrepUnits() + " prep unit(s) left).");
        }
        order.markServed();
        customer.markServed(turn);
        markActed();
    }
}
