package cs420.cs420finalproject;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.scene.chart.LineChart;
import javafx.util.Duration;

import java.io.PrintStream;

public class ApplicationController {

    @FXML private Label statusLabel;
    @FXML private TextArea logs;
    @FXML private TextArea itemDetails;
    @FXML private Pane dronePane;
    private Circle animatedDrone;
    private Rectangle droneBase;
    private List<Rectangle> fieldItems = new ArrayList<>(); // List of fields for the drone to visit
    private List<Rectangle> pastureItems = new ArrayList<>(); // List of pastures for the drone to visit
    private Map<String, CropGrowthData> cropDataMap = new HashMap<>(); // Mapping of crop data
    private Map<String, SoilMoistureData> soilDataMap = new HashMap<>(); // Mapping of soil moisture data
    private Map<String, LivestockFeedingData> livestockDataMap = new HashMap<>(); // Mapping of livestock feeding data
    private Map<String, PestData> pestDataMap = new HashMap<>(); // Mapping of pest data
    @FXML private LineChart<String, Number> growthLineChart; // Reference to the growth line chart
    @FXML private TreeView<String> itemTreeView; // TreeView to display all items
    Set<String> addedItems = new HashSet<>(); // Set to track added items
    private Circle existingDrone;
    private Rectangle existingDroneBase;
    @FXML private TextField itemNameField;
    @FXML private TextField itemTypeField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private CheckBox containerCheckBox;
    @FXML private ListView<String> itemListView;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    public void initialize() {
        statusLabel.setText("System ready.");
        List<CropGrowthData> savedCropData = DatabaseConnection.getCropGrowthData();
        for (CropGrowthData data : savedCropData) {
            cropDataMap.put(data.getFieldId(), data);
        }
        List<SoilMoistureData> savedSoilData = DatabaseConnection.getSoilMoistureData();
        for (SoilMoistureData data : savedSoilData) {
            soilDataMap.put(data.getFieldId(), data);
        }
        List<LivestockFeedingData> savedLivestockData = DatabaseConnection.getLivestockFeedingData();
        for (LivestockFeedingData data : savedLivestockData) {
            livestockDataMap.put(data.getPastureId(), data);
        }
        List<PestData> savedPestData = DatabaseConnection.getPestData();
        for (PestData data : savedPestData) {
            pestDataMap.put(data.getFieldId(), data);
        }
        loadItemsIntoTree();
        itemTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleItemSelection(newValue);
            }
        });
        System.setOut(new PrintStream(new TextAreaOutputStream(System.out, logs), true));
    }

    private void handleItemSelection(TreeItem<String> selectedItem) {
        if (selectedItem != null) {
            // Enable Edit/Delete buttons
            editButton.setDisable(false);
            deleteButton.setDisable(false);

            // Populate item details into TextArea
            String itemName = selectedItem.getValue();
            Item item = DatabaseConnection.getItemByName(itemName); // A helper method to locate the item by name
            if (item != null) {
                StringBuilder details = new StringBuilder();
                details.append("Name: ").append(item.getName()).append("\n")
                        .append("Type: ").append(item.getType()).append("\n")
                        .append("Price: $").append(String.format("%.2f", item.getPrice())).append("\n")
                        .append("Position: (").append(item.getX()).append(", ").append(item.getY()).append(")\n")
                        .append("Dimensions: ").append(item.getLength()).append(" x ").append(item.getWidth()).append("\n");

                if (item instanceof Container) {
                    Container container = (Container) item;
                    details.append("Contained Items:\n");
                    for (Item containedItem : container.getContainedItems()) {
                        details.append("- ").append(containedItem.getName()).append("\n");
                    }
                }

                itemDetails.setText(details.toString());
            } else {
                itemDetails.setText("Item details not found.");
            }
        } else {
            // Disable buttons when no item is selected
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            itemDetails.clear(); // Clear the TextArea
        }
    }

    private TreeItem<String> loadItemsIntoTree() {
        List<Item> savedItems = DatabaseConnection.getItems();
        Map<String, TreeItem<String>> itemTreeMap = new HashMap<>();
        Map<String, Container> containerMap = new HashMap<>();

        // Create TreeItem nodes for each item and separate containers
        for (Item item : savedItems) {
            TreeItem<String> itemNode = new TreeItem<>(item.getName());
            itemTreeMap.put(item.getName(), itemNode);

            if (item instanceof Container) {
                containerMap.put(item.getName(), (Container) item);
            }
        }

        // Populate children for containers
        for (Container container : containerMap.values()) {
            TreeItem<String> containerNode = itemTreeMap.get(container.getName());
            List<Item> containedItems = container.getContainedItems();

            for (Item containedItem : containedItems) {
                TreeItem<String> containedNode = itemTreeMap.get(containedItem.getName());
                if (containedNode != null) {
                    if (containedNode.getParent() == null) {
                        containerNode.getChildren().add(containedNode);
                    }
                }
            }
        }

        // Build the root of the tree and add orphaned items
        TreeItem<String> root = new TreeItem<>("Items");
        for (TreeItem<String> node : itemTreeMap.values()) {
            if (node.getParent() == null) {
                root.getChildren().add(node);
            }
        }
        root.setExpanded(true);

        // Recursively expand all children
        expandAllNodes(root);

        itemTreeView.setRoot(root);

        // Now load items into the visual pane (dronePane)
        loadItemsIntoVisualPane(containerMap);

        // Return the root of the tree
        return root;
    }

    private void expandAllNodes(TreeItem<String> node) {
        node.setExpanded(true);
        for (TreeItem<String> child : node.getChildren()) {
            expandAllNodes(child);  // Recursively expand child nodes
        }
    }

    @FXML
    private void handleDeleteItem() {
        TreeItem<String> selectedItem = itemTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            System.out.println("Attempting to delete item: " + selectedItem.getValue());

            // Get item data from database
            String itemToDelete = selectedItem.getValue();

            if (itemToDelete != null) {
                System.out.println("Item found for deletion: " + itemToDelete);

                // Delete item relationships in contained_items table
                DatabaseConnection.deleteContainedItemsRelationships(itemToDelete);

                // Delete the item itself from the items table
                DatabaseConnection.deleteItem(itemToDelete);
            }

            // Remove from TreeView
            itemTreeView.getRoot().getChildren().remove(selectedItem);

            // Refresh TreeView
            loadItemsIntoTree();  // Reload tree to reflect changes

            removeExistingVisual(itemToDelete);

            loadItemsIntoVisualPane(new HashMap<>());

            System.out.println("Item deleted successfully.");
        } else {
            System.out.println("No item selected for deletion.");
        }
    }


    @FXML
    private void handleEditItem() {
        // Get the selected item from the TreeView
        TreeItem<String> selectedItem = itemTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {

            // Retrieve item data from the database based on the selected item name
            Item selectedItemData = DatabaseConnection.getItemByName(selectedItem.getValue());

            // Debugging: Print item details
            if (selectedItemData != null) {
                //System.out.println("Item found for editing: " + selectedItemData.getName() + " of type " + selectedItemData.getType());
            } else {
                System.out.println("No item found for editing.");
            }

            // If item data is not found, display a message
            if (selectedItemData == null) {
                System.out.println("Item not found.");
                return;
            }

            // Save the original name before editing
            String originalName = selectedItemData.getName();

            // Open the EditItemView in a modal window with pre-filled data
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("editItemView.fxml"));
                Parent root = fxmlLoader.load();
                EditItemController controller = fxmlLoader.getController();

                // Prefill the fields in the EditItemController with the selected item's data
                controller.prefillFields(selectedItemData);

                // Pass the item being edited to the controller to ensure it doesn't show up in the ListView
                controller.setItemBeingEdited(selectedItemData);

                // Create and show the modal window
                Stage stage = new Stage();
                stage.setTitle("Edit Item");
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // After the modal is closed, check if the item was updated
                if (controller.isItemUpdated()) {

                    loadItemsIntoTree();

                    Item updatedItem = controller.getUpdatedItem();

                    // Update the item in the database, passing the original name
                    DatabaseConnection.updateItem(updatedItem, originalName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error opening edit item view.");
            }
        } else {
            // If no item is selected in the TreeView, show a message
            System.out.println("No item selected to edit."); // Debugging print
        }
    }

    private void loadItemsIntoVisualPane(Map<String, Container> containerMap) {
        // Clear previous visual representations but retain drone, base, and labels
        dronePane.getChildren().removeIf(node -> !(node == animatedDrone || node == droneBase || node instanceof Label));

        // Reset the tracking set
        addedItems.clear();

        // Get the root of the TreeView
        TreeItem<String> root = itemTreeView.getRoot();
        for (TreeItem<String> node : root.getChildren()) {
            Item item = DatabaseConnection.getItemByName(node.getValue());
            if (item != null) {
                double x = item.getX();
                double y = item.getY();
                // Pass along existing visual representations
                removeExistingVisual(item.getName());
                loadItemNodeVisual(node, x, y, containerMap);
            }
        }

        // Re-add drone and base if necessary
        if (animatedDrone != null && !dronePane.getChildren().contains(animatedDrone)) {
            dronePane.getChildren().add(animatedDrone);
        }
        if (droneBase != null && !dronePane.getChildren().contains(droneBase)) {
            dronePane.getChildren().add(droneBase);
        }
    }

    private void loadItemNodeVisual(TreeItem<String> node, double offsetX, double offsetY, Map<String, Container> containerMap) {
        String itemName = node.getValue();
        Item item = DatabaseConnection.getItemByName(itemName);
        String itemType = item.getType();

        if (item == null) {
            return; // If item is null, skip this item
        }

        // Check if the item has already been added to prevent duplicates
        if (addedItems.contains(itemName)) {
            return; // Skip if already added
        }

        // Mark the item as added with its name (unique)
        addedItems.add(itemName);

        // Remove any existing visual elements for the current item to prevent duplicates
        removeExistingVisual(itemName);

        // Size adjustments based on item length and width
        double length = item.getLength();
        double width = item.getWidth();

        // Skip drone and drone base creation, as they are handled separately
        if (itemType.equalsIgnoreCase("drone")) {
            if (existingDrone == null) {
                addDroneToPane(offsetX, offsetY);
            }
            return; // Return early since the drone is already added
        } else if (itemType.equalsIgnoreCase("drone base")) {
            if (existingDroneBase == null) {
                addDroneBase(offsetX, offsetY, itemName);
            }
            return; // Return early since the drone base is already added
        }

        // Check if the current node has children. If it does, it's a container.
        if (node.getChildren().isEmpty()) {
            // Load non-container item (Rectangle representation)
            Rectangle itemRect = createVisualItem(itemType);
            itemRect.setLayoutX(offsetX);
            itemRect.setLayoutY(offsetY);
            itemRect.setWidth(width);
            itemRect.setHeight(length);
            itemRect.setId(itemName); // Use item name as the unique ID

            dronePane.getChildren().add(itemRect);

            // Create label and position it inside the item
            Label itemLabel = new Label(itemName);
            itemLabel.setLayoutX(offsetX); // Position label at the same X as item
            itemLabel.setLayoutY(offsetY + itemRect.getHeight()); // Position label at the same Y as item

            dronePane.getChildren().add(itemLabel);

            // Bring label to the front
            itemLabel.toFront();

            // Apply styling based on item type
            if (itemType.equalsIgnoreCase("field")) {
                itemRect.setStyle("-fx-fill: green;");
                fieldItems.add(itemRect); // Add to fieldItems list
            } else if (itemType.equalsIgnoreCase("pasture")) {
                itemRect.setStyle("-fx-fill: #d8cc49;");
                pastureItems.add(itemRect); // Add to pastureItems list
            }

        } else {
            // Handle container item with size adjustments
            double containerWidth = Math.max(width + 15, 50); // Increased width buffer
            double containerLength = Math.max(length + 15, 50); // Increased height buffer
            Rectangle containerRect = new Rectangle(containerWidth, containerLength);
            containerRect.setId(itemName); // Use item name as the unique ID

            // Apply container styles based on type
            if (itemType.equalsIgnoreCase("field")) {
                containerRect.setStyle("-fx-fill: green; -fx-stroke: black; -fx-stroke-width: 2;");
                fieldItems.add(containerRect); // Add to fieldItems list
            } else if (itemType.equalsIgnoreCase("pasture")) {
                containerRect.setStyle("-fx-fill: #d8cc49; -fx-stroke: black; -fx-stroke-width: 2;");
                pastureItems.add(containerRect); // Add to pastureItems list
            } else {
                containerRect.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
            }

            containerRect.setLayoutX(offsetX);
            containerRect.setLayoutY(offsetY);
            dronePane.getChildren().add(containerRect);

            // Create label and position it inside the container
            Label containerLabel = new Label(itemName);
            containerLabel.setLayoutX(offsetX + 5); // Position label at the same X as container
            containerLabel.setLayoutY(offsetY); // Position label at the same Y as container

            dronePane.getChildren().add(containerLabel);

            // Send the label to the front
            containerLabel.toFront();

            // Track the position of contained items
            double containedOffsetX = offsetX + 20; // Increased horizontal buffer
            double containedOffsetY = offsetY + 20; // Increased vertical buffer
            int itemsInRow = 0;
            double rowHeight = 0;

            // Keep track of occupied space inside the container
            for (TreeItem<String> child : node.getChildren()) {
                Item childItem = DatabaseConnection.getItemByName(child.getValue());
                double childWidth = childItem.getWidth();
                double childLength = childItem.getLength();

                // Ensure no overlap by checking available space
                if (containedOffsetX + childWidth > containerWidth) {
                    // Move to next row if the item doesn't fit
                    containedOffsetX = offsetX + 15; // Reset X position
                    containedOffsetY += rowHeight + 20; // Add space for previous row
                    rowHeight = 0; // Reset row height
                }

                // Position the child item
                loadItemNodeVisual(child, containedOffsetX, containedOffsetY, containerMap);

                // Update the occupied space
                rowHeight = Math.max(rowHeight, childLength); // Track maximum height in the row
                containedOffsetX += childWidth + 15; // Increased horizontal buffer

                itemsInRow++;
                if (itemsInRow % 3 == 0) {
                    containedOffsetX = offsetX + 15; // Reset X position after 3 items
                    containedOffsetY += 90; // Increased space for next row
                }
            }
        }
    }

    private void removeExistingVisual(String itemName) {
        // Remove all visuals (rectangles, labels, circles for the drone) based on item name
        dronePane.getChildren().removeIf(node -> {
            if (node instanceof Rectangle || node instanceof Circle) {
                return node.getId() != null && node.getId().equals(itemName); // Remove by ID (unique to each item)
            } else if (node instanceof Label) {
                Label label = (Label) node;
                return label.getText().contains(itemName); // Match label by name
            }
            return false; // Don't remove other nodes
        });

        // Clear drone-specific visuals if the item name contains "drone" or "drone base"
        if (itemName.contains("drone")) {
            animatedDrone = null; // Clear reference
        } else if (itemName.contains("drone base")) {
            droneBase = null; // Clear reference
        }

        // Remove from lists based on item name
        fieldItems.removeIf(field -> field.getId() != null && field.getId().equals(itemName));
        pastureItems.removeIf(pasture -> pasture.getId() != null && pasture.getId().equals(itemName));
    }

    @FXML public void addItemToPane() {
        openItemDetailsPopup();
    }

    private void openItemDetailsPopup() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addItemView.fxml"));
            Parent root = fxmlLoader.load();
            AddItemController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setTitle("Add New Item");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (controller.isItemCreated()) {
                Item item = controller.getItem();
                DatabaseConnection.insertItem(item);
                loadItemsIntoTree();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50);
        item.setId(itemType); // Assign identifier for easier management
        switch (itemType.toLowerCase()) {
            case "field":
                item.setStyle("-fx-fill: green;");
                break;
            case "pasture":
                item.setStyle("-fx-fill: lightgreen;");
                break;
            default:
                item.setStyle("-fx-fill: gray;");
                break;
        }
        return item;
    }

    private void addDroneToPane(double x, double y) {

        animatedDrone = new Circle(10);
        animatedDrone.setLayoutX(x);
        animatedDrone.setLayoutY(y);
        animatedDrone.setId("drone");
        dronePane.getChildren().add(animatedDrone);

    }

    private void addDroneBase(double x, double y, String name) {

        droneBase = new Rectangle(50, 50);
        droneBase.setLayoutX(x);
        droneBase.setLayoutY(y);
        droneBase.setId("drone base");
        droneBase.setStyle("-fx-fill: #333333; -fx-stroke: black; -fx-stroke-width: 2;");
        dronePane.getChildren().add(droneBase);

    }


    @FXML
    private void onCropDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting crop growth data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        // Loop through field items and create a move transition for each field
        for (Rectangle field : fieldItems) {
            CropGrowthData cropData = findOrCreateCropData(field);
            cropData.increaseGrowthLevel();
            cropData.setTimestamp(timestamp);

            // Create the transition for moving the drone to the field
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                // Once the drone reaches the field, insert crop data into the database
                DatabaseConnection.insertCropGrowthData(cropData);
            });
            transitions.add(moveToField);
        }

        // Create the transition for the drone to return to the base after all fields have been visited
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            // Once the drone returns to the base, update the status label
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        // Combine all transitions and play them sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);

        // Start all transitions
        allTransitions.setOnFinished(event -> {
            // This will ensure that the function only completes after all transitions are finished
        });

        // Play the transition
        allTransitions.play();
    }


    public CropGrowthData findOrCreateCropData(Rectangle field) {
        String fieldId = "Field " + fieldItems.indexOf(field);
        if (cropDataMap.containsKey(fieldId)) {
            return cropDataMap.get(fieldId);
        }
        CropGrowthData newData = new CropGrowthData(
                new SimpleDateFormat("dd/mm/yy HH:mm:ss").format(new Date()),
                fieldId,
                0
        );
        cropDataMap.put(fieldId, newData);
        return newData;
    }

    @FXML public void onViewGrowthChartButtonClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("growthChartView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Crop Growth Chart");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            GrowthChartController chartController = fxmlLoader.getController();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onViewSoilMoistureChartButtonClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("soilMoistureChartView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Soil Moisture Chart");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            SoilMoistureChartController chartController = fxmlLoader.getController();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onViewLivestockFeedingChartButtonClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("livestockFeedingChartView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Livestock Feeding Chart");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            LivestockFeedingChartController chartController = fxmlLoader.getController();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onViewPestDataChartButtonClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("pestDataChartView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Pest Data Chart");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            PestDataChartController chartController = fxmlLoader.getController();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleHarvestCrops() {
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        for (Rectangle field : fieldItems) {
            CropGrowthData cropData = findOrCreateCropData(field);
            if (cropData.getGrowthLevel() == 10) {
                cropData.setGrowthLevel(0);
                cropData.setTimestamp(timestamp);
                DatabaseConnection.insertCropGrowthData(cropData);
                System.out.println("Crops harvested.");
                statusLabel.setText("Crops harvested. System ready.");
            }
        }
    }

    @FXML
    private void onSoilMoistureCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting soil moisture data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        // Loop through field items and create a move transition for each field
        for (Rectangle field : fieldItems) {
            SoilMoistureData soilData = findOrCreateSoilMoistureData(field);
            soilData.decreaseMoistureLevel();  // Decrease moisture between 0-3
            soilData.setTimestamp(timestamp);

            // Create the transition for moving the drone to the field
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                // Once the drone reaches the field, insert soil moisture data into the database
                DatabaseConnection.insertSoilMoistureData(soilData);
            });
            transitions.add(moveToField);
        }

        // Create the transition for the drone to return to the base
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        // Combine all transitions and play them sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
        allTransitions.play();
    }

    private SoilMoistureData findOrCreateSoilMoistureData(Rectangle field) {
        String fieldId = "Field " + fieldItems.indexOf(field);
        if (soilDataMap.containsKey(fieldId)) {
            return soilDataMap.get(fieldId);
        }
        SoilMoistureData newData = new SoilMoistureData(
                new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()),
                fieldId,
                10 // Initial moisture level
        );
        soilDataMap.put(fieldId, newData);
        return newData;
    }

    @FXML
    private void onWaterCrops() {
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        boolean watered = false;

        for (SoilMoistureData data : soilDataMap.values()) {
            if (data.getMoistureLevel() < 10) { // Only water if moisture is not at max
                data.setMoistureLevel(10);  // Reset moisture level to 10
                data.setTimestamp(timestamp); // Update timestamp
                DatabaseConnection.insertSoilMoistureData(data);  // Update database
                watered = true;
            }
        }

        if (watered) {
            System.out.println("Crops watered.");
            statusLabel.setText("Crops watered. System ready.");
        } else {
            System.out.println("Crops are already at maximum moisture.");
            statusLabel.setText("No watering needed. System ready.");
        }
    }

    @FXML
    private void onLivestockFeedingCollect() {
        if (animatedDrone == null || droneBase == null || pastureItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and pastures first.");
            return;
        }
        statusLabel.setText("Collecting livestock feeding data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        // Iterate over pastureItems instead of fieldItems
        for (Rectangle pasture : pastureItems) {
            LivestockFeedingData feedingData = findOrCreateLivestockFeedingData(pasture);
            feedingData.decreaseFeedingLevel();  // Decrease feeding level between 0-3
            feedingData.setTimestamp(timestamp);

            // Create the transition for moving the drone to the pasture
            SequentialTransition moveToPasture = droneAnim.moveDrone(pasture.getLayoutX(), pasture.getLayoutY());
            moveToPasture.setOnFinished(event -> {
                // Once the drone reaches the pasture, insert livestock feeding data into the database
                DatabaseConnection.insertLivestockFeedingData(feedingData);
            });
            transitions.add(moveToPasture);
        }

        // Create the transition for the drone to return to the base
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        // Combine all transitions and play them sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
        allTransitions.play();
    }


    private LivestockFeedingData findOrCreateLivestockFeedingData(Rectangle pasture) {
        String pastureId = "Pasture " + pastureItems.indexOf(pasture);  // Update to pastureItems list
        if (livestockDataMap.containsKey(pastureId)) {
            return livestockDataMap.get(pastureId);
        }
        LivestockFeedingData newData = new LivestockFeedingData(
                new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()),
                pastureId,
                10 // Initial feeding level
        );
        livestockDataMap.put(pastureId, newData);
        return newData;
    }

    @FXML
    private void onFeedLivestock() {
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        boolean fed = false;

        for (LivestockFeedingData data : livestockDataMap.values()) {
            if (data.getFeedingLevel() < 10) { // Only feed if feeding level is not at max
                data.setFeedingLevel(10);  // Reset feeding level to 10
                data.setTimestamp(timestamp); // Update timestamp
                DatabaseConnection.insertLivestockFeedingData(data);  // Update database
                fed = true;
            }
        }

        if (fed) {
            System.out.println("Livestock fed.");
            statusLabel.setText("Livestock fed. System ready.");
        } else {
            System.out.println("Livestock are already fully fed.");
            statusLabel.setText("No feeding needed. System ready.");
        }
    }

    @FXML
    private void onPestDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting pest data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        // Loop through field items and create a move transition for each field
        for (Rectangle field : fieldItems) {
            PestData pestData = findOrCreatePestData(field);
            pestData.increasePestLevel();  // Increase pest level between 0-3
            pestData.setTimestamp(timestamp);

            // Create the transition for moving the drone to the field
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                // Once the drone reaches the field, insert pest data into the database
                DatabaseConnection.insertPestData(pestData);
            });
            transitions.add(moveToField);
        }

        // Create the transition for the drone to return to the base after all fields have been visited
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            // Once the drone returns to the base, update the status label
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        // Combine all transitions and play them sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);

        // Set a listener for when all transitions are finished
        allTransitions.setOnFinished(event -> {
            // This will ensure that the function only completes after all transitions are finished
        });

        // Start playing all transitions
        allTransitions.play();
    }

    private PestData findOrCreatePestData(Rectangle field) {
        String fieldId = "Field " + fieldItems.indexOf(field);
        if (pestDataMap.containsKey(fieldId)) {
            return pestDataMap.get(fieldId);
        }
        PestData newData = new PestData(
                new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()),
                fieldId,
                0 // Initial pest level
        );
        pestDataMap.put(fieldId, newData);
        return newData;
    }

    @FXML
    private void onSprayPesticide() {
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        boolean sprayed = false;

        for (PestData data : pestDataMap.values()) {
            if (data.getPestLevel() > 0) { // Only spray if pest level is greater than 0
                data.setPestLevel(0);  // Reset pest level to 0
                data.setTimestamp(timestamp); // Update timestamp
                DatabaseConnection.insertPestData(data);  // Update database
                sprayed = true;
            }
        }

        if (sprayed) {
            System.out.println("Pesticide sprayed.");
            statusLabel.setText("Pesticide sprayed. System ready.");
        } else {
            System.out.println("No pests to spray.");
            statusLabel.setText("No pests to spray. System ready.");
        }
    }

    @FXML
    private void onManageFlightPlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FlightPlanConfig.fxml"));
            Parent root = loader.load();

            // Create the scene and stage for the flight plan configuration dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Flight Plan Configuration");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void onExecuteFlightPlan() {
        // Get the flight plan order from the database
        List<String> flightPlan = DatabaseConnection.getFlightPlan();

        // Check if the flight plan is empty
        if (flightPlan.isEmpty()) {
            System.out.println("No tasks found in the flight plan.");
            return;
        }

        // Initialize a list of SequentialTransition for chaining tasks
        List<SequentialTransition> taskTransitions = new ArrayList<>();

        // Loop through each task in the flight plan and add the corresponding method to the transitions
        for (String task : flightPlan) {
            SequentialTransition taskTransition = null; // initialize as null

            switch (task) {
                case "Crop Growth":
                    taskTransition = getCropGrowthDataTransition();  // Add the crop growth data collection transition
                    break;
                case "Soil Moisture":
                    taskTransition = getSoilMoistureDataTransition();  // Add the soil moisture data collection transition
                    break;
                case "Livestock Feeding":
                    taskTransition = getLivestockFeedingDataTransition();  // Add the livestock feeding data collection transition
                    break;
                case "Pest Data":
                    taskTransition = getPestDataCollectionTransition();  // Add the pest data collection transition
                    break;
                default:
                    System.out.println("Unknown task: " + task);
                    break;
            }

            // Ensure the taskTransition is not null before adding
            if (taskTransition != null) {
                taskTransitions.add(taskTransition);
            }
        }

        // Combine all task transitions into one SequentialTransition
        SequentialTransition allTasksTransition = new SequentialTransition();
        allTasksTransition.getChildren().addAll(taskTransitions);

        // Set onFinished for the entire flight plan to indicate completion
        allTasksTransition.setOnFinished(event -> {
            System.out.println("Flight plan execution complete.");
            statusLabel.setText("Flight plan executed.");
        });

        // Start the flight plan transitions
        allTasksTransition.play();
    }

