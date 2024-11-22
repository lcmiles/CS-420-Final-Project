package cs420.cs420finalproject;

import java.util.Objects;

public class Item {

    private String name;
    private String type;
    private double price;
    private double x;
    private double y;
    private double length;
    private double width;

    public Item(String name, String type, double price, double x, double y, double length, double width) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
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

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
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
