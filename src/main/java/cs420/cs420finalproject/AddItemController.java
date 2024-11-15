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
        String itemName = itemNameField.getText();
        double x = 0, y = 0;
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates entered.");
            return;
        }

        // Determine whether the item is a container based on the checkbox
        boolean isContainer = containerCheckBox.isSelected();

        // Create a new item (either regular Item or Container based on the checkbox)
        if (isContainer) {
            newItem = new Container(itemName, "container", x, y);  // Use "container" type for a Container
        } else {
            newItem = new Item(itemName, "regular", x, y) {  // Use "regular" type for a non-container item
                @Override
                public void saveToDatabase() {
                    // Insert new item into database
                    DatabaseConnection.insertItem(this);
                }
            };
        }

        // If the item is a container, insert the contained items into the database
        if (isContainer) {
            Container container = (Container) newItem;
            for (Item selectedItem : itemListView.getSelectionModel().getSelectedItems()) {
                // Insert into contained_items table
                DatabaseConnection.insertContainedItem(container, selectedItem);
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
