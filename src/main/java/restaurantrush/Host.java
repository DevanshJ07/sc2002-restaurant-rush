package restaurantrush;

import java.util.List;

/**
 * Host: when asked, seats the head of the queue at the lowest-ID free table, once per turn.
 * Independent of the Waiter: it never uses the Waiter's action.
 */
public class Host extends Staff {
    public Host(String name) {
        super(name);
    }

    /** Returns the seated customer, or null if already acted / queue empty / no free table. */
    public Customer seatNextCustomer(WaitingQueue queue, List<Table> tables) throws InvalidActionException {
        if (queue == null || tables == null) {
            throw new IllegalArgumentException("queue and tables must not be null");
        }
        if (hasActedThisTurn() || queue.isEmpty()) {
            return null;
        }
        Table target = null;
        for (Table table : tables) {
            if (!table.isOccupied() && (target == null || table.getTableId() < target.getTableId())) {
                target = table;
            }
        }
        if (target == null) {
            return null;
        }
        Customer customer = queue.getNextCustomer();
        if (customer.getState() != CustomerState.QUEUED) {
            throw new InvalidActionException("Inconsistent state: queue head " + customer.getCustomerId() + " is not QUEUED.");
        }
        queue.removeCustomer(customer);
        target.assignCustomer(customer);
        customer.setState(CustomerState.SEATED);
        markActed();
        return customer;
    }
}
