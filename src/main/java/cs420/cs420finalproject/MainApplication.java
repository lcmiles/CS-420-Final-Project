package cs420.cs420finalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("farmManagementView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Agricultural Drone Automation System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        launch();
    }
}
