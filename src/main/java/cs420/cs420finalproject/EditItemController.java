package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditItemController {

    @FXML
    private TextField itemNameField;
    @FXML private TextField itemTypeField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<Item> itemListView; // Corrected to Item type

    private Item selectedItem;
    private Item updatedItem;
    private boolean itemCreated = false;

    @FXML
    private void onSaveChanges() {
        if (selectedItem == null) {
            System.out.println("Error: No item selected for update.");
            return; // Exit if no item selected
        }

        // Collect updated data and save to the database
        System.out.println("Saving changes for item: " + selectedItem.getName());
        selectedItem.setName(itemNameField.getText());
        selectedItem.setType(itemTypeField.getText());
        selectedItem.setX(Double.parseDouble(xField.getText()));
        selectedItem.setY(Double.parseDouble(yField.getText()));

        if (containerCheckBox.isSelected()) {
            if (!(selectedItem instanceof Container)) {
                selectedItem = new Container(
                        selectedItem.getName(),
                        selectedItem.getType(),
                        selectedItem.getX(),
                        selectedItem.getY()
                );
            }

            // Add selected items to the container
            Container container = (Container) selectedItem;
            for (Item selectedContainedItem : itemListView.getSelectionModel().getSelectedItems()) {
                DatabaseConnection.insertContainedItem(container, selectedContainedItem);
            }
        }

        // Update in database
        DatabaseConnection.updateItem(selectedItem);

        updatedItem = selectedItem;
        itemCreated = true;
        closePopup();
    }

    public void prefillFields(Item item) {
        selectedItem = item;

        itemNameField.setText(item.getName());
        itemTypeField.setText(item.getType());
        xField.setText(String.valueOf(item.getX()));
        yField.setText(String.valueOf(item.getY()));
        containerCheckBox.setSelected(item instanceof Container);
    }

    @FXML
    private void onContainerCheckBoxChanged() {
        if (containerCheckBox.isSelected()) {
            itemListView.setDisable(false);
            loadItemsIntoListView(); // Refresh the ListView with current items from the DB
        } else {
            itemListView.setDisable(true);
        }
    }

    private void loadItemsIntoListView() {
        itemListView.getItems().clear(); // Clear the ListView before populating it

        List<Item> allItems = DatabaseConnection.getItems();
        List<Item> containedItems = DatabaseConnection.getContainedItems();

        List<Item> availableItems = new ArrayList<>();
        for (Item item : allItems) {
            boolean alreadyContained = false;
            for (Item containedItem : containedItems) {
                if (item.hashCode() == containedItem.hashCode()) {
                    alreadyContained = true;
                    break;
                }
            }
            if (!alreadyContained) {
                availableItems.add(item);
            }
        }

        itemListView.getItems().addAll(availableItems);
        itemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public Item getUpdatedItem() {
        return updatedItem;
    }

    public boolean isItemUpdated() {
        return updatedItem != null;
    }

    private void closePopup() {
        Stage stage = (Stage) itemTypeField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancel() {
        closePopup();
    }
}
