package cs420.cs420finalproject;

import java.util.Objects;

public class Item {

    private String name;
    private String type;
    private double x;
    private double y;

    public Item(String name, String type, double x, double y) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if the objects are the same reference
        if (o == null || getClass() != o.getClass()) return false; // Check if the object is of the same type

        Item item = (Item) o;

        return Double.compare(item.x, x) == 0 && // Compare x coordinates
                Double.compare(item.y, y) == 0 && // Compare y coordinates
                name.equals(item.name) && // Compare names
                type.equals(item.type); // Compare types
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, x, y); // Generate hash code based on important fields
    }

    @Override
    public String toString() {
        return getName() + " (" + getType() + ")";
    }

    public void saveToDatabase() {
        DatabaseConnection.insertItem(this);
    }

}
