package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
        }

        // Update in database
        DatabaseConnection.updateItem(selectedItem);

        updatedItem = selectedItem;  // Update the updatedItem variable
        itemCreated = true;  // Mark the item as created/modified
        closePopup();
    }

    public void prefillFields(Item item) {
        selectedItem = item;  // Ensure selectedItem is set here

        itemNameField.setText(item.getName());
        itemTypeField.setText(item.getType());
        xField.setText(String.valueOf(item.getX()));
        yField.setText(String.valueOf(item.getY()));
        containerCheckBox.setSelected(item instanceof Container);

    }


    @FXML
    private void onContainerCheckBoxChanged() {
        System.out.println("onContainerCheckBoxChanged called");

        // Handle the checkbox change event
        if (containerCheckBox.isSelected()) {
            // Enable the ListView or make any other necessary changes
            itemListView.setDisable(false);
        } else {
            // Disable the ListView or revert any changes
            itemListView.setDisable(true);
        }
    }

    public Item getUpdatedItem() {
        return updatedItem;
    }

    public boolean isItemUpdated() {
        return updatedItem != null;  // Checks if an updated item exists
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
