package restaurantrush;

/** One item on the menu. Immutable: no setters. */
public class MenuItem {
    private final String name;
    private final int priceInCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceInCents, int preparationUnits) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu item name must not be empty");
        }
        if (priceInCents < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        if (preparationUnits <= 0) {
            throw new IllegalArgumentException("Preparation units must be positive");
        }
        this.name = name;
        this.priceInCents = priceInCents;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    @Override
    public String toString() {
        return name;
    }
}
