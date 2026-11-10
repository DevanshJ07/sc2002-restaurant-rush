package restaurantrush;

/**
 * The game's running results and the victory rule.
 * GameController decides when something happens; this class only counts it, so the
 * targets and the average calculation live in one place.
 */
public class GameStats {
    private static final int TARGET_PAID_CUSTOMERS = 4;
    private static final int TARGET_REVENUE_CENTS = 4500;
    private static final int TARGET_AVERAGE_SATISFACTION = 60;

    private int paidCustomerCount;
    private int totalRevenueInCents;
    private int paidSatisfactionTotal;
    private int servedCount;
    private int abandonedCount;

    /** Records a payment: the bill counts as revenue and the satisfaction at payment time counts toward the average. */
    public void recordPayment(int billInCents, int satisfaction) {
        if (billInCents < 0 || satisfaction < 0) {
            throw new IllegalArgumentException("Bill and satisfaction must not be negative");
        }
        totalRevenueInCents += billInCents;
        paidSatisfactionTotal += satisfaction;
        paidCustomerCount++;
    }

    public void recordServed() {
        servedCount++;
    }

    public void recordAbandoned() {
        abandonedCount++;
    }

    public int getPaidCustomerCount() {
        return paidCustomerCount;
    }

    public int getRevenueInCents() {
        return totalRevenueInCents;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getAbandonedCount() {
        return abandonedCount;
    }

    /** Average satisfaction of paid customers; 0 when nobody has paid (the CLI shows N/A in that case). */
    public double getAverageSatisfaction() {
        return paidCustomerCount == 0 ? 0 : (double) paidSatisfactionTotal / paidCustomerCount;
    }

    public boolean meetsTargets() {
        return meetsTargets(paidCustomerCount, totalRevenueInCents, paidSatisfactionTotal);
    }

    /**
     * All three targets must be met. The average is checked as total >= 60 * paid,
     * so it is exact and never affected by rounding.
     */
    static boolean meetsTargets(int paid, int cents, int satisfactionTotal) {
        return paid >= TARGET_PAID_CUSTOMERS
                && cents >= TARGET_REVENUE_CENTS
                && satisfactionTotal >= (long) TARGET_AVERAGE_SATISFACTION * paid;
    }
}