// These methods need to return SequentialTransitions for each of the tasks

    private SequentialTransition getCropGrowthDataTransition() {
        SequentialTransition cropGrowthTransition = new SequentialTransition();

        // You can include logic to perform transitions (animation) and database tasks
        cropGrowthTransition.getChildren().add(new PauseTransition(Duration.seconds(2))); // Placeholder for animation/transition
        cropGrowthTransition.setOnFinished(event -> onCropDataCollect()); // Execute crop data collection
        return cropGrowthTransition;
    }

    private SequentialTransition getSoilMoistureDataTransition() {
        SequentialTransition soilMoistureTransition = new SequentialTransition();

        // Placeholder for transition/animation logic
        soilMoistureTransition.getChildren().add(new PauseTransition(Duration.seconds(2)));
        soilMoistureTransition.setOnFinished(event -> onSoilMoistureCollect()); // Execute soil moisture data collection
        return soilMoistureTransition;
    }

    private SequentialTransition getLivestockFeedingDataTransition() {
        SequentialTransition livestockFeedingTransition = new SequentialTransition();

        // Placeholder for transition/animation logic
        livestockFeedingTransition.getChildren().add(new PauseTransition(Duration.seconds(2)));
        livestockFeedingTransition.setOnFinished(event -> onLivestockFeedingCollect()); // Execute livestock feeding data collection
        return livestockFeedingTransition;
    }

    private SequentialTransition getPestDataCollectionTransition() {
        SequentialTransition pestDataTransition = new SequentialTransition();

        // Placeholder for transition/animation logic
        pestDataTransition.getChildren().add(new PauseTransition(Duration.seconds(2)));
        pestDataTransition.setOnFinished(event -> onPestDataCollect()); // Execute pest data collection
        return pestDataTransition;
    }

    @FXML
    private void onScanFarm() {
        if (animatedDrone == null || droneBase == null) {
            statusLabel.setText("Add a drone and base first.");
            return;
        }

        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        statusLabel.setText("Scanning farm...");

        // Create a DroneAnimation instance
        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);

        // Perform the scan across the entire pane
        SequentialTransition scanTransition = droneAnim.scanEntirePane(dronePane);

        // Add the transition to return to base after scanning
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        scanTransition.getChildren().add(returnToBase);

        // Set onFinished for the scan to handle data collection
        scanTransition.setOnFinished(event -> {
            // Perform data collection after the animation
            for (Rectangle field : fieldItems) {
                CropGrowthData cropData = findOrCreateCropData(field);
                SoilMoistureData soilData = findOrCreateSoilMoistureData(field);
                PestData pestData = findOrCreatePestData(field);
                cropData.increaseGrowthLevel();
                soilData.decreaseMoistureLevel();
                pestData.increasePestLevel();
                cropData.setTimestamp(timestamp);
                soilData.setTimestamp(timestamp);
                pestData.setTimestamp(timestamp);
                DatabaseConnection.insertCropGrowthData(cropData);
                DatabaseConnection.insertSoilMoistureData(soilData);
                DatabaseConnection.insertPestData(pestData);
            }

            for (Rectangle pasture : pastureItems) {
                LivestockFeedingData livestockFeedingData = findOrCreateLivestockFeedingData(pasture);
                livestockFeedingData.decreaseFeedingLevel();
                livestockFeedingData.setTimestamp(timestamp);
                DatabaseConnection.insertLivestockFeedingData(livestockFeedingData);
            }

            // Update status label after data collection
            System.out.println("Farm scan and data collection complete.");
            statusLabel.setText("System ready.");
        });

        // Start the animation
        scanTransition.play();
    }

}