package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GrowthChartController {

    @FXML
    private LineChart<String, Number> growthLineChart;

    // This method will be called when the popup is opened
    public void initialize() {
        populateGrowthData();
    }

    private void populateGrowthData() {
        // Clear previous data
        growthLineChart.getData().clear();
        // Retrieve crop growth data from the database
        List<CropGrowthData> growthDataList = DatabaseConnection.getCropGrowthData();
        // Group the data by field ID and create a series for each field
        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        for (CropGrowthData data : growthDataList) {
            // Get or create the series for the current field
            XYChart.Series<String, Number> series = seriesMap.getOrDefault(data.getFieldId(), new XYChart.Series<>());
            series.setName(data.getFieldId()); // Name the series after the field
            series.getData().add(new XYChart.Data<>(data.getTimestamp(), data.getGrowthLevel()));
            // Put the series back into the map
            seriesMap.put(data.getFieldId(), series);
        }
        // Add all series to the chart
        growthLineChart.getData().addAll(seriesMap.values());
    }

    public void updateGrowthChart() {
        List<CropGrowthData> growthData = DatabaseConnection.getCropGrowthData();
        // Clear previous data
        growthLineChart.getData().clear();
        // Create a series for each field
        for (CropGrowthData data : growthData) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(data.getFieldId()); // Use Field ID as series name
            // Add a data point for each timestamp and growth level
            series.getData().add(new XYChart.Data<>(data.getTimestamp(), data.getGrowthLevel()));
            // Add the series to the chart
            growthLineChart.getData().add(series);
        }
    }

}