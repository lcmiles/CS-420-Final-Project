package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.SelectionMode;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField itemTypeField;
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<Item> itemListView; // Use ListView instead of ComboBox
    private Item newItem;
    private boolean itemCreated = false;

    @FXML
    public void initialize() {
        // Populate the ListView with items from the database
        loadItemsIntoListView();
    }

    private void loadItemsIntoListView() {
        // Retrieve items from the database
        itemListView.getItems().clear();  // Clear any existing items in the ListView
        itemListView.getItems().addAll(DatabaseConnection.getItems());  // Add all items from the database to the ListView
        itemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

        // Create a new item (regular Item or Container)
        if ("container".equals(itemType)) {
            newItem = new Container(itemName, itemType, x, y);  // Create a Container object if the type is "container"
        } else {
            newItem = new Item(itemName, itemType, x, y) {
                @Override
                public void saveToDatabase() {
                    // Insert new item into database
                    DatabaseConnection.insertItem(this);
                }
            };
        }

        // Insert the new item into the database (will be checked for duplicates)
        DatabaseConnection.insertItem(newItem);

        // If the item is a container, insert the contained items into the database
        if (containerCheckBox.isSelected()) {
            if (newItem instanceof Container) {
                Container container = (Container) newItem;
                for (Item selectedItem : itemListView.getSelectionModel().getSelectedItems()) {
                    // Insert into contained_items table
                    DatabaseConnection.insertContainedItem(container, selectedItem);
                }
            } else {
                System.out.println("The created item is not a container.");
            }
        }

        itemCreated = true;
        System.out.println("Item created: " + newItem);
        closePopup();
    }

    @FXML
    private void onContainerCheckBoxChanged() {
        // Enable or disable the ListView based on the CheckBox state
        if (containerCheckBox.isSelected()) {
            itemListView.setDisable(false); // Enable ListView
            loadItemsIntoListView(); // Refresh the ListView with current items from the DB
        } else {
            itemListView.setDisable(true); // Disable ListView
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
