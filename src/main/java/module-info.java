module cs420.cs420finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;


    opens cs420.cs420finalproject to javafx.fxml;
    exports cs420.cs420finalproject;
}