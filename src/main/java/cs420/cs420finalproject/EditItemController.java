package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditItemController {

    @FXML
    private TextField itemNameField;
    @FXML
    private ComboBox<String> itemTypeComboBox;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private CheckBox containerCheckBox;
    @FXML
    private ListView<Item> itemListView;
    @FXML
    private TextField customItemTypeField;

    private Item selectedItem;
    private Item itemBeingEdited;
    private Item updatedItem;

    @FXML
    public void initialize() {
        // Initialize ComboBox with predefined options
        itemTypeComboBox.getItems().addAll("field", "pasture", "drone", "drone base", "other");

        // Add listener to show custom item type field when "Other" is selected
        itemTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("other".equals(newValue)) {
                customItemTypeField.setVisible(true);  // Show text field for custom input
            } else {
                customItemTypeField.setVisible(false);  // Hide text field if not "other"
            }
        });

        // Prefill fields for editing
        prefillFields(selectedItem);
    }

    @FXML
    private void onSaveChanges() {
        if (selectedItem == null) {
            System.out.println("Error: No item selected for update.");
            return;
        }

        // Store the original name before making changes
        String originalName = selectedItem.getName();

        // Update basic properties
        selectedItem.setName(itemNameField.getText());

        // Check if "Other" is selected and use the custom item type if needed
        String itemType = itemTypeComboBox.getValue();
        if ("other".equals(itemType)) {
            itemType = customItemTypeField.getText();  // Use custom item type
        }
        selectedItem.setType(itemType);  // Set the selected or custom item type

        selectedItem.setX(Double.parseDouble(xField.getText()));
        selectedItem.setY(Double.parseDouble(yField.getText()));

        if (containerCheckBox.isSelected()) {
            // Convert to a container if it's not already one
            if (!(selectedItem instanceof Container)) {
                selectedItem = new Container(
                        selectedItem.getName(),
                        selectedItem.getType(),
                        selectedItem.getX(),
                        selectedItem.getY()
                );
            }

            // Add contained items to the container
            Container container = (Container) selectedItem;
            container.getContainedItems().clear(); // Clear current contained items
            for (Item selectedContainedItem : itemListView.getSelectionModel().getSelectedItems()) {
                DatabaseConnection.insertContainedItem(container, selectedContainedItem);
            }
        } else {
            // Check if the item is in the contained_items table before attempting to remove it
            List<Item> containedItems = DatabaseConnection.getContainedItemsForContainer(selectedItem);
            for (Item containedItem : containedItems) {
                if (containedItem.getName().equals(selectedItem.getName())) {
                    // Remove the item from contained_items if it's found
                    DatabaseConnection.removeContainedItem(selectedItem.getName(), containedItem.getName());
                    break;
                }
            }

            // Replace the container with a regular item
            selectedItem = new Item(
                    selectedItem.getName(),
                    selectedItem.getType(),
                    selectedItem.getX(),
                    selectedItem.getY()
            );

            // Ensure that the item no longer has any contained items
            DatabaseConnection.deleteContainedItemsRelationships(originalName);
        }

        // Update the item in the database, passing the original name
        DatabaseConnection.updateItem(selectedItem, originalName);

        // Update the locally stored reference
        updatedItem = selectedItem;

        // Close the popup window
        closePopup();
    }

    public void prefillFields(Item item) {
        if (item == null) {
            System.out.println("Error: The item to be edited is null.");
            return;
        }

        selectedItem = item;
        itemBeingEdited = item;

        // Prefill basic fields
        itemNameField.setText(item.getName());
        itemTypeComboBox.setValue(item.getType());  // Set the item type in the ComboBox
        xField.setText(String.valueOf(item.getX()));
        yField.setText(String.valueOf(item.getY()));

        // Check if the item is a container
        boolean isContainer = DatabaseConnection.isContainer(item.getName());
        containerCheckBox.setSelected(isContainer);

        if (isContainer) {
            itemListView.setDisable(false); // Enable the ListView
            itemListView.getItems().clear(); // Clear any previous items

            // Set a custom cell factory for displaying items in the ListView
            itemListView.setCellFactory(param -> new ListCell<Item>() {
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName()); // Display item name or any other property
                    }
                }
            });

            // Now, call loadItemsIntoListView to populate the ListView
            loadItemsIntoListView();

            // Retrieve the container instance from the database
            Container container = DatabaseConnection.getContainerByName(item.getName());
            if (container != null) {
                List<Item> containedItems = DatabaseConnection.getContainedItemsForContainer(container);

                // Preselect items already contained in the container
                for (Item containedItem : containedItems) {
                    // Ensure selection works based on object equality
                    itemListView.getSelectionModel().select(containedItem);
                    System.out.println("Selected contained item: " + containedItem);
                }
            } else {
                System.err.println("Error: Item is marked as a container, but no container found in the database.");
            }
        } else {
            itemListView.setDisable(true); // Disable the ListView for non-containers
            itemListView.getItems().clear(); // Clear items for non-containers
        }

        if ("other".equals(item.getType())) {
            customItemTypeField.setVisible(true);
            customItemTypeField.setText(item.getType());
        } else {
            customItemTypeField.setVisible(false);
        }
    }

    @FXML
    private void onContainerCheckBoxChanged() {
        if (containerCheckBox.isSelected()) {
            itemListView.setDisable(false);
            loadItemsIntoListView();
        } else {
            itemListView.setDisable(true);
        }
    }

    public void loadItemsIntoListView() {
        itemListView.getItems().clear(); // Clear current items

        if (itemBeingEdited == null) {
            System.out.println("Error: itemBeingEdited is null.");
            return;  // Exit early if itemBeingEdited is null
        }

        List<Item> allItems = DatabaseConnection.getItems();
        List<Item> containedItems = DatabaseConnection.getContainedItems();

        List<Item> availableItems = new ArrayList<>();

        // Iterate through all items to determine which ones should be available
        for (Item item : allItems) {
            // Skip the item that is currently being edited (itemBeingEdited)
            if (item.equals(itemBeingEdited) || item.getName().equals(itemBeingEdited.getName())) {
                continue;  // Skip items that have the same name or are the same item
            }

            // Check if the item is already contained in the selected item (container)
            boolean isContained = containedItems.stream()
                    .anyMatch(containedItem -> containedItem.equals(item));

            // If the item is not already contained, add it to the available list
            if (!isContained) {
                availableItems.add(item);
            }
        }

        // Update the ListView with available items
        itemListView.getItems().addAll(availableItems);
        itemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setItemBeingEdited(Item item) {
        this.itemBeingEdited = item;
    }

    public Item getUpdatedItem() {
        return updatedItem;
    }

    public boolean isItemUpdated() {
        return updatedItem != null;  // Checks if an updated item exists
    }

    private void closePopup() {
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancel() {
        closePopup();
    }
}