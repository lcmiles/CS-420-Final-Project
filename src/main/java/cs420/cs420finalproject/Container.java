package cs420.cs420finalproject;

import java.util.ArrayList;
import java.util.List;

public class Container extends Item {
    private List<Item> containedItems;

    public Container(String name, String type, double x, double y) {
        super(name, type, x, y);
        containedItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        containedItems.add(item);
    }

    public List<Item> getContainedItems() {
        return containedItems;
    }

    // New method to check if the container already contains an item
    public boolean containsItem(Item item) {
        return containedItems.contains(item);
    }

    @Override
    public void saveToDatabase() {
        // Insert container into the database
        DatabaseConnection.insertItem(this);

        // Insert contained items to the database as well
        for (Item containedItem : containedItems) {
            DatabaseConnection.insertItem(containedItem);
            // You may also need to insert the relationship in a junction table, e.g., "contained_items"
            // Example: DatabaseConnection.insertContainedItem(this, containedItem);
        }
    }
}
