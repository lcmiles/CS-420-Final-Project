package cs420.cs420finalproject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
public class AddItemController {
    @FXML private TextField itemNameField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField isContainerField;
    @FXML private TextField itemTypeField; // Replaced ComboBox with a TextField for item type input
    @FXML private TextField parentItemField; // New TextField for the parent item
    private Item newItem;
    private boolean itemCreated = false;
    // Initialize method
    @FXML
    public void initialize() {
        // Set default values or initializations for other fields if needed
    }
    // Handle when user confirms item creation in the popup
    @FXML
    public void onCreateItem() {
        // Get values from input fields
        String itemType = itemTypeField.getText(); // Use itemTypeField instead of itemTypeComboBox
        String itemName = itemNameField.getText();
        double x = 0, y = 0;
        boolean isContainer = Boolean.parseBoolean(isContainerField.getText());
        // If it's not a container, parse the X and Y coordinates
        if (!isContainer) {
            try {
                x = Double.parseDouble(xField.getText());
                y = Double.parseDouble(yField.getText());
            } catch (NumberFormatException e) {
                // Handle invalid coordinate input (optional: show error message to the user)
                System.out.println("Invalid coordinates entered.");
                return;
            }
        }
        // Create the new item
        newItem = new Item(itemName, itemType, x, y, isContainer) {
            @Override
            public void saveToDatabase() {
                // Implement database save logic here (e.g., save newItem to your database)
            }
        };
        // Handle container logic - if the item is a container, set the parent item
        String parentItemName = parentItemField.getText();
        if (!parentItemName.isEmpty()) {
            Item parentItem = findParentItemByName(parentItemName);
            if (parentItem != null) {
                newItem.setParentItem(parentItem); // Set the parent item
            } else {
                System.out.println("Parent item not found.");
            }
        }
        // Mark the item as created
        itemCreated = true;
        // Optionally, you can print the item or perform additional actions
        System.out.println("Item created: " + newItem);
        // Close the popup window
        closePopup();
    }
    public Item getItem() {
        return newItem;
    }
    public boolean isItemCreated() {
        return itemCreated;
    }
    // Find the parent item by name
    private Item findParentItemByName(String parentItemName) {
        // Implement logic to find and return the parent item by name
        // For example, search the database or an existing list of items
        return DatabaseConnection.getItemByName(parentItemName);
    }
    // Close the popup window
    private void closePopup() {
        Stage stage = (Stage) itemTypeField.getScene().getWindow(); // Updated to use itemTypeField's scene
        stage.close();
    }
}