package cs420.cs420finalproject;

import java.util.ArrayList;
import java.util.List;

public abstract class Item {

    private String name;
    private String type;
    private double x, y;
    private boolean container;
    private String containedItem;
    private Item parentItem;

    public Item(String name, String type, double x, double y, boolean container) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean getIsContainer() {
        return container;
    }

    public void setIsContainer(boolean container) {
        this.container = container;
    }

    // Change return type to List<Item> for consistency
    public List<Item> getContainedItems() {
        return new ArrayList<>();  // Return an empty list by default for non-container items
    }

    public void setContainedItem(String containedItem) {
        this.containedItem = containedItem;
    }

    public Item getParentItem() {
        return parentItem;
    }

    public void setParentItem(Item parentItem) {
        this.parentItem = parentItem;
    }

    // Abstract method to save to the database
    public abstract void saveToDatabase();
}