package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ApplicationController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("System ready.");
    }

    @FXML
    private void onCollectData() {
        statusLabel.setText("Collecting data...");
        // Logic to collect data from drones and database calls
        DatabaseConnection.collectData();
        statusLabel.setText("Data collection completed.");
    }

    @FXML
    private void onViewData() {
        statusLabel.setText("Viewing data...");
        // Logic to view collected data
    }

    @FXML
    private void onIrrigate() {
        statusLabel.setText("Irrigating...");
        // Logic for irrigation functionality
    }
}
