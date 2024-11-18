package cs420.cs420finalproject;

import java.util.ArrayList;
import java.util.List;

public class Container extends Item {
    private List<Item> containedItems;

    public Container(String name, String type, double x, double y) {
        super(name, type, x, y);
        this.containedItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        if (containedItems == null) {
            containedItems = DatabaseConnection.getContainedItemsForContainer(this); // Lazy load items
        }
        containedItems.add(item);
    }

    public List<Item> getContainedItems() {
        if (containedItems == null) {
            // Lazy load contained items from the database
            containedItems = DatabaseConnection.getContainedItemsForContainer(this);
        }
        return containedItems;
    }

    @Override
    public void saveToDatabase() {
        // Save the container itself
        DatabaseConnection.insertItem(this);

        // Save the relationships between the container and its contained items
        for (Item containedItem : getContainedItems()) {
            if (containedItem instanceof Container) {
                ((Container) containedItem).saveToDatabase(); // Recursively save sub-containers
            } else {
                containedItem.saveToDatabase();
            }
            DatabaseConnection.insertContainedItem(this, containedItem); // Insert the relationship
        }
    }
}
