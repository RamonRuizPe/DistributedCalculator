module com.example.clientcalc1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.clientcalc1 to javafx.fxml;
    exports com.example.clientcalc1;
}