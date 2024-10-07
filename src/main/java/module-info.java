module cs420.cs420finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens cs420.cs420finalproject to javafx.fxml;
    exports cs420.cs420finalproject;
}