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
        TreeItem<String> root = loadItemsIntoTree();
        renderItemsFromTree(root);
        System.setOut(new PrintStream(new TextAreaOutputStream(System.out, logs), true));
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
        itemTreeView.setRoot(root);

        printTreeStructure(root,0);

        // Return the root of the tree
        return root;
    }

    // Helper method to print the tree structure
    private void printTreeStructure(TreeItem<String> node, int depth) {
        String indent = "  ".repeat(depth); // Create indentation based on depth
        System.out.println(indent + node.getValue());
        for (TreeItem<String> child : node.getChildren()) {
            printTreeStructure(child, depth + 1); // Recursively print children
        }
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
                renderItemsFromTree(itemTreeView.getRoot());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderItemsFromTree(TreeItem<String> rootNode) {
        // Create a set to track added items and prevent duplication
        Set<String> addedItems = new HashSet<>();
        // Loop over all children of the root (not the root itself)
        for (TreeItem<String> childNode : rootNode.getChildren()) {
            renderTreeNode(childNode, addedItems, 1); // Start with depth 1 to skip rendering root
        }
    }

    private void renderTreeNode(TreeItem<String> node, Set<String> addedItems, int depth) {
        String itemName = node.getValue();

        // Skip the root item by checking if it has the value "Items" (or whatever your root value is)
        if (depth == 0) {
            return; // Skip root node (this is the first level of depth)
        }

        // Fetch the item from the database by name
        Item item = DatabaseConnection.getItemByName(itemName);

        if (item == null || addedItems.contains(itemName)) {
            return; // Skip if the item does not exist or is already rendered
        }

        System.out.println("Rendering " + (item instanceof Container ? "Container" : "Item") +
                ": " + itemName + " at depth " + depth);

        if (item instanceof Container) {
            // Render container and its children
            renderContainer((Container) item, node, addedItems, depth);
        } else {
            // Render regular item
            renderItem(item);
        }

        // Mark the item as added
        addedItems.add(itemName);

        // Render children recursively
        for (TreeItem<String> childNode : node.getChildren()) {
            renderTreeNode(childNode, addedItems, depth + 1);
        }
    }

    private void renderContainer(Container container, TreeItem<String> node, Set<String> addedItems, int depth) {

        System.out.println("Rendering Container: " + container.getName() +
                " at depth " + depth + " with position (" + container.getX() + ", " + container.getY() + ")");

        // Create a visual representation for the container
        double baseSize = 150; // Base size for containers
        double containerWidth = baseSize + depth * 50; // Increase size with depth
        double containerHeight = baseSize + depth * 50;
        double padding = 10; // Padding for contained items

        Rectangle containerRectangle = new Rectangle(containerWidth, containerHeight);
        containerRectangle.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
        containerRectangle.setLayoutX(container.getX());
        containerRectangle.setLayoutY(container.getY());
        dronePane.getChildren().add(containerRectangle);

        // Calculate available space for child items inside the container
        double innerWidth = containerWidth - 2 * padding;
        double innerHeight = containerHeight - 2 * padding;
        double itemSize = Math.min(innerWidth, innerHeight) / Math.max(2, node.getChildren().size()); // Adjust size based on the number of children

        // Start position for child items inside the container
        double startX = containerRectangle.getLayoutX() + padding;
        double startY = containerRectangle.getLayoutY() + padding;

        int index = 0; // Index to position items
        for (TreeItem<String> childNode : node.getChildren()) {
            Item childItem = DatabaseConnection.getItemByName(childNode.getValue());
            if (childItem != null && !addedItems.contains(childItem.getName())) {
                if (childItem instanceof Container) {
                    // Render contained container recursively
                    renderContainer((Container) childItem, childNode, addedItems, depth + 1);
                } else {
                    // Render a regular contained item
                    Rectangle childRectangle = createVisualItem(childItem.getType());
                    childRectangle.setWidth(itemSize);
                    childRectangle.setHeight(itemSize);
                    // Calculate position within the container grid
                    double itemX = startX + (index % 2) * (itemSize + padding);
                    double itemY = startY + (index / 2) * (itemSize + padding);
                    childRectangle.setLayoutX(itemX);
                    childRectangle.setLayoutY(itemY);
                    dronePane.getChildren().add(childRectangle);
                }
                addedItems.add(childItem.getName());
                index++;
            }
        }
    }

    private void renderItem(Item item) {
        // Render regular items visually based on their type and coordinates
        Rectangle itemRectangle = createVisualItem(item.getType());
        itemRectangle.setLayoutX(item.getX());
        itemRectangle.setLayoutY(item.getY());
        System.out.println("Rendering Item: " + item.getName() +
                " at position (" + item.getX() + ", " + item.getY() + ")");
        dronePane.getChildren().add(itemRectangle);
    }

    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50);
        switch (itemType.toLowerCase()) {
            case "field": item.setStyle("-fx-fill: green;"); break;
            case "pasture": item.setStyle("-fx-fill: lightgreen;"); break;
            case "container": item.setStyle("-fx-fill: lightgray;"); break;
            case "drone": addDroneToPane(item.getX(),item.getY());
            case "drone base": addDroneBase();
            default: item.setStyle("-fx-fill: gray;");
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
