package restaurantrush;

/** Regular customer: loses 8 satisfaction per waiting turn and pays the full price. */
public class RegularCustomer extends Customer {
    public RegularCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public int getSatisfactionLossRate() {
        return 8;
    }

    @Override
    protected int applyPricing(int baseCents) {
        return baseCents;
    }

    @Override
    public String getTypeLabel() {
        return "Regular";
    }
}
