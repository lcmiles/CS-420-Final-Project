package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private TextField customItemTypeField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField lengthField;
    @FXML private TextField widthField;
    @FXML private TextField priceField;  // New field for price
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<Item> itemListView;

    private Item newItem;
    private boolean itemCreated = false;

    @FXML
    public void initialize() {
        itemTypeComboBox.getItems().addAll("field", "pasture", "drone", "drone base", "other");
        itemTypeComboBox.getSelectionModel().selectFirst();
        customItemTypeField.setVisible(false);

        itemTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            customItemTypeField.setVisible("other".equals(newValue));
        });

        loadItemsIntoListView();
    }

    private void loadItemsIntoListView() {
        itemListView.getItems().clear();

        List<Item> allItems = DatabaseConnection.getItems();
        List<Item> containedItems = DatabaseConnection.getContainedItems();
        List<Item> availableItems = new ArrayList<>();

        for (Item item : allItems) {
            boolean alreadyContained = containedItems.stream()
                    .anyMatch(containedItem -> item.hashCode() == containedItem.hashCode());
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

        if (DatabaseConnection.isItemNameTaken(itemName)) {
            showError("Item name already taken. Please choose another name.");
            return;
        }

        String itemType = itemTypeComboBox.getValue();
        if ("other".equals(itemType)) {
            itemType = customItemTypeField.getText();
        }

        double x, y, length, width, price;
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
            length = Double.parseDouble(lengthField.getText());
            width = Double.parseDouble(widthField.getText());
            price = Double.parseDouble(priceField.getText());  // Parse the price input
        } catch (NumberFormatException e) {
            showError("Invalid input. Coordinates, dimensions, and price must be valid numbers.");
            return;
        }

        if (length <= 0 || width <= 0 || price < 0) {
            showError("Length, width, and price must be positive values.");
            return;
        }

        if (x < 0 || x > 1000 || y < 0 || y > 1000) {
            showError("Coordinates must be within valid ranges (0 <= x, y <= 1000).");
            return;
        }

        boolean isContainer = containerCheckBox.isSelected();
        if (isContainer) {
            newItem = new Container(itemName, itemType, price, x, y, length, width);
        } else {
            newItem = new Item(itemName, itemType, price, x, y, length, width);
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
        itemListView.setDisable(!containerCheckBox.isSelected());
        if (containerCheckBox.isSelected()) {
            loadItemsIntoListView();
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
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) itemTypeComboBox.getScene().getWindow();
        stage.close();
    }
}
