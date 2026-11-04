package restaurantrush;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The restaurant menu. Owns its items (composition). Order of insertion matters for food choice. */
public class Menu {
    private final List<MenuItem> items = new ArrayList<>();

    public Menu() {
    }

    public void addItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must not be null");
        }
        if (findItemByName(item.getName()) != null) {
            throw new IllegalArgumentException("Menu already contains an item called " + item.getName());
        }
        items.add(item);
    }

    /** Unmodifiable view, in menu order. */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Case-insensitive lookup; null if there is no such item. */
    public MenuItem findItemByName(String name) {
        if (name == null) {
            return null;
        }
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name.trim())) {
                return item;
            }
        }
        return null;
    }
}
