package cs420.cs420finalproject;

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
import javafx.animation.SequentialTransition;
import java.util.*;
import javafx.scene.chart.LineChart;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ApplicationController {

    @FXML private Label statusLabel;
    @FXML private Pane dronePane;
    private Circle animatedDrone;
    private Rectangle droneBase; // The drone base where the drone returns
    private List<Rectangle> fieldItems = new ArrayList<>(); // List of fields for the drone to visit
    private Map<String, CropGrowthData> cropDataMap = new HashMap<>();
    @FXML private LineChart<String, Number> growthLineChart; // Reference to the growth line chart
    // TableView to display all items
    @FXML private TableView<Item> itemTableView;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, Double> xColumn;
    @FXML private TableColumn<Item, Double> yColumn;
    @FXML private TableColumn<Item, Boolean> isContainerColumn;

    @FXML
    public void initialize() {
        statusLabel.setText("System ready.");
        // Load saved crop data from the database
        List<CropGrowthData> savedCropData = DatabaseConnection.getCropGrowthData();
        for (CropGrowthData data : savedCropData) {
            cropDataMap.put(data.getFieldId(), data); // Initialize the map with saved data
        }
        // Load saved items from the database
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
        isContainerColumn.setCellValueFactory(new PropertyValueFactory<>("isContainer"));
    }

    private void loadItemsIntoTable() {
        ObservableList<Item> items = FXCollections.observableArrayList(DatabaseConnection.getItems());
        itemTableView.setItems(items);
    }

    // Add item to the pane depending on its type (called from AddItemController)
    @FXML public void addItemToPane() {
        openItemDetailsPopup(); // Open the popup to select item type and enter details
    }

    private void openItemDetailsPopup() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addItemView.fxml"));
            Parent root = fxmlLoader.load();
            AddItemController controller = fxmlLoader.getController(); // Get the controller for the popup
            // Open the popup window
            Stage stage = new Stage();
            stage.setTitle("Add New Item");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Wait for the popup to close
            // Once the popup is closed, check if the item is created and save it if it was
            if (controller.isItemCreated()) {
                Item item = controller.getItem();
                DatabaseConnection.insertItem(item);
                addItemToPaneFromDatabase(item); // Add the new item to the pane
                loadItemsIntoTable(); // Refresh the table to show the new item
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemToPaneFromDatabase(Item item) {
        // This method will now handle both container and non-container items uniformly
        Rectangle itemRectangle = createVisualItem(item.getType());
        itemRectangle.setLayoutX(item.getX());  // Set X position from database
        itemRectangle.setLayoutY(item.getY());  // Set Y position from database
        dronePane.getChildren().add(itemRectangle);  // Add to pane
        if (item instanceof Container) {
            Container container = (Container) item;
            double containedItemX = item.getX() + 10;  // Offset for contained items
            double containedItemY = item.getY() + 10;  // Offset for contained items
            // Add contained items inside the container
            for (Item containedItem : container.getContainedItems()) {
                Rectangle containedRectangle = createVisualItem(containedItem.getType());
                containedRectangle.setLayoutX(containedItemX);
                containedRectangle.setLayoutY(containedItemY);
                dronePane.getChildren().add(containedRectangle);
                containedItemY += containedRectangle.getHeight() + 10;  // Space out contained items
            }
        }
        if (item.getType().equals("Field")) {
            fieldItems.add(itemRectangle); // Track field items separately
        }
    }

    private Rectangle createVisualItem(String itemType) {
        Rectangle item = new Rectangle(50, 50);  // Default size
        // Set color based on item type
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
            default:
                item.setStyle("-fx-fill: gray;");  // Default color for unknown types
                break;
        }
        item.setStyle(item.getStyle() + " -fx-stroke: black; -fx-stroke-width: 2;");  // Add border
        return item;
    }

    @FXML private void onCropDataCollect() {
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
            cropData.increaseGrowthLevel();
            cropData.setTimestamp(timestamp); // Update the timestamp
            SequentialTransition moveToField = droneAnim.moveDrone(field.getLayoutX(), field.getLayoutY());
            moveToField.setOnFinished(event -> DatabaseConnection.insertCropGrowthData(cropData));
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
        String fieldId = "Field " + fieldItems.indexOf(field);
        if (cropDataMap.containsKey(fieldId)) {
            return cropDataMap.get(fieldId);
        }
        CropGrowthData newData = new CropGrowthData(
                new SimpleDateFormat("dd/mm/yy HH:mm:ss").format(new Date()), // Timestamp
                fieldId, // Field ID
                0 // Initial growth level set to 0
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
                cropData.setTimestamp(new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()));
                DatabaseConnection.insertCropGrowthData(cropData);
                System.out.println("Crops harvested.");
                statusLabel.setText("Crops harvested. System ready.");
            }
        }
    }

}