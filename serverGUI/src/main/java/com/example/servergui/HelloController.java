package com.example.servergui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class HelloController {
    @FXML private Pane mainPane;

    @FXML private Label lblPort;
    @FXML public TextArea txtAreaHist;
    @FXML public Label lblUUID;

    private double x, y;

    static int actualPort = 4050;
    static int maxConnectionAttempts = 30;
    static ServerSocket serverSocket = null;
    static int nodeConnectionPort;
    static UUID cellIdentifier = null;

    @FXML
    protected void onBtnClick() {
        txtAreaHist.setText("");
    }

    static void initial_connection_handler(){
        while(true){
            try {
                serverSocket = new ServerSocket(actualPort);
                for (int i = 0; i < maxConnectionAttempts; i++) {
                    int randomPort = new Random().ints(1,4000, 4005).findFirst().getAsInt();
                    try{
                        Socket socket = new Socket("127.0.0.1", randomPort);
                        //Remember the node port
                        nodeConnectionPort = randomPort;
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
                System.out.println("Running port in "+actualPort);
                break;
            }catch (Exception e){
                actualPort++;
            }

        }
    }

    static void send_message_toPort(String message){
        try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)){
            System.out.println("Sending messages" + message);
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        cellIdentifier = UUID.randomUUID();
        lblPort.setText(lblPort.getText()+" "+ actualPort);
        lblUUID.setText(lblUUID.getText()+" "+ cellIdentifier);
        if (!thread.isAlive()){
            thread.start();
        }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            String server_answer;
            String operationRequested = "";

            while(true){
                try{
                    Socket client = serverSocket.accept();
                    System.out.println("Connection at local port "+ client.getLocalPort());

                    //send_message_toPort("");

                    //reading inputStream from node
                    InputStream input = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    String message = String.valueOf(actualPort) + "," + cellIdentifier + ",";
                    server_answer = reader.readLine();
                    String finalServer_answer = server_answer;
                    Platform.runLater(()->txtAreaHist.appendText(finalServer_answer));
                    //System.out.println(server_answer);
                    String[] messageItems = server_answer.split(",");
                    message += messageItems[1] + "," + messageItems[2] + ",";
                    server_answer = server_answer.substring(server_answer.indexOf(messageItems[2])+ messageItems[2].length() + 1);
                    if(server_answer.startsWith("1,")){
                        message+="5,";
                        operationRequested = "sum";
                    } else if (server_answer.startsWith("2,")) {
                        message+="6,";
                        operationRequested = "sub";
                    } else if (server_answer.startsWith("3,")) {
                        message+="7,";
                        operationRequested = "mult";
                    } else if (server_answer.startsWith("4,")) {
                        message+="8,";
                        operationRequested = "div";
                    }else{
                        continue;
                    }

                    try{
                        //SOA urls
                        URL[] jarFileURL;
                        URLClassLoader urlClassLoader;
                        Class<?> mainClass;
                        String finalOperationRequested = operationRequested;
                        Platform.runLater(()->txtAreaHist.appendText("\n"+"Loading "+ finalOperationRequested +".jar"));
                        //System.out.println("Loading "+operationRequested+".jar");
                        File file = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\microservices\\"+operationRequested+".jar");
                        mainClass = new URLClassLoader(new URL[] { file.toURI().toURL() }).loadClass(operationRequested);
                        Method method = mainClass.getMethods()[0];
                        Object objInstance = mainClass.getDeclaredConstructor().newInstance();
                        System.out.println(Float.parseFloat(server_answer.split(",")[1]));
                        System.out.println(Float.parseFloat(server_answer.split(",")[2]));
                        float result = (float)method.invoke(objInstance, Float.parseFloat(server_answer.split(",")[1]), Float.parseFloat( server_answer.split(",")[2]));
                        //System.out.println(result);
                        message+=String.valueOf(result);
                        send_message_toPort(message);
                        //urlClassLoader = new URLClassLoader(jarFileURL);
                        //System.out.println(urlClassLoader);
                        //mainClass = urlClassLoader.loadClass("sum");
                        //Method opMethod = mainClass.getMethod();
                        //Expected constructor: message[port, thisCellID, senderCellID, EventID], FirstOperand, SecondOperand, port
                        //Constructor<?> constructor = mainClass.getConstructor(String.class, float.class, float.class, int.class);
                        //constructor.newInstance(message,Float.parseFloat(server_answer.split(",")[1]),Float.parseFloat( server_answer.split(",")[2]), nodeConnectionPort);
                    } catch (Exception e) {
//                        throw new RuntimeException(e);
                        continue;
                    }


                }catch (IOException e){
                    System.out.println(e);
                }

            }
        }
    });
}