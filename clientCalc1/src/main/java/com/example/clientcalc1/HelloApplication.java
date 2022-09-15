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
    private static int port;
    private static String dir;

    @Override
    public void start(Stage stage) throws IOException {
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
    }

    public static void main(String[] args) {
        port = Integer.parseInt(args[1]);
        dir = args[0];
        launch(args);
    }

    static int getPort(){
        return port;
    }

    static String getDir(){
        return dir;
    }
}