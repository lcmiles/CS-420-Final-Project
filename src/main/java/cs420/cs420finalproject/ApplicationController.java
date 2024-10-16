package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.animation.SequentialTransition;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.scene.chart.LineChart;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ApplicationController {

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<String> itemTypeComboBox; // Dropdown for item types

    @FXML
    private Pane dronePane; // Pane for displaying the drone and draggable items
    private Circle animatedDrone; // A visual drone object
    private Rectangle droneBase; // The drone base where the drone returns
    private List<Rectangle> fieldItems = new ArrayList<>(); // List of fields for the drone to visit
    private Map<String, CropGrowthData> cropDataMap = new HashMap<>();

    @FXML
    private LineChart<String, Number> growthLineChart; // Reference to the growth line chart

    @FXML
    public void initialize() {
        statusLabel.setText("System ready.");
        itemTypeComboBox.getSelectionModel().selectFirst(); // Set default item type
        // Load saved crop data from the database
        List<CropGrowthData> savedCropData = DatabaseConnection.getCropGrowthData();
        for (CropGrowthData data : savedCropData) {
            cropDataMap.put(data.getFieldId(), data); // Initialize the map with saved data
        }
    }

    // Method to add a draggable item to the Pane based on the selected type
    @FXML
    public void addItemToPane() {
        String itemType = itemTypeComboBox.getValue();
        if (itemType.equals("Drone")) {
            addDroneToPane(); // Add animated drone
        } else if (itemType.equals("Drone Base")) {
            addDroneBase(); // Add the drone base
        } else {
            Rectangle item = createDraggableItem(itemType);
            dronePane.getChildren().add(item); // Add to the pane
            if (itemType.equals("Field")) {
                fieldItems.add(item); // Track field items
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

    // Method to add the drone base
    private void addDroneBase() {
        if (droneBase == null) { // Ensure only one base is added
            droneBase = new Rectangle(50, 50);
            droneBase.setLayoutX(350); // Position for the drone base
            droneBase.setLayoutY(350);
            droneBase.setStyle("-fx-fill: brown;");
            dronePane.getChildren().add(droneBase);
        }
        makeDraggable(droneBase);
    }

    // Method to create a draggable item (Field, Pasture, Irrigation)
    private Rectangle createDraggableItem(String itemType) {
        Rectangle item = new Rectangle(50, 50); // Default size
        item.setLayoutX(100); // Starting position
        item.setLayoutY(100);
        // Assign a unique color based on the type
        switch (itemType) {
            case "Field":
                item.setStyle("-fx-fill: green;");
                break;
            case "Pasture":
                item.setStyle("-fx-fill: lightgreen;");
                break;
            case "Irrigation":
                item.setStyle("-fx-fill: blue;");
                break;
        }
        makeDraggable(item);
        return item;
    }

    // Make an item draggable
    private void makeDraggable(Rectangle item) {
        final double[] offsetX = {0};
        final double[] offsetY = {0};
        item.setOnMousePressed(event -> {
            offsetX[0] = event.getSceneX() - item.getLayoutX();
            offsetY[0] = event.getSceneY() - item.getLayoutY();
        });
        item.setOnMouseDragged(event -> {
            item.setLayoutX(event.getSceneX() - offsetX[0]);
            item.setLayoutY(event.getSceneY() - offsetY[0]);
        });
    }

    @FXML
    private void onCropDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting crop growth data...");
        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        // Create a timestamp for the data collection
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        List<SequentialTransition> transitions = new ArrayList<>();
        for (Rectangle field : fieldItems) {
            CropGrowthData cropData = findOrCreateCropData(field);
            // If the crop has been harvested, avoid immediately setting it back to 10.
            cropData.increaseGrowthLevel();
            cropData.setTimestamp(timestamp); // Update the timestamp
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> {
                DatabaseConnection.insertCropGrowthData(cropData); // Update database with new data
            });
            transitions.add(moveToField);
        }
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        transitions.add(returnToBase);
        returnToBase.setOnFinished(event -> statusLabel.setText("System ready."));
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
        allTransitions.play();
    }
    
    // Find existing CropGrowthData or create a new one
    public CropGrowthData findOrCreateCropData(Rectangle field) {
        // Create a unique field ID based on the field's index
        String fieldId = "Field " + fieldItems.indexOf(field);
        // Check if the crop data already exists in the map
        if (cropDataMap.containsKey(fieldId)) {
            return cropDataMap.get(fieldId); // Return the existing CropGrowthData
        }
        // If no data exists, create new CropGrowthData with initial values
        CropGrowthData newData = new CropGrowthData(
                new SimpleDateFormat("dd/mm/yy HH:mm:ss").format(new Date()), // Timestamp
                fieldId, // Field ID
                0 // Initial growth level set to 0
        );
        // Store the newly created CropGrowthData in the map for future use
        cropDataMap.put(fieldId, newData);
        return newData;
    }
    
    // Method to open a popup and view the growth chart
    @FXML
    public void onViewChartButtonClicked() {
        try {
            // Load the FXML for the popup
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("growthChartView.fxml"));
            Parent root = fxmlLoader.load();
            // Create a new Stage (window)
            Stage stage = new Stage();
            stage.setTitle("Crop Growth Chart");
            // Set the scene to the loaded FXML
            Scene scene = new Scene(root);
            stage.setScene(scene);
            // Specify that the popup should be modal (block input to other windows)
            stage.initModality(Modality.APPLICATION_MODAL);
            // Access the controller of the chart window and update the chart data
            GrowthChartController chartController = fxmlLoader.getController();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to handle the "Harvest Crops" button action
    @FXML
    private void handleHarvestCrops() {
        String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());
        for (Rectangle field : fieldItems) {
            CropGrowthData cropData = findOrCreateCropData(field);
            if (cropData.getGrowthLevel() == 10) {
                cropData.setGrowthLevel(0); // Reset growth level
                cropData.setTimestamp(new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date())); // Update timestamp
                DatabaseConnection.insertCropGrowthData(cropData); // Save to database
                System.out.println("Crops harvested.");
                statusLabel.setText("Crops harvested. System ready.");
            }
        }
    }

}