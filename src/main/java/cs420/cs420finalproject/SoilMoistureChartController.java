package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

public class SoilMoistureChartController {

    @FXML
    private LineChart<String, Number> soilMoistureLineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public void initialize() {
        List<SoilMoistureData> soilMoistureData = DatabaseConnection.getSoilMoistureData(); // Implement this function in DatabaseConnection

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Soil Moisture Data");

        for (SoilMoistureData data : soilMoistureData) {
            series.getData().add(new XYChart.Data<>(data.getTimestamp(), data.getMoistureLevel()));
        }

        soilMoistureLineChart.getData().add(series);
    }
}
