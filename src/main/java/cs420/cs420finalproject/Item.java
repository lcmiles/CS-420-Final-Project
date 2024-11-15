package cs420.cs420finalproject;

public abstract class Item {

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

    public abstract void saveToDatabase();

    @Override
    public String toString() {
        return String.format("Item{name='%s', type='%s', x=%.2f, y=%.2f}", name, type, x, y);
    }

}
