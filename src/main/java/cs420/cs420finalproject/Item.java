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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if the objects are the same reference
        if (o == null || getClass() != o.getClass()) return false; // Check if the object is of the same type

        Item item = (Item) o;

        // Perform case-insensitive comparison for name and type
        return Double.compare(item.x, x) == 0 && // Compare x coordinates
                Double.compare(item.y, y) == 0 && // Compare y coordinates
                name.equalsIgnoreCase(item.name) && // Compare names (case-insensitive)
                type.equalsIgnoreCase(item.type); // Compare types (case-insensitive)
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
