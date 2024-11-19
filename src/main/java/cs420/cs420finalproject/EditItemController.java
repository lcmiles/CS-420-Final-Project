package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class EditItemController {

    @FXML
    private TextField itemNameField;
    @FXML private TextField itemTypeField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<String> itemListView;

    private Item selectedItem;
    private Item updatedItem;  // Added declaration for updatedItem
    private boolean itemCreated = false;  // Added declaration for itemCreated

    @FXML
    private void onSaveChanges() {
        if (selectedItem == null) {
            // Optionally, alert the user that no item is selected.
            System.out.println("No item selected for update");
            return;
        }

        // Collect updated data and save to the database
        selectedItem.setName(itemNameField.getText());
        selectedItem.setType(itemTypeField.getText());
        selectedItem.setX(Double.parseDouble(xField.getText()));
        selectedItem.setY(Double.parseDouble(yField.getText()));

        if (containerCheckBox.isSelected()) {
            // Make sure selectedItem is a container if needed
            if (!(selectedItem instanceof Container)) {
                // Create a new container using the properties of selectedItem
                selectedItem = new Container(
                        selectedItem.getName(),
                        selectedItem.getType(),
                        selectedItem.getX(),
                        selectedItem.getY()
                );
            }
        }

        // Update in database
        DatabaseConnection.updateItem(selectedItem);

        updatedItem = selectedItem;  // Update the updatedItem variable
        itemCreated = true;  // Mark the item as created/modified

        // Close the window or do any other post-save actions
    }

    @FXML
    private void onContainerCheckBoxChanged() {
        // Handle the checkbox change event
        if (containerCheckBox.isSelected()) {
            // Enable the ListView or make any other necessary changes
            itemListView.setDisable(false);
        } else {
            // Disable the ListView or revert any changes
            itemListView.setDisable(true);
        }
    }


    // Method to prefill fields with data
    public void prefillFields(Item item) {
        itemNameField.setText(item.getName());
        itemTypeField.setText(item.getType());
        xField.setText(String.valueOf(item.getX()));
        yField.setText(String.valueOf(item.getY()));
        containerCheckBox.setSelected(item instanceof Container);
        // Optionally, handle ListView or any other components related to containers
    }

    public Item getUpdatedItem() {
        return updatedItem;
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    public boolean isItemUpdated() {
        return updatedItem != null;  // Checks if an updated item exists
    }

    @FXML
    private void onCancel() {
        // Handle cancellation, e.g., close the window
    }
}
