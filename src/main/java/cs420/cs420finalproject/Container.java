package cs420.cs420finalproject;
import java.util.List;
public class Container extends Item {
    private List<Item> containedItems;
    public Container(String name, String type, double x, double y, boolean isContainer, List<Item> containedItems) {
        super(name, type, x, y, isContainer);
        this.containedItems = containedItems;
    }
    public List<Item> getContainedItems() {
        return containedItems;
    }
    public void setContainedItems(List<Item> containedItems) {
        this.containedItems = containedItems;
    }
    // Override saveToDatabase to handle the recursive saving of contained items
    @Override
    public void saveToDatabase() {
        // First, save the container itself
        DatabaseConnection.insertItem(this);
        // Then, if this container has contained items, save them recursively
        for (Item containedItem : containedItems) {
            containedItem.saveToDatabase();  // This will recursively call saveToDatabase on contained items
        }
    }
}