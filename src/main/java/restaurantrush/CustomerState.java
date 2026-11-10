package restaurantrush;

/** Life-cycle states of a customer, from joining the queue to paying or leaving unhappy. */
public enum CustomerState {
    QUEUED, SEATED, EATING, READY_TO_PAY, PAID, ABANDONED
}
