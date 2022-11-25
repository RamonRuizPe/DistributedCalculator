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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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

    final Timer checkConnTimer = new Timer();

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
                System.out.println("Running server in "+actualPort);
                break;
            }catch (Exception e){
                actualPort++;
            }

        }
    }

    static void new_connection_handler(){
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
                    //Update new node information table
                    text.println(sent);

                    socket.close();
                    break;
                }catch(Exception e){
                    if (i == (maxConnectionAttempts - 1)){
                        System.out.println("Not available port");
                    }
                }
            }
            break;
        }
    }

    static void send_message_toPort(String message){
        try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)){
            System.out.println("Sending messages " + message);
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeApp(){
        try {
            serverSocket.close();
            checkConnTimer.cancel();
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
        File portDir = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\microservices\\"+actualPort);
        if(!(portDir.exists())){
            portDir.mkdir();
        }
        portDir.deleteOnExit();
        cellIdentifier = UUID.randomUUID();
        lblPort.setText(lblPort.getText()+" "+ actualPort);
        lblUUID.setText(lblUUID.getText()+" "+ cellIdentifier);
        if (!thread.isAlive()){
            thread.start();
        }
    }

    Thread thread = new Thread(new Runnable() {

        final TimerTask checkConn = new TimerTask() {
            @Override
            public void run() {
                try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)){
                    System.out.println("Verifying connection");
                } catch (IOException e) {
                    new_connection_handler();
                }
            }
        };


        @Override
        public void run() {
            String server_answer;
            String operationRequested = "";
            int maxNeeded = 0;
            int tries = 0;
            boolean duplicating = true;
            checkConnTimer.schedule(checkConn, 3000, 5000);
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
                    Platform.runLater(()->txtAreaHist.appendText(finalServer_answer + "\n"));
                    //System.out.println(server_answer);
                    String[] messageItems = server_answer.split(",");

                    //Inyecting service
                    if (messageItems.length == 3 && messageItems[1].equals("manager")){
                        String inyectService = messageItems[1];

                        Path pathSource = Path.of(messageItems[2]);
                        Path pathTarget = (Path) Paths.get("/Users",
                                "benra", "Documents", "7mo_semestre","calculadora","microservices", String.valueOf(actualPort), messageItems[2].split("\\\\")[7]);
                        Files.copy(pathSource,pathTarget, StandardCopyOption.REPLACE_EXISTING);

                        //The structure of path is C:\Users\benra\Documents\7mo_semestre\calculadora\manager\<service to inyect>
                        Platform.runLater(()->txtAreaHist.appendText(inyectService+" inyecting service " + messageItems[2].split("\\\\")[7]));
                        continue;
                    } else if (messageItems.length == 6 && messageItems[1].equals("manager")) {
                        continue;
                    }
                    message += messageItems[1] + "," + messageItems[2] + ",";


                    //duplicate request
                    //Cheking if UUID belongs to the server
                    if (messageItems[1].equals(String.valueOf(cellIdentifier))) {
                        if(Integer.parseInt(messageItems[2]) != maxNeeded){
                            maxNeeded = Integer.parseInt(messageItems[2]);
                            System.out.println(maxNeeded);
                        }
                        //serverSocket.close();
                        System.out.println("Try");
                        if (duplicating) {
                            duplicating = false;
                            int tempPort = actualPort + 1;
                            while(true){
                                try(ServerSocket tempSocket = new ServerSocket(tempPort)){
                                    new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\microservices\\"+tempPort).mkdir();
                                    File dir = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\microservices\\"+actualPort);
                                    File[] dirList = dir.listFiles();
                                    if(dirList != null){
                                        for(File microservices : dirList){
                                            Path pathTarget = (Path) Paths.get("/Users",
                                                    "benra", "Documents", "7mo_semestre","calculadora","microservices", String.valueOf(tempPort), microservices.getName());
                                            Path pathSource = (Path) Paths.get("/Users",
                                                    "benra", "Documents", "7mo_semestre","calculadora","microservices", String.valueOf(actualPort), microservices.getName());
                                            try{
                                                System.out.println(
                                                        "Number of bytes copied: "
                                                                + Files.copy(pathSource, pathTarget, StandardCopyOption.REPLACE_EXISTING));
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                    break;
                                }catch (Exception e){
                                    tempPort++;
                                }
                            }
                            ProcessBuilder pb = new ProcessBuilder("C:\\Users\\benra\\Desktop\\runServerCell.bat");
                            try{

                                Process p = pb.start();
                                //p.waitFor();

                                //serverSocket = new ServerSocket(actualPort);
                                System.out.println("");
                                if (tries < maxNeeded){
                                    duplicating = true;
                                    tries++;
                                }


                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } else {
                            continue;
                        }
                    }
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
                        Platform.runLater(()->txtAreaHist.appendText("Loading "+ finalOperationRequested +".jar"+"\n"));
                        //System.out.println("Loading "+operationRequested+".jar");
                        File file = new File("C:\\Users\\benra\\Documents\\7mo_semestre\\calculadora\\microservices\\"+actualPort+"\\"+operationRequested+".jar");
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