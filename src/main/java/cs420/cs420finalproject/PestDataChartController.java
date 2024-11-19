package cs420.cs420finalproject;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

public class PestDataChartController {

    @FXML
    private LineChart<String, Number> pestDataLineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public void initialize() {
        List<PestData> pestData = DatabaseConnection.getPestData(); // Fetch pest data

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pest Data");

        for (PestData data : pestData) {
            // Replace getActivityLevel() with getPestLevel()
            series.getData().add(new XYChart.Data<>(data.getTimestamp(), data.getPestLevel()));
        }

        pestDataLineChart.getData().add(series);
    }
}