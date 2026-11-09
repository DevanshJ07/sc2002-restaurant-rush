package restaurantrush;

import java.util.List;

/**
 * Cashier: collects payment from the "oldest" eligible customer: READY_TO_PAY with
 * readyToPayTurn &lt; current turn, earliest readyToPayTurn first, ties by lower customerId.
 * Revenue is NOT updated here; the controller aggregates from the returned customer.
 */
public class Cashier extends Staff {
    public Cashier(String name) {
        super(name);
    }

    /** Returns the paid customer, or null if already acted / nobody eligible. */
    public Customer collectPayment(List<Table> tables, int turn) throws InvalidActionException {
        if (tables == null) {
            throw new IllegalArgumentException("tables must not be null");
        }
        if (hasActedThisTurn()) {
            return null;
        }
        Table chosenTable = null;
        for (Table table : tables) {
            Customer c = table.getCurrentCustomer();
            if (c == null || c.getState() != CustomerState.READY_TO_PAY || c.getReadyToPayTurn() >= turn) {
                continue;
            }
            if (chosenTable == null || isOlder(c, chosenTable.getCurrentCustomer())) {
                chosenTable = table;
            }
        }
        if (chosenTable == null) {
            return null;
        }
        Customer customer = chosenTable.getCurrentCustomer();
        Order order = customer.getOrder();
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            // READY_TO_PAY is only reachable after serving, so this would be a bug, not a player mistake.
            throw new IllegalStateException("Customer " + customer.getCustomerId() + " has no served order to pay.");
        }
        order.markPaid();
        customer.setState(CustomerState.PAID);
        chosenTable.freeTable();
        markActed();
        return customer;
    }

    private boolean isOlder(Customer a, Customer b) {
        if (a.getReadyToPayTurn() != b.getReadyToPayTurn()) {
            return a.getReadyToPayTurn() < b.getReadyToPayTurn();
        }
        return a.getCustomerId() < b.getCustomerId();
    }
}
