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

//! add drop down with input box for other item type
//! edit/delete button greyed out until item selected then can do that
//! edit item options will include option to add item within item (making parent item container class)
//! key for visualization colors