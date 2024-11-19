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
    private TextField itemTypeField;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private CheckBox containerCheckBox;
    @FXML
    private ListView<Item> itemListView;

    private Item selectedItem;
    private Item updatedItem;

    @FXML
    private void onSaveChanges() {
        if (selectedItem == null) {
            System.out.println("Error: No item selected for update.");
            return;
        }

        // Update basic properties
        selectedItem.setName(itemNameField.getText());
        selectedItem.setType(itemTypeField.getText());
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
            // Remove container functionality if checkbox is unchecked
            if (selectedItem instanceof Container) {
                Container container = (Container) selectedItem;

                // Remove all contained items from the database
                for (Item containedItem : container.getContainedItems()) {
                    DatabaseConnection.removeContainedItem(container.getName(), containedItem.getName());
                }

                // Replace the container with a regular item
                selectedItem = new Item(
                        container.getName(),
                        container.getType(),
                        container.getX(),
                        container.getY()
                );
            }
        }

        // Update the item in the database
        DatabaseConnection.updateItem(selectedItem);
        updatedItem = selectedItem;
        closePopup();
    }

    public void prefillFields(Item item) {
        selectedItem = item;

        // Prefill basic fields
        itemNameField.setText(item.getName());
        itemTypeField.setText(item.getType());
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

            // Retrieve all available items
            List<Item> allItems = DatabaseConnection.getItems();
            itemListView.getItems().addAll(allItems);

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
            if (!alreadyContained || (selectedItem instanceof Container &&
                    ((Container) selectedItem).getContainedItems().contains(item))) {
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
