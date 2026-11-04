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
    public int calculateBill(int baseCents) {
        if (baseCents < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        return baseCents;
    }

    @Override
    public String getTypeLabel() {
        return "Regular";
    }
}
