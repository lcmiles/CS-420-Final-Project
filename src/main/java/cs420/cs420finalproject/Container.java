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

    @Override
    public void saveToDatabase() {
        // Insert container into the database
        DatabaseConnection.insertItem(this);
    }
}

