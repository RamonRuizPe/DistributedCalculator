package com.example.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HelloController {
    @FXML private Pane mainPane;
    @FXML private ListView<String> lvOperations;
    @FXML private ListView<String> lvServers;
    @FXML private Button btnSend, btnUpdate;
    File dirManager = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\manager");
    File dirServer = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\microservices");
    File[] dirList;
    ObservableList<String> operations;
    ObservableList<String> servers;
    private double x, y;
    static int actualPort = 4100;
    static int maxConnectionAttempts = 30;
    static ServerSocket serverSocket = null;
    static int nodeConnectionPort;

    static void initial_connection_handler(){
        while(true){
            for (int i = 0; i < maxConnectionAttempts; i++) {
                int randomPort = new Random().ints(1,4000, 4005).findFirst().getAsInt();
                try{
                    Socket socket = new Socket("127.0.0.1", randomPort);
                    //Remember the node port
                    nodeConnectionPort = randomPort;
                    System.out.println("Connected to port " +nodeConnectionPort);
                    OutputStream out = socket.getOutputStream();
                    PrintWriter text = new PrintWriter(
                            new OutputStreamWriter(out, StandardCharsets.UTF_8), true
                    );
                    final String sent = "Cell" + "," + actualPort;
                    text.println(sent);
                    socket.close();
                    break;
                }catch(Exception e){
                    if (i == (maxConnectionAttempts - 1)){
                        System.out.println("Not available port");
                    }
                }
            }
            //System.out.println("Running server in "+actualPort);
            break;
        }
    }

    static void send_message_toPort(String message){
        try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)){
            System.out.println("Sending messages " + message);
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            initial_connection_handler();
            try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)) {
                System.out.println("Sending messages " + message);
                PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
                p.println(message);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void getOperations() {
        dirList = dirManager.listFiles();
        List<String> currentOperations = new ArrayList<>();
        if(dirList != null){
            for (File microservices : dirList){
                currentOperations.add(microservices.getName());
            }
            operations = FXCollections.observableArrayList(currentOperations);
        }
    }

    public void getServers() {
        dirList = dirServer.listFiles();
        List<String> currentOperations = new ArrayList<>();
        if(dirList != null){
            for (File servers : dirList){
                currentOperations.add(servers.getName());
            }
            servers = FXCollections.observableArrayList(currentOperations);
        }
    }

    public void init(Stage stage){
        mainPane.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });
        mainPane.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX()-x);
            stage.setY(mouseEvent.getScreenY()-y);
        });
        initial_connection_handler();
        //Getting the current existing operations and servers
        getOperations();
        getServers();
        //Displaying it
        lvOperations.setItems(operations);
        lvServers.setItems(servers);
        lvServers.setSelectionModel(new NoSelectionModel<String>());
        lvServers.setFocusTraversable(false);
    }

    @FXML protected void onBtnSendClick(){
        String operation = lvOperations.getSelectionModel().getSelectedItem();
        String path = String.valueOf(dirManager);
        String message = actualPort + ",manager," + path + "\\" + operation;
        send_message_toPort(message);

        Dialog dialog = new Dialog();

        Label send = new Label(operation + " fue enviado correctamente");
        VBox content = new VBox(send);

        DialogPane pane = new DialogPane();
        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.setDialogPane(pane);

        dialog.showAndWait();
    }

    @FXML protected void onBtnUpdateClick(){
        //Refreshing information
        getOperations();
        getServers();
        //Displaying it
        lvOperations.setItems(operations);
        lvServers.setItems(servers);
        lvServers.setSelectionModel(new NoSelectionModel<String>());
        lvServers.setFocusTraversable(false);
    }

    public static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
        //This class allows the server list to be non-clickable or editable
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int index, int... indices) {
        }

        @Override
        public void selectAll() {
        }

        @Override
        public void selectFirst() {
        }

        @Override
        public void selectLast() {
        }

        @Override
        public void clearAndSelect(int index) {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void select(T obj) {
        }

        @Override
        public void clearSelection(int index) {
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {
        }

        @Override
        public void selectNext() {
        }
    }
}