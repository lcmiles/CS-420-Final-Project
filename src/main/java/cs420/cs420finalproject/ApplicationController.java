package cs420.cs420finalproject;

import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.scene.chart.LineChart;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ApplicationController {

    @FXML private Label statusLabel;
    @FXML private Pane dronePane;
    private Circle animatedDrone;
    private Rectangle droneBase;
    private List<Rectangle> fieldItems = new ArrayList<>(); // List of fields for the drone to visit
    private Map<String, CropGrowthData> cropDataMap = new HashMap<>(); // Mapping of crop data
    @FXML private LineChart<String, Number> growthLineChart; // Reference to the growth line chart
    @FXML private TableView<Item> itemTableView; // TableView to display all items
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, Double> xColumn;
    @FXML private TableColumn<Item, Double> yColumn;

    @FXML public void initialize() {
        statusLabel.setText("System ready.");
        List<CropGrowthData> savedCropData = DatabaseConnection.getCropGrowthData();
        for (CropGrowthData data : savedCropData) {
            cropDataMap.put(data.getFieldId(), data);
        }
        List<Item> savedItems = DatabaseConnection.getItems();
        for (Item item : savedItems) {
            addItemToPaneFromDatabase(item);
        }
        setupTableColumns();
        loadItemsIntoTable();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
    }

    private void loadItemsIntoTable() {
        ObservableList<Item> items = FXCollections.observableArrayList(DatabaseConnection.getItems());
        itemTableView.setItems(items);
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
                loadItemsIntoTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemToPaneFromDatabase(Item item) {
        // If the item is of type "drone", add the drone to the pane
        if ("drone".equals(item.getType())) {
            addDroneToPane();
        } else {
            // Otherwise, handle other items as usual
            Rectangle itemRectangle = createVisualItem(item.getType());
            itemRectangle.setLayoutX(item.getX());
            itemRectangle.setLayoutY(item.getY());
            dronePane.getChildren().add(itemRectangle);
            if (item.getType().equals("Field")) {
                fieldItems.add(itemRectangle);
            }
        }
    }

    // Method to add the animated drone to the pane
    private void addDroneToPane() {
        if (animatedDrone == null) { // Ensure only one drone is added
            animatedDrone = new Circle(10); // A drone represented as a circle, radius 10
            animatedDrone.setLayoutX(50); // Initial X position
            animatedDrone.setLayoutY(50); // Initial Y position
            dronePane.getChildren().add(animatedDrone); // Add to the Pane
        }
    }

    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50); // Default size
        switch (itemType) {
            case "Field": item.setStyle("-fx-fill: green;"); break;
            case "Pasture": item.setStyle("-fx-fill: lightgreen;"); break;
            case "Irrigation": item.setStyle("-fx-fill: blue;"); break;
            default: item.setStyle("-fx-fill: gray;");
        }
        item.setStyle(item.getStyle() + " -fx-stroke: black; -fx-stroke-width: 2;");
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

    @FXML public void onViewChartButtonClicked() {
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