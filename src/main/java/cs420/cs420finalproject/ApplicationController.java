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
import javafx.scene.control.cell.TextFieldTreeCell;
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
    private Map<String, CropGrowthData> cropDataMap = new HashMap<>(); // Mapping of crop data
    @FXML private LineChart<String, Number> growthLineChart; // Reference to the growth line chart
    @FXML private TreeView<String> itemTreeView; // TreeView to display all items
    Set<String> addedItems = new HashSet<>(); // Set to track added items

    public void initialize() {
        statusLabel.setText("System ready.");
        List<CropGrowthData> savedCropData = DatabaseConnection.getCropGrowthData();
        for (CropGrowthData data : savedCropData) {
            cropDataMap.put(data.getFieldId(), data);
        }
        renderItems(); // Render all items with the correct order
        loadItemsIntoTree();

        // Redirect System.out to the TextArea
        System.setOut(new PrintStream(new TextAreaOutputStream(System.out, logs), true));
    }

    private void loadItemsIntoTree() {
        // Load all items from the database
        List<Item> savedItems = DatabaseConnection.getItems();
        Map<String, TreeItem<String>> itemTreeMap = new HashMap<>(); // Map to track TreeItems by item name
        Map<String, Container> containerMap = new HashMap<>(); // Map to track containers by name

        // First pass: Create tree nodes for all items and containers
        for (Item item : savedItems) {
            TreeItem<String> itemNode = new TreeItem<>(item.getName());
            itemTreeMap.put(item.getName(), itemNode); // Map the tree node to the item's name

            // If the item is a container, track it
            if (item instanceof Container) {
                containerMap.put(item.getName(), (Container) item);
            }
        }

        // Second pass: Link contained items and sub-containers
        for (Container container : containerMap.values()) {
            TreeItem<String> containerNode = itemTreeMap.get(container.getName());

            // Retrieve contained items for the container
            List<Item> containedItems = container.getContainedItems();

            // Add contained items as children of the container node
            for (Item containedItem : containedItems) {
                TreeItem<String> containedNode = itemTreeMap.get(containedItem.getName());
                if (containedNode != null) {
                    // Ensure the contained node has not been linked to another container
                    if (containedNode.getParent() == null) {
                        containerNode.getChildren().add(containedNode);
                    }
                }
            }
        }

        // Build the root of the tree
        TreeItem<String> root = new TreeItem<>("Items"); // Root node for the TreeView

        // Add top-level items (not contained in any container) to the root
        for (TreeItem<String> node : itemTreeMap.values()) {
            if (node.getParent() == null) { // Only add nodes that are not yet attached to a parent
                root.getChildren().add(node);
            }
        }

        // Always expand the root node
        root.setExpanded(true);

        // Set the root node to the TreeView
        itemTreeView.setRoot(root);
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
                addItemToPaneFromDatabase(item);
                loadItemsIntoTree();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemToPaneFromDatabase(Item item) {
        if ("drone".equals(item.getType())) {
            addDroneToPane(item.getX(), item.getY());
        } else if ("drone base".equals(item.getType())) {
            addDroneBase();
        } else if (item instanceof Container) {
            addContainerToPane((Container) item, addedItems, calculateContainerDepth((Container) item));
        } else {
            // Handle regular items
            Rectangle itemRectangle = createVisualItem(item.getType());
            itemRectangle.setLayoutX(item.getX());
            itemRectangle.setLayoutY(item.getY());
            dronePane.getChildren().add(itemRectangle);
        }
    }

    private void addContainerToPane(Container container, Set<String> addedItems, int depth) {
        // Adjust size based on container depth
        double containerWidth = 100 + depth * 50;
        double containerHeight = 100 + depth * 50;

        // Create the container's visual representation
        Rectangle containerRectangle = new Rectangle(containerWidth, containerHeight);
        containerRectangle.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
        containerRectangle.setLayoutX(container.getX());
        containerRectangle.setLayoutY(container.getY());

        // Add container to the pane
        dronePane.getChildren().add(containerRectangle);

        // Position items inside the container and render them
        double padding = 10;
        double itemSize = (containerRectangle.getWidth() - 2 * padding) / 2;
        double startX = containerRectangle.getLayoutX() + padding;
        double startY = containerRectangle.getLayoutY() + padding;

        int index = 0;
        for (Item containedItem : container.getContainedItems()) {
            // Prevent adding the same item multiple times
            if (!addedItems.contains(containedItem.getName())) {
                Rectangle containedRectangle = createVisualItem(containedItem.getType());
                containedRectangle.setWidth(itemSize);
                containedRectangle.setHeight(itemSize);

                // Calculate position of the contained item
                double offsetX = index % 2 == 0 ? 0 : itemSize + padding;
                double offsetY = index / 2 * (itemSize + padding);

                containedRectangle.setLayoutX(startX + offsetX);
                containedRectangle.setLayoutY(startY + offsetY);

                // Add item to pane
                dronePane.getChildren().add(containedRectangle);
                addedItems.add(containedItem.getName()); // Mark item as added
                index++;
            }
        }

        // Recursively render items in nested containers
        for (Item containedItem : container.getContainedItems()) {
            if (containedItem instanceof Container) {
                addContainerToPane((Container) containedItem, addedItems, depth + 1); // Increase depth for nested containers
            }
        }
    }

    private int calculateContainerDepth(Container container) {
        int depth = 0;
        Container parentContainer = container.getParentContainer(); // Assuming each container has a reference to its parent
        while (parentContainer != null) {
            depth++;
            parentContainer = parentContainer.getParentContainer();
        }
        return depth;
    }

    private void renderItems() {
        Set<String> addedItems = new HashSet<>(); // Set to track added items

        // First, add all containers and their contained items
        for (Item item : DatabaseConnection.getItems()) {
            if (item instanceof Container) {
                addContainerToPane((Container) item, addedItems, calculateContainerDepth((Container) item)); // Pass the set to avoid duplicates
            }
        }

        // Next, add non-container items
        for (Item item : DatabaseConnection.getItems()) {
            if (!(item instanceof Container) && !addedItems.contains(item.getName())) {
                // Avoid duplicate addition if the item is already added as part of a container
                Rectangle itemRectangle = createVisualItem(item.getType());
                itemRectangle.setLayoutX(item.getX());
                itemRectangle.setLayoutY(item.getY());
                dronePane.getChildren().add(itemRectangle);
                addedItems.add(item.getName()); // Mark as added
            }
        }
    }


    // Method to add the animated drone to the pane
    private void addDroneToPane(double x, double y) {
        if (animatedDrone == null) { // Ensure only one drone is added
            animatedDrone = new Circle(10); // A drone represented as a circle, radius 10
            animatedDrone.setLayoutX(x); // Initial X position
            animatedDrone.setLayoutY(y); // Initial Y position
            dronePane.getChildren().add(animatedDrone); // Add to the Pane
        }
    }

    // Method to add the drone base
    private void addDroneBase() {
        if (droneBase == null) { // Ensure only one base is added
            droneBase = new Rectangle(50, 50);
            droneBase.setLayoutX(350); // Position for the drone base
            droneBase.setLayoutY(350);
            droneBase.setStyle("-fx-fill: brown;");
            dronePane.getChildren().add(droneBase);
        }
    }


    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50); // Default size
        switch (itemType) {
            case "field": item.setStyle("-fx-fill: green;"); break;
            case "pasture": item.setStyle("-fx-fill: lightgreen;"); break;
            default: item.setStyle("-fx-fill: gray;");
        }
        return item;
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
}
