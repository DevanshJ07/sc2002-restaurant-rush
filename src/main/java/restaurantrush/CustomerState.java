package restaurantrush;

/** Life-cycle states of a customer. ABANDONED is the single spelling used everywhere (v3 fix F13). */
public enum CustomerState {
    QUEUED, SEATED, EATING, READY_TO_PAY, PAID, ABANDONED
}
