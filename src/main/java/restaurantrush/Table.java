package restaurantrush;

/** A table. Occupancy is derived from currentCustomer, so there is one source of truth. */
public class Table {
    private final int tableId;
    private Customer currentCustomer;

    public Table(int tableId) {
        if (tableId < 1) {
            throw new IllegalArgumentException("Table id must be at least 1");
        }
        this.tableId = tableId;
    }

    public int getTableId() {
        return tableId;
    }

    public boolean isOccupied() {
        return currentCustomer != null;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    /** Validates the table is free and the customer is still waiting in the queue; the seating staff sets SEATED. */
    public void assignCustomer(Customer customer) throws InvalidActionException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        if (isOccupied()) {
            throw new InvalidActionException("Table " + tableId + " is already occupied.");
        }
        if (customer.getState() != CustomerState.QUEUED) {
            throw new InvalidActionException("Customer " + customer.getCustomerId() + " is not waiting to be seated.");
        }
        currentCustomer = customer;
    }

    public void freeTable() {
        currentCustomer = null;
    }
}
