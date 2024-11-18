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

            // Debug: Print item class type and order
            System.out.println("Loading item: " + item.getName() + " | Class: " + item.getClass().getName());

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

        // Now load items into the visual pane (dronePane)
        loadItemsIntoVisualPane(containerMap);

        // Return the root of the tree
        return root;
    }


    private void loadItemsIntoVisualPane(Map<String, Container> containerMap) {
        // Iterate through the root's children (which now include items and containers)
        TreeItem<String> root = itemTreeView.getRoot();
        for (TreeItem<String> node : root.getChildren()) {
            // For top-level node, use actual coordinates (e.g., from item data)
            Item item = DatabaseConnection.getItemByName(node.getValue());
            double x = item.getX();
            double y = item.getY();
            loadItemNodeVisual(node, 0, x, y);
        }
    }

    private void loadItemNodeVisual(TreeItem<String> node, int depth, double offsetX, double offsetY) {
        // Calculate size based on depth, where top-level containers are larger
        double sizeFactor = 1 + (0.2 * (3 - depth)); // Scale factor that decreases with depth
        double containerSize = 100 * sizeFactor;  // Adjust container size for depth

        // Check if the current node has children. If it does, it's a container.
        if (node.getChildren().isEmpty()) {
            // Load non-container item
            Rectangle itemRect = createVisualItem(node.getValue());
            itemRect.setLayoutX(offsetX);
            itemRect.setLayoutY(offsetY);
            dronePane.getChildren().add(itemRect);

            // Add buffer space for next item
            offsetY += 10;
        } else {
            // Load the container as a rectangle with adjusted size based on depth
            Rectangle containerRect = new Rectangle(containerSize, containerSize);  // Size adjusted for depth
            containerRect.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
            containerRect.setLayoutX(offsetX);
            containerRect.setLayoutY(offsetY);
            dronePane.getChildren().add(containerRect);

            // Recursively load contained items within the container
            double containedOffsetX = offsetX + 10;
            double containedOffsetY = offsetY + 10;
            for (TreeItem<String> child : node.getChildren()) {
                loadItemNodeVisual(child, depth + 1, containedOffsetX, containedOffsetY);
                containedOffsetY += 10;  // Buffer space between contained items
            }
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
