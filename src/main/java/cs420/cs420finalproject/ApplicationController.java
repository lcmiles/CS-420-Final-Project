package cs420.cs420finalproject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.animation.SequentialTransition;
import java.util.ArrayList;
import java.util.List;
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
    // Method to animate the drone to each field item, then return to the base
    @FXML
    private void onCropDataCollect() {
        if (animatedDrone == null || droneBase == null || fieldItems.isEmpty()) {
            statusLabel.setText("Add a drone, base, and fields first.");
            return;
        }
        statusLabel.setText("Collecting crop growth data...");
        DroneAnimation droneAnim = new DroneAnimation(animatedDrone);
        // Collect the transitions for each field and the final return to base
        List<SequentialTransition> transitions = new ArrayList<>();
        // Animate the drone to each field
        for (Rectangle field : fieldItems) {
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            transitions.add(moveToField);
        }
        // Finally, animate the drone to return to the base
        SequentialTransition returnToBase = droneAnim.moveDrone(droneBase.getLayoutX(), droneBase.getLayoutY());
        transitions.add(returnToBase);
        // Execute the transitions sequentially
        SequentialTransition allTransitions = new SequentialTransition();
        allTransitions.getChildren().addAll(transitions);
        // Add a listener to the last transition (drone returns to base)
        returnToBase.setOnFinished(event -> {
            statusLabel.setText("System ready.");
        });
        // Play the animation
        allTransitions.play();
    }
}