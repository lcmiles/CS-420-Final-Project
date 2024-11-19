package cs420.cs420finalproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class FlightPlanConfigController {

    @FXML private ListView<String> dataTypesListView;

    private ObservableList<String> dataTypes;

    @FXML
    public void initialize() {
        // Initial data types that can be selected
        dataTypes = FXCollections.observableArrayList(
                "Crop Growth",
                "Soil Moisture",
                "Livestock Feeding",
                "Pest Data"
        );

        dataTypesListView.setItems(dataTypes);

        // Allow multiple selection
        dataTypesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // Move selected item up in the list
    @FXML
    private void moveUp() {
        List<Integer> selectedIndices = dataTypesListView.getSelectionModel().getSelectedIndices();
        if (!selectedIndices.isEmpty()) {
            for (int i = 0; i < selectedIndices.size(); i++) {
                int selectedIndex = selectedIndices.get(i);
                if (selectedIndex > 0) {
                    String selectedItem = dataTypes.get(selectedIndex);
                    dataTypes.remove(selectedIndex);
                    dataTypes.add(selectedIndex - 1, selectedItem);
                }
            }
        }
    }

    // Move selected item down in the list
    @FXML
    private void moveDown() {
        List<Integer> selectedIndices = dataTypesListView.getSelectionModel().getSelectedIndices();
        if (!selectedIndices.isEmpty()) {
            for (int i = selectedIndices.size() - 1; i >= 0; i--) {
                int selectedIndex = selectedIndices.get(i);
                if (selectedIndex < dataTypes.size() - 1) {
                    String selectedItem = dataTypes.get(selectedIndex);
                    dataTypes.remove(selectedIndex);
                    dataTypes.add(selectedIndex + 1, selectedItem);
                }
            }
        }
    }

    // Save the flight plan order with selected items
    @FXML
    private void saveFlightPlan() {
        // Get the selected data types in order
        List<String> selectedDataTypes = dataTypesListView.getSelectionModel().getSelectedItems();

        // Ensure the list is not empty
        if (!selectedDataTypes.isEmpty()) {
            // Call insertFlightPlan to save the selected order to the database
            DatabaseConnection.insertFlightPlan(selectedDataTypes);

            // Print confirmation for debugging
            System.out.println("Selected Flight Plan Order: " + selectedDataTypes);
        } else {
            System.out.println("No items selected for the flight plan.");
        }

        // Close the window
        closeWindow();
    }


    // Cancel the operation (close the window without saving)
    @FXML
    private void cancel() {
        closeWindow();
    }

    // Close the current window (this would be a dialog)
    private void closeWindow() {
        // Get the current stage and close it
        javafx.stage.Stage stage = (javafx.stage.Stage) dataTypesListView.getScene().getWindow();
        stage.close();
    }
}
