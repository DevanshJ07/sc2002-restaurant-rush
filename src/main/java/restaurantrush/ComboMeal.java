package restaurantrush;

/**
 * A combo of two menu items: price = 90% of the summed prices (rounded to nearest cent),
 * preparation units = sum of both. Both are computed once in the constructor. Because a
 * ComboMeal IS a MenuItem, menu, ordering, cooking and billing need no changes.
 * Salad (900) + Pasta (1800) = 2430 cents, 3 units.
 */
public class ComboMeal extends MenuItem {
    private final MenuItem componentA;
    private final MenuItem componentB;

    public ComboMeal(String name, MenuItem a, MenuItem b) {
        super(name, comboPrice(a, b), comboUnits(a, b));
        this.componentA = a;
        this.componentB = b;
    }

    private static int comboPrice(MenuItem a, MenuItem b) {
        requireBoth(a, b);
        return (int) Math.round((a.getPriceInCents() + b.getPriceInCents()) * 90 / 100.0);
    }

    private static int comboUnits(MenuItem a, MenuItem b) {
        requireBoth(a, b);
        return a.getPreparationUnits() + b.getPreparationUnits();
    }

    private static void requireBoth(MenuItem a, MenuItem b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("A combo needs two menu items");
        }
    }

    public MenuItem getComponentA() {
        return componentA;
    }

    public MenuItem getComponentB() {
        return componentB;
    }
}
