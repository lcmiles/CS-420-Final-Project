package cs420.cs420finalproject;

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
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.scene.chart.LineChart;
import java.io.PrintStream;

public class ApplicationController {

    @FXML private Label statusLabel;
    @FXML private TextArea logs;
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
        } else {
            // Disable buttons when no item is selected
            editButton.setDisable(true);
            deleteButton.setDisable(true);
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

            // Optionally reload the visual representation
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
                    Item updatedItem = controller.getUpdatedItem();

                    // Update the item in the database
                    DatabaseConnection.updateItem(updatedItem);

                    // Refresh the TreeView and visual representation of the items
                    loadItemsIntoTree();
                    loadItemsIntoVisualPane(new HashMap<>());
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
        dronePane.getChildren().removeIf(node ->
                !(node == animatedDrone || node == droneBase || node instanceof Label));

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
                loadItemNodeVisual(node, 0, x, y, containerMap);
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

    private void loadItemNodeVisual(TreeItem<String> node, int depth, double offsetX, double offsetY, Map<String, Container> containerMap) {

        String itemName = node.getValue();
        String itemType = DatabaseConnection.getItemByName(itemName).getType();

        // Check if the item has already been added
        if (addedItems.contains(itemName)) {
            return; // Skip if already added
        }

        // Mark the item as added
        addedItems.add(itemName);

        // Remove any existing visual elements for the current item to prevent duplicates
        removeExistingVisual(itemName);

        // Calculate size based on depth, where top-level containers are larger
        double sizeFactor = 1 + (0.2 * (3 - depth)); // Scale factor that decreases with depth
        double containerSize = 100 * sizeFactor; // Adjust container size for depth

        // Skip drone and drone base creation here, as they are handled separately
        if (itemType.equalsIgnoreCase("drone")) {
            if (existingDrone == null) {
                addDroneToPane(offsetX, offsetY);
            }
            return; // Return early since the drone is already added
        } else if (itemType.equalsIgnoreCase("drone base")) {
            if (existingDroneBase == null) {
                addDroneBase();
            }
            return; // Return early since the drone base is already added
        }

        // Check if the current node has children. If it does, it's a container.
        if (node.getChildren().isEmpty()) {
            // Load non-container item
            Rectangle itemRect = createVisualItem(itemType);
            itemRect.setLayoutX(offsetX);
            itemRect.setLayoutY(offsetY);
            itemRect.setId(itemType); // Assign the itemType as the ID for uniqueness
            dronePane.getChildren().add(itemRect);

            // Set a higher view order for contained items (to appear above containers)
            if (itemType.equalsIgnoreCase("field")) {
                itemRect.setViewOrder(1); // Ensure fields appear above containers
                itemRect.setStyle("-fx-fill: green;");
                fieldItems.add(itemRect); // Add field to the fieldItems list
            } else if (itemType.equalsIgnoreCase("pasture")) {
                itemRect.setViewOrder(1); // Ensure pastures appear above containers
                itemRect.setStyle("-fx-fill: #d8cc49;"); // Light green for pastures
                pastureItems.add(itemRect); // Add pasture to the pastureItems list
            }

            // Add buffer space for next item
            offsetY += 10;
        } else {
            // Load the container as a rectangle with adjusted size based on depth
            Rectangle containerRect = new Rectangle(containerSize, containerSize); // Size adjusted for depth
            containerRect.setId(itemType); // Assign the itemType as the ID for uniqueness

            // If this container is a "field", make it green
            if (itemType.equalsIgnoreCase("field")) {
                containerRect.setStyle("-fx-fill: green; -fx-stroke: black; -fx-stroke-width: 2;");
                fieldItems.add(containerRect); // Add this container to the fieldItems list
            } else if (itemType.equalsIgnoreCase("pasture")) {
                // If this is a pasture, make it light green
                containerRect.setStyle("-fx-fill: #d8cc49; -fx-stroke: black; -fx-stroke-width: 2;");
                pastureItems.add(containerRect); // Add this container to the pastureItems list
            } else {
                containerRect.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
            }

            containerRect.setLayoutX(offsetX);
            containerRect.setLayoutY(offsetY);
            dronePane.getChildren().add(containerRect);

            // Set a lower view order for the container (so contained items appear above it)
            containerRect.setViewOrder(0);

            // Recursively load contained items within the container
            double containedOffsetX = offsetX + 10;
            double containedOffsetY = offsetY + 10;
            for (TreeItem<String> child : node.getChildren()) {
                loadItemNodeVisual(child, depth + 1, containedOffsetX, containedOffsetY, containerMap);
                containedOffsetY += 10; // Buffer space between contained items
            }
        }
    }

    private void removeExistingVisual(String itemName) {
        // Remove from the dronePane if it exists
        dronePane.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                return rect.getId() != null && rect.getId().equals(itemName);
            }
            return false;
        });

        // Remove from the fieldItems list if it exists
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
        if (animatedDrone == null) {
            animatedDrone = new Circle(10);
            animatedDrone.setLayoutX(x);
            animatedDrone.setLayoutY(y);
            dronePane.getChildren().add(animatedDrone);
        }
    }

    private void addDroneBase() {
        if (droneBase == null) {
            droneBase = new Rectangle(50, 50);
            droneBase.setLayoutX(350);
            droneBase.setLayoutY(350);
            droneBase.setStyle("-fx-fill: brown;");
            dronePane.getChildren().add(droneBase);
        }
    }

    @FXML private void onCropDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting crop growth data...");

        // Initialize the drone animation
        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        // Create transitions for moving the drone to each field and then back to the base
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

        // Create the transition for the drone to return to the base
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        // Combine all transitions and play them sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);

        // Debugging: confirm that all transitions are set
        allTransitions.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });

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

    @FXML private void onSoilMoistureCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting soil moisture data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        for (Rectangle field : fieldItems) {
            SoilMoistureData soilData = findOrCreateSoilMoistureData(field);
            soilData.decreaseMoistureLevel();  // Decrease moisture between 0-3
            soilData.setTimestamp(timestamp);

            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                DatabaseConnection.insertSoilMoistureData(soilData);
            });
            transitions.add(moveToField);
        }

        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

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

    @FXML private void onWaterCrops() {
        for (SoilMoistureData data : soilDataMap.values()) {
            data.setMoistureLevel(10);  // Reset moisture level to 10
            DatabaseConnection.insertSoilMoistureData(data);  // Update database
        }
    }

    @FXML private void onLivestockFeedingCollect() {
        if (animatedDrone == null || droneBase == null || pastureItems.isEmpty()) {  // Check pastureItems
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

            SequentialTransition moveToPasture = droneAnim.moveDrone(pasture.getLayoutX(), pasture.getLayoutY());
            moveToPasture.setOnFinished(event -> {
                DatabaseConnection.insertLivestockFeedingData(feedingData);
            });
            transitions.add(moveToPasture);
        }

        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

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

    @FXML private void onFeedLivestock() {
        for (LivestockFeedingData data : livestockDataMap.values()) {
            data.setFeedingLevel(10);  // Reset feeding level to 10
            DatabaseConnection.insertLivestockFeedingData(data);  // Update database
        }
    }

    @FXML private void onPestDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting pest data...");

        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

        List<SequentialTransition> transitions = new ArrayList<>();

        for (Rectangle field : fieldItems) {
            PestData pestData = findOrCreatePestData(field);
            pestData.increasePestLevel();  // Increase pest level between 0-3
            pestData.setTimestamp(timestamp);

            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                DatabaseConnection.insertPestData(pestData);
            });
            transitions.add(moveToField);
        }

        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        transitions.add(returnToBase);

        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
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

    @FXML private void onSprayPesticide() {
        for (PestData data : pestDataMap.values()) {
            data.setPestLevel(0);  // Reset pest level to 0
            DatabaseConnection.insertPestData(data);  // Update database
        }
    }

}