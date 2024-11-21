package cs420.cs420finalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    // The single instance of MainApplication
    private static MainApplication instance;

    // Public constructor for JavaFX
    public MainApplication() {
        // Prevent instantiation directly by other classes
        if (instance != null) {
            throw new IllegalStateException("MainApplication is a singleton class and cannot be instantiated more than once.");
        }
        instance = this;
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("farmManagementView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Agricultural Drone Automation System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Initialize the database before launching
        DatabaseInitializer.initialize();

        // Launch the application
        launch();
    }
}