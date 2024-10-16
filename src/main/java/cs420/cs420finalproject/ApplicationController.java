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
import javafx.scene.chart.XYChart;
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
        String timestamp = new SimpleDateFormat("dd/mm/yy HH:mm:ss").format(new Date());
        // Collect the transitions for each field and the final return to base
        List<SequentialTransition> transitions = new ArrayList<>();
        for (Rectangle field : fieldItems) {
            // Retrieve existing CropGrowthData or create a new one
            CropGrowthData cropData = findOrCreateCropData(field);
            cropData.increaseGrowthLevel(); // Increase the growth level
            // Set the timestamp for the cropData
            cropData.setTimestamp(timestamp); // Ensure you have a setter for timestamp in CropGrowthData
            // Create a transition to move the drone to the field
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            // Add a listener to the transition to insert data into the database after the drone moves to the field
            moveToField.setOnFinished(event -> {
                DatabaseConnection.insertCropGrowthData(cropData); // Insert the crop data into the database
            });
            transitions.add(moveToField);
        }
        // Finally, animate the drone to return to the base
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        transitions.add(returnToBase);
        // Add a listener to the last transition (drone returns to base)
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        // Execute the transitions sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
        // Play the animation
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
}