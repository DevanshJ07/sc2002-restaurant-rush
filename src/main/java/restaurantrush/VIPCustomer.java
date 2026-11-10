package restaurantrush;

/** VIP customer: loses 5 satisfaction per waiting turn and pays 90% of the base price (rounded to nearest cent). */
public class VIPCustomer extends Customer {
    public VIPCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public int getSatisfactionLossRate() {
        return 5;
    }

    @Override
    protected int applyPricing(int baseCents) {
        // multiply first (exact int), divide once as a double, then round half up
        return (int) Math.round(baseCents * 90 / 100.0);
    }

    @Override
    public String getTypeLabel() {
        return "VIP";
    }
}
