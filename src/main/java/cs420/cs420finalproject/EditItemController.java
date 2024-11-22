package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

public class EditItemController {

    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private TextField priceField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField lengthField;
    @FXML private TextField widthField;
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<Item> itemListView;
    @FXML private TextField customItemTypeField;

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
            showError("Error: No item selected for update.");
            return;
        }

        // Check if the new name already exists in the database
        String newItemName = itemNameField.getText();
        if (newItemName.trim().isEmpty()) {
            showError("Item name cannot be empty.");
            return;  // Stop saving if name is empty
        }

        if (!newItemName.equals(selectedItem.getName()) && DatabaseConnection.isItemNameTaken(newItemName)) {
            showError("Item name already taken. Please choose another name.");
            return;  // Stop saving if name is taken
        }

        // Validate coordinates and dimensions
        double price = 0, x = 0, y = 0, length = 0, width = 0;
        try {
            price = Double.parseDouble(priceField.getText());
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
            length = Double.parseDouble(lengthField.getText());
            width = Double.parseDouble(widthField.getText());
        } catch (NumberFormatException e) {
            showError("Invalid input. Coordinates and dimensions must be valid numbers.");
            return;  // Stop saving if input is invalid
        }

        // Check if length and width are positive
        if (length <= 0 || width <= 0) {
            showError("Length and width must be positive values.");
            return;
        }

        // Check if coordinates are within valid ranges (example ranges: 0 <= x, y <= 1000)
        if (x < 0 || x > 1000 || y < 0 || y > 1000) {
            showError("Coordinates must be within valid ranges (0 <= x, y <= 1000).");
            return;
        }

        // Store the original name before making changes
        String originalName = selectedItem.getName();

        // Update item properties
        selectedItem.setName(newItemName);
        String itemType = itemTypeComboBox.getValue();
        if ("other".equals(itemType)) {
            itemType = customItemTypeField.getText();
        }
        selectedItem.setType(itemType);
        selectedItem.setPrice(price);
        selectedItem.setX(x);
        selectedItem.setY(y);
        selectedItem.setLength(length);
        selectedItem.setWidth(width);

        if (containerCheckBox.isSelected()) {
            if (!(selectedItem instanceof Container)) {
                selectedItem = new Container(
                        selectedItem.getName(),
                        selectedItem.getType(),
                        selectedItem.getPrice(),
                        selectedItem.getX(),
                        selectedItem.getY(),
                        selectedItem.getLength(),
                        selectedItem.getWidth()
                );
            }
            Container container = (Container) selectedItem;
            container.getContainedItems().clear();
            for (Item selectedContainedItem : itemListView.getSelectionModel().getSelectedItems()) {
                DatabaseConnection.insertContainedItem(container, selectedContainedItem);
            }
        }

        DatabaseConnection.updateItem(selectedItem, originalName);
        updatedItem = selectedItem;
        closePopup();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        priceField.setText(String.valueOf(item.getPrice()));
        xField.setText(String.valueOf(item.getX()));
        yField.setText(String.valueOf(item.getY()));
        lengthField.setText(String.valueOf(item.getLength()));  // Prefill length
        widthField.setText(String.valueOf(item.getWidth()));    // Prefill width

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
        List<Item> filteredItems = new ArrayList<>();
        for (Item item : allItems) {
            if (!item.getName().equals(itemBeingEdited.getName())) {
                filteredItems.add(item);
            }
        }

        itemListView.setItems(FXCollections.observableArrayList(filteredItems)); // Add items to ListView
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

    @FXML
    private void onCancel() {
        closePopup(); // Close the edit window
    }

    private void closePopup() {
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        stage.close(); // Close the popup window
    }
}
