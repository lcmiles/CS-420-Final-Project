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
        renderItems();
        loadItemsIntoTree();
        System.setOut(new PrintStream(new TextAreaOutputStream(System.out, logs), true));
    }

    private void loadItemsIntoTree() {
        List<Item> savedItems = DatabaseConnection.getItems();
        Map<String, TreeItem<String>> itemTreeMap = new HashMap<>();
        Map<String, Container> containerMap = new HashMap<>();

        for (Item item : savedItems) {
            TreeItem<String> itemNode = new TreeItem<>(item.getName());
            itemTreeMap.put(item.getName(), itemNode);

            if (item instanceof Container) {
                containerMap.put(item.getName(), (Container) item);
            }
        }

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

        TreeItem<String> root = new TreeItem<>("Items");
        for (TreeItem<String> node : itemTreeMap.values()) {
            if (node.getParent() == null) {
                root.getChildren().add(node);
            }
        }
        root.setExpanded(true);
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
            Container container = (Container) item;
            Rectangle containerRectangle = createVisualItem(item.getType());
            containerRectangle.setLayoutX(item.getX());
            containerRectangle.setLayoutY(item.getY());
            containerRectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
            dronePane.getChildren().add(containerRectangle);

            List<Item> containedItems = container.getContainedItems();
            double containerX = item.getX();
            double containerY = item.getY();
            double offsetX = 10;
            double offsetY = 10;

            for (Item containedItem : containedItems) {
                Rectangle containedRectangle = createVisualItem(containedItem.getType());
                containedRectangle.setLayoutX(containerX + offsetX);
                containedRectangle.setLayoutY(containerY + offsetY);
                containedRectangle.setScaleX(0.8);
                containedRectangle.setScaleY(0.8);
                dronePane.getChildren().add(containedRectangle);
                offsetX += 60;
                if (offsetX > 100) {
                    offsetX = 10;
                    offsetY += 60;
                }
                if (containedItem instanceof Container) {
                    addNestedContainers((Container) containedItem, offsetX, offsetY);
                }
            }
        } else {
            Rectangle itemRectangle = createVisualItem(item.getType());
            itemRectangle.setLayoutX(item.getX());
            itemRectangle.setLayoutY(item.getY());
            dronePane.getChildren().add(itemRectangle);
        }
    }

    private void addContainerToPane(Container container, Set<String> addedItems, int depth) {
        // Create the container visual element (rectangle)
        double containerWidth = 100 + depth * 50;
        double containerHeight = 100 + depth * 50;
        Rectangle containerRectangle = new Rectangle(containerWidth, containerHeight);
        containerRectangle.setStyle("-fx-fill: lightgray; -fx-stroke: black; -fx-stroke-width: 2;");
        containerRectangle.setLayoutX(container.getX());
        containerRectangle.setLayoutY(container.getY());
        dronePane.getChildren().add(containerRectangle);

        // Track items that have been added to prevent duplicates
        addedItems.add(container.getName());

        // Add contained items (not containers) to the current container
        double padding = 10;
        double itemSize = (containerRectangle.getWidth() - 2 * padding) / 2;
        double startX = containerRectangle.getLayoutX() + padding;
        double startY = containerRectangle.getLayoutY() + padding;

        int index = 0;
        for (Item containedItem : container.getContainedItems()) {
            if (!addedItems.contains(containedItem.getName())) {
                Rectangle containedRectangle = createVisualItem(containedItem.getType());
                containedRectangle.setWidth(itemSize);
                containedRectangle.setHeight(itemSize);
                double offsetX = index % 2 == 0 ? 0 : itemSize + padding;
                double offsetY = index / 2 * (itemSize + padding);
                containedRectangle.setLayoutX(startX + offsetX);
                containedRectangle.setLayoutY(startY + offsetY);
                dronePane.getChildren().add(containedRectangle);
                addedItems.add(containedItem.getName());
                index++;
            }
        }

        // Recursively add sub-containers
        for (Item containedItem : container.getContainedItems()) {
            if (containedItem instanceof Container) {
                // Properly adjust the offset for sub-containers to place them within the parent container
                addContainerToPane((Container) containedItem, addedItems, depth + 1);
            }
        }
    }


    private void renderItems() {
        Set<String> addedItems = new HashSet<>();
        for (Item item : DatabaseConnection.getItems()) {
            if (item instanceof Container && !addedItems.contains(item.getName())) {
                addContainerToPane((Container) item, addedItems, 0);  // Start with depth 0 for root containers
            }
        }

        // Now add non-container items (those that are not in containers)
        for (Item item : DatabaseConnection.getItems()) {
            System.out.println("Item type: " + item.getClass().getName());
            if (!(item instanceof Container) && !addedItems.contains(item.getName())) {
                Rectangle itemRectangle = createVisualItem(item.getType());
                itemRectangle.setLayoutX(item.getX());
                itemRectangle.setLayoutY(item.getY());
                dronePane.getChildren().add(itemRectangle);
                addedItems.add(item.getName());
            }
        }
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

    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50);
        switch (itemType) {
            case "field": item.setStyle("-fx-fill: green;"); break;
            case "pasture": item.setStyle("-fx-fill: lightgreen;"); break;
            case "container": item.setStyle("-fx-fill: lightgray;"); break;
            default: item.setStyle("-fx-fill: gray;");
        }
        return item;
    }

    private void addNestedContainers(Container parentContainer, double offsetX, double offsetY) {
        if (parentContainer != null) {
            double width = 100;
            double height = 100;
            Rectangle containerRectangle = new Rectangle(width, height);
            containerRectangle.setStyle("-fx-fill: lightblue;");
            containerRectangle.setLayoutX(offsetX);
            containerRectangle.setLayoutY(offsetY);
            dronePane.getChildren().add(containerRectangle);

            for (Item containedItem : parentContainer.getContainedItems()) {
                addItemToPaneFromDatabase(containedItem);
            }
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
