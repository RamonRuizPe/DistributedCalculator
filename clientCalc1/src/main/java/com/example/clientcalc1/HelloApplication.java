package com.example.clientcalc1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Calculator");
            stage.setResizable(false);
            //stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../../../../../../../../Downloads/abacus.png"))));

            stage.show();
            ((HelloController)fxmlLoader.getController()).init(stage);
        }catch(IOException e){
            throw new RuntimeException("");
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}