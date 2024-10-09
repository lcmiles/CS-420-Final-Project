package cs420.cs420finalproject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;
public class ApplicationController {
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> itemTypeComboBox; // Dropdown for item types
    @FXML
    private Pane dronePane; // Pane for displaying the drone and draggable items
    private List<Circle> drones = new ArrayList<>(); // List to store visual drone representations
    @FXML
    public void initialize() {
        statusLabel.setText("System ready.");
        itemTypeComboBox.getSelectionModel().selectFirst(); // Set default item type
    }
    @FXML
    private void onCollectData() {
        statusLabel.setText("Collecting data...");
        DatabaseConnection.collectData();
        statusLabel.setText("Data collection completed.");
    }
    @FXML
    private void onViewData() {
        statusLabel.setText("Viewing data...");
    }
    @FXML
    private void onIrrigate() {
        statusLabel.setText("Irrigating...");
        if (!drones.isEmpty()) {
            Circle drone = drones.get(0);
            DroneAnimation droneAnim = new DroneAnimation(drone);
            droneAnim.moveDrone(200, 300); // Coordinates for the irrigation task
        }
    }
    // Method to add a new drone to the right-side panel
    @FXML
    public void addDroneToPane() {
        Circle newDrone = new Circle(10); // A drone represented as a circle, radius 10
        newDrone.setLayoutX(50); // Initial X position
        newDrone.setLayoutY(50); // Initial Y position
        drones.add(newDrone); // Add the drone to the list
        dronePane.getChildren().add(newDrone); // Add it to the Pane
    }
    // Method to add a draggable item to the Pane based on the selected type
    @FXML
    public void addItemToPane() {
        String itemType = itemTypeComboBox.getValue();
        Rectangle item = createDraggableItem(itemType);
        dronePane.getChildren().add(item); // Add to the pane
    }
    // Method to create a draggable Rectangle item for the selected type
    private Rectangle createDraggableItem(String itemType) {
        Rectangle item = new Rectangle(50, 50); // Default size
        item.setLayoutX(100); // Starting position
        item.setLayoutY(100);
        // Assign a unique color or label based on the type
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
            case "Drone":
                item.setStyle("-fx-fill: gray;");
                break;
            case "Drone Base":
                item.setStyle("-fx-fill: brown;");
                break;
        }
        // Make the item draggable
        makeDraggable(item);
        return item;
    }
    // Method to make an item draggable
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
}