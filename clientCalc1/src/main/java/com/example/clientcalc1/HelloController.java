package com.example.clientcalc1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class HelloController {
    @FXML private Pane titlePane;
    @FXML private ImageView btnHide, btnClose;

    @FXML private Label lblResult;

    private double x, y;
    private float num1 = 0;
    private String EqualClick = "start";
    private String operator = "";
    int node_connections = 3;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader input;

    String[] operators = {"+","-","*","/"};

    public void init(Stage stage){
        //Prevent pane from having wrong position in window.
        titlePane.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });
        titlePane.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX()-x);
            stage.setY(mouseEvent.getScreenY()-y);
        });

        //Set button to close window and hide it
        btnClose.setOnMouseClicked(mouseEvent -> stage.close());
        btnHide.setOnMouseClicked(mouseEvent -> stage.setIconified(true));
        try{
            socket = new Socket(HelloApplication.getDir(), HelloApplication.getPort());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void onNumClick(MouseEvent event){
        int value = Integer.parseInt(((Pane)event.getSource()).getId().replace("btn",""));
        lblResult.setText(lblResult.getText() + value);
    }

    @FXML
    void onOperClick(MouseEvent event){
        operator = ((Pane)event.getSource()).getId().replace("btn","");
            switch(operator){
                case "Sum":  lblResult.setText(lblResult.getText()+"+"); break;
                case "Minus":  lblResult.setText(lblResult.getText()+"-"); break;
                case "Mult":  lblResult.setText(lblResult.getText()+"*"); break;
                case "Div":  lblResult.setText(lblResult.getText()+"/"); break;
                case "Clear": lblResult.setText(""); break;
                case "Equals":
                    //Main thread to make inputs
                    String operation = lblResult.getText();
                    operation = lblResult.getText();
                    String output;
                    if (operation.contains(operators[0])) {
                        output = "1," + operation.replace(operators[0], ",");
                    } else if (operation.contains(operators[1])) {
                        output = "2," + operation.replace(operators[1], ",");
                    } else if (operation.contains(operators[2])) {
                        output = "3," + operation.replace(operators[2], ",");
                    } else if (operation.contains(operators[3])) {
                        output = "4," + operation.replace(operators[3], ",");
                    } else {
                        output = operation;
                    }
                    System.out.println(output);
                    out.println(output);

                    //Starting listener thread in order to receive response
                    thread.start();
            }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                String server_answer;
                String result="";
                try {
                    //message = lblResult.getText();
                    //Mandar mensaje
                    server_answer = input.readLine();
                    System.out.println(server_answer);
                    if (!server_answer.contains("|")) {
                        if (server_answer.startsWith("5,")) {
                            result = server_answer.split(",")[1];
                            System.out.println(result);
                        } else if (server_answer.startsWith("6,")) {
                            result = server_answer.split(",")[1];
                            System.out.println(result);
                        } else if (server_answer.startsWith("7,")) {
                            result = server_answer.split(",")[1];
                            System.out.println(result);
                        } else if (server_answer.startsWith("8,")) {
                            result = server_answer.split(",")[1];
                            System.out.println(result);
                        }
                        final String toLblResult = result;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                lblResult.setText(toLblResult);
                            }
                        });

                    }
                } catch(Exception e){
                    throw new RuntimeException("Continue");
                }

            }
        }

    });


}