module com.example.nodegui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.nodegui to javafx.fxml;
    exports com.example.nodegui;
}