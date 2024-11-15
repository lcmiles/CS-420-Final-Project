package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField itemTypeField; // Replaced ComboBox with a TextField for item type input
    @FXML private CheckBox containerCheckBox;
    @FXML private ComboBox<Item> itemComboBox;
    private Item newItem;
    private boolean itemCreated = false;

    @FXML
    public void initialize() {
        // Populate the ComboBox with items from the database
        loadItemsIntoComboBox();
    }

    private void loadItemsIntoComboBox() {
        // Retrieve items from the database
        itemComboBox.getItems().clear();  // Clear any existing items in the ComboBox
        itemComboBox.getItems().addAll(DatabaseConnection.getItems());  // Add all items from the database to the ComboBox
    }

    @FXML
    public void onCreateItem() {
        String itemType = itemTypeField.getText();
        String itemName = itemNameField.getText();
        double x = 0, y = 0;
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates entered.");
            return;
        }
        newItem = new Item(itemName, itemType, x, y) {
            @Override
            public void saveToDatabase() {
                // Implement database save logic here (e.g., save newItem to your database)
                DatabaseConnection.insertItem(this);
            }
        };
        itemCreated = true;
        System.out.println("Item created: " + newItem);
        closePopup();
    }

    @FXML
    private void onContainerCheckBoxChanged() {
        // Enable or disable the ComboBox based on the CheckBox state
        if (containerCheckBox.isSelected()) {
            itemComboBox.setDisable(false); // Enable ComboBox
            loadItemsIntoComboBox(); // Refresh the ComboBox with current items from the DB
        } else {
            itemComboBox.setDisable(true); // Disable ComboBox
        }
    }

    public Item getItem() {
        return newItem;
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    private void closePopup() {
        Stage stage = (Stage) itemTypeField.getScene().getWindow();
        stage.close();
    }
}
