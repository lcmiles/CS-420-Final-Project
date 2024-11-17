package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

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
        itemListView.getItems().clear();  // Clear any existing items in the ListView

        // Retrieve all items from the database
        List<Item> allItems = DatabaseConnection.getItems();

        // Retrieve all containers and the items contained in them
        List<Item> containedItems = DatabaseConnection.getContainedItems();

        // Filter out items that are already added to a container
        List<Item> availableItems = new ArrayList<>();
        for (Item item : allItems) {
            boolean alreadyContained = false;
            for (Item containedItem : containedItems) {
                if (item.hashCode() == containedItem.hashCode()) {
                    alreadyContained = true;
                    break;  // This item is already contained in a container, skip it
                }
            }
            if (!alreadyContained) {
                availableItems.add(item);  // Add item to the list if it's not already contained
            }
        }

        itemListView.getItems().addAll(availableItems);  // Add available items to the ListView
        itemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // Allow multi-selection
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

        boolean isContainer = containerCheckBox.isSelected();
        System.out.println("Creating item. Is container: " + isContainer);

        // Create item or container based on checkbox
        if (isContainer) {
            String itemType = itemTypeField.getText();
            newItem = new Container(itemName, itemType, x, y);
            System.out.println("Container created: " + newItem);
        } else {
            String itemType = itemTypeField.getText();
            newItem = new Item(itemName, itemType, x, y);
            System.out.println("Regular item created: " + newItem);
        }

        // Add contained items to container if it's a container
        if (isContainer) {
            Container container = (Container) newItem;
            for (Item selectedItem : itemListView.getSelectionModel().getSelectedItems()) {
                System.out.println("Adding contained item: " + selectedItem);
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
