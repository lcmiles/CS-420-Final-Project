package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> itemTypeComboBox;  // Use ComboBox instead of TextField for item type
    @FXML private TextField customItemTypeField;       // TextField for custom item type when "Other" is selected
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField lengthField;  // New field for length
    @FXML private TextField widthField;   // New field for width
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<Item> itemListView;
    private Item newItem;
    private boolean itemCreated = false;

    @FXML
    public void initialize() {
        // Initialize ComboBox with predefined options
        itemTypeComboBox.getItems().addAll("field", "pasture", "drone", "drone base", "other");
        itemTypeComboBox.getSelectionModel().selectFirst();  // Select the first option by default

        // Hide the custom item type field initially
        customItemTypeField.setVisible(false);

        // Add listener to show custom item type field when "Other" is selected
        itemTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("other".equals(newValue)) {
                customItemTypeField.setVisible(true);  // Show text field for custom input
            } else {
                customItemTypeField.setVisible(false);  // Hide text field if not "other"
            }
        });

        // Populate the ListView with items from the database
        loadItemsIntoListView();
    }

    private void loadItemsIntoListView() {
        itemListView.getItems().clear();

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

    @FXML
    public void onCreateItem() {
        String itemName = itemNameField.getText();

        // Check if the item name already exists in the database
        if (DatabaseConnection.isItemNameTaken(itemName)) {
            showError("Item name already taken. Please choose another name.");
            return;  // Stop creation if name is taken
        }

        // Continue with item creation if the name is valid
        String itemType = itemTypeComboBox.getValue();

        if ("other".equals(itemType)) {
            itemType = customItemTypeField.getText();  // Use custom type if "Other" is selected
        }

        double x = 0, y = 0, length = 0, width = 0;

        // Validate coordinates and dimensions
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
            length = Double.parseDouble(lengthField.getText());
            width = Double.parseDouble(widthField.getText());
        } catch (NumberFormatException e) {
            showError("Invalid input. Coordinates and dimensions must be valid numbers.");
            return;  // Stop creation if input is invalid
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

        boolean isContainer = containerCheckBox.isSelected();
        if (isContainer) {
            newItem = new Container(itemName, itemType, x, y, length, width);
        } else {
            newItem = new Item(itemName, itemType, x, y, length, width);
        }

        if (isContainer) {
            Container container = (Container) newItem;
            for (Item selectedItem : itemListView.getSelectionModel().getSelectedItems()) {
                DatabaseConnection.insertContainedItem(container, selectedItem);
            }
        }

        itemCreated = true;
        System.out.println("Item created: " + newItem);
        closePopup();
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    public Item getItem() {
        return newItem;
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    @FXML
    private void onCancel() {
        closePopup(); // Close the edit window
    }

    private void closePopup() {
        Stage stage = (Stage) itemTypeComboBox.getScene().getWindow();
        stage.close();
    }
}
