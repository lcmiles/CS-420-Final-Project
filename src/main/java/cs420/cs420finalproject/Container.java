package cs420.cs420finalproject;

import java.util.ArrayList;
import java.util.List;

public class Container extends Item {
    private List<Item> containedItems;

    public Container(String name, String type, double x, double y) {
        super(name, type, x, y);
        this.containedItems = new ArrayList<>();
    }

    public List<Item> getContainedItems() {
        return containedItems;
    }

    public void addItem(Item item) {
        containedItems.add(item);
    }

    @Override
    public void saveToDatabase() {
        DatabaseConnection.insertItem(this);
        for (Item item : containedItems) {
            DatabaseConnection.insertContainedItem(this, item);
        }
    }

    @Override
    public String toString() {
        return String.format("Container{name='%s', type='%s', x=%.2f, y=%.2f, containedItems=%s}",
                getName(), getType(), getX(), getY(), containedItems);
    }
}
