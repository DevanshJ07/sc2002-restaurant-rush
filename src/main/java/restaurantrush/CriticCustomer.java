package restaurantrush;

/**
 * Critic: loses 12 satisfaction per waiting turn, pays full price, and always orders the
 * most expensive item on the menu (first one on a price tie).
 */
public class CriticCustomer extends Customer {
    public CriticCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public int getSatisfactionLossRate() {
        return 12;
    }

    @Override
    protected int applyPricing(int baseCents) {
        return baseCents;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu must not be null");
        }
        return menu.getMostExpensiveItem();
    }

    @Override
    public String getTypeLabel() {
        return "Critic";
    }
}
