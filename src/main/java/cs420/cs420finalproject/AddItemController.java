package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> itemTypeComboBox;  // Use ComboBox instead of TextField for item type
    @FXML private TextField customItemTypeField;       // TextField for custom item type when "Other" is selected
    @FXML private TextField xField;
    @FXML private TextField yField;
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
        String itemType = itemTypeComboBox.getValue();

        if ("other".equals(itemType)) {
            itemType = customItemTypeField.getText();  // Use custom type if "Other" is selected
        }

        double x = 0, y = 0;
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates entered.");
            return;
        }

        boolean isContainer = containerCheckBox.isSelected();
        if (isContainer) {
            newItem = new Container(itemName, itemType, x, y);
        } else {
            newItem = new Item(itemName, itemType, x, y);
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

    private void closePopup() {
        Stage stage = (Stage) itemTypeComboBox.getScene().getWindow();
        stage.close();
    }
}
