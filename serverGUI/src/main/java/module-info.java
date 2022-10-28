module com.example.servergui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.servergui to javafx.fxml;
    exports com.example.servergui;
}