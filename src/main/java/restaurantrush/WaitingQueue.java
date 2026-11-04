package restaurantrush;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** FIFO line of customers waiting for a table (renamed from Queue to avoid java.util.Queue). */
public class WaitingQueue {
    private final LinkedList<Customer> waitingCustomers = new LinkedList<>();

    public WaitingQueue() {
    }

    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        if (customer.getState() != CustomerState.QUEUED) {
            throw new IllegalStateException("Only a QUEUED customer can join the queue (state " + customer.getState() + ")");
        }
        for (Customer c : waitingCustomers) {
            if (c == customer || c.getCustomerId() == customer.getCustomerId()) {
                throw new IllegalStateException("Customer " + customer.getCustomerId() + " is already in the queue");
            }
        }
        waitingCustomers.addLast(customer);
    }

    /** True if the customer was in the queue. */
    public boolean removeCustomer(Customer customer) {
        return waitingCustomers.remove(customer);
    }

    /** Peek at the head without removing it; null if empty. */
    public Customer getNextCustomer() {
        return waitingCustomers.peekFirst();
    }

    public int getQueueSize() {
        return waitingCustomers.size();
    }

    public boolean isEmpty() {
        return waitingCustomers.isEmpty();
    }

    /** Unmodifiable view. Copy it before removing customers while iterating. */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(waitingCustomers);
    }
}
