package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

public class LivestockFeedingChartController {

    @FXML
    private LineChart<String, Number> livestockFeedingLineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public void initialize() {
        List<LivestockFeedingData> livestockFeedingData = DatabaseConnection.getLivestockFeedingData(); // Implement this function in DatabaseConnection

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Livestock Feeding Data");

        for (LivestockFeedingData data : livestockFeedingData) {
            series.getData().add(new XYChart.Data<>(data.getTimestamp(), data.getFeedingLevel()));
        }

        livestockFeedingLineChart.getData().add(series);
    }
}
