package restaurantrush;

/**
 * Owns the Happy Hour rules (once per game, lasts 2 turns, 20% off new orders, +15 satisfaction recovery).
 * It holds only the rule state. The controller decides when to recover customers and when to call
 * applyDiscount; no customer loops live here.
 */
public class PolicyManager {
    private static final int DISCOUNT_PERCENT = 20;
    private static final int RECOVERY = 15;
    private static final int DURATION = 2;

    private boolean happyHourHasBeenUsed;
    private int happyHourRemainingTurns;

    public PolicyManager() {
        this.happyHourHasBeenUsed = false;
        this.happyHourRemainingTurns = 0;
    }

    public void activateHappyHour() throws InvalidActionException {
        if (happyHourHasBeenUsed) {
            throw new InvalidActionException("Happy Hour has already been used.");
        }
        happyHourHasBeenUsed = true;
        happyHourRemainingTurns = DURATION;
    }

    /** Called once at the end of every turn; never goes below 0. */
    public void decrementHappyHourTimer() {
        if (happyHourRemainingTurns > 0) {
            happyHourRemainingTurns--;
        }
    }

    public boolean isHappyHourActive() {
        return happyHourRemainingTurns > 0;
    }

    public int getRemainingTurns() {
        return happyHourRemainingTurns;
    }

    /** 20% off a brand-new order while Happy Hour is active (after any VIP discount); otherwise does nothing. */
    public void applyDiscount(Order order) throws InvalidActionException {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (isHappyHourActive()) {
            order.applyDiscount(DISCOUNT_PERCENT);
        }
    }

    public int getSatisfactionRecovery() {
        return RECOVERY;
    }
}
