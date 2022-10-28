package com.example.nodegui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Node");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
        ((HelloController)fxmlLoader.getController()).init(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}