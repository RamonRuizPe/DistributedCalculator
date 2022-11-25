package com.example.clientcalc1;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HelloController {
    @FXML private Pane titlePane;
    @FXML private ImageView btnHide, btnClose;

    @FXML private Label lblResult;
    @FXML private TextArea txtAreaHist;

    private double x, y;
    private float num1 = 0;
    private String EqualClick = "start";
    private String operator = "";
    int node_connections = 3;
    //private Socket socket;
    static int maxConnectionAttempts = 30;
    static private ServerSocket serverSocket = null;
    static int actualPort = 4050;
    static int nodeConnectionPort;

    //Values to be added to ServerSocket port in order to be event identifiers
    static int sumEventCounter = 1;
    static int subEventCounter = 1;
    static int multEventCounter = 1;
    static int divEventCounter = 1;
    String[] operators = {"+","-","*","/"};

    //Min receipts per operation
    static int minSum = 3;
    static int minSub = 2;
    static int minMult = 2;
    static int minDiv = 1;

    //indexes of the operations to be sent
    static int idxSum = 1;
    static int idxSub = 1;
    static int idxMult = 1;
    static int idxDiv = 1;

    static boolean isReceivingSum = false;
    static boolean isReceivingSub = false;
    static boolean isReceivingMult = false;
    static boolean isReceivingDiv = false;

    //Save information about events and receipts
    Hashtable<String, ArrayList<UUID>> sumOperations = new Hashtable<>();
    Hashtable<String, ArrayList<UUID>> subOperations = new Hashtable<>();
    Hashtable<String, ArrayList<UUID>> multOperations = new Hashtable<>();
    Hashtable<String, ArrayList<UUID>> divOperations = new Hashtable<>();

    Queue<String> sumQueue = new LinkedList<>();
    Queue<String> subQueue = new LinkedList<>();
    Queue<String> multQueue = new LinkedList<>();
    Queue<String> divQueue = new LinkedList<>();
    static UUID cellIdentifier = null;


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

    static void new_connection_handler(String message){
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

                    //Send the message that was meant to be sent
                    text.println(message);
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
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            //Making new attempt
            new_connection_handler(message);
        }
    }

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
        //Initialize connection with random node
        initial_connection_handler();
        cellIdentifier = UUID.randomUUID();
        System.out.println(cellIdentifier);
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
                    String operation = "";
                    operation = lblResult.getText();
                    String output = String.valueOf(actualPort) + "," + cellIdentifier + ",";
                    String EventID;
                    if (operation.contains(operators[0])) {
                        EventID = String.valueOf((actualPort * 10) + sumEventCounter);
                        output += EventID +",1," + operation.replace(operators[0], ",");

                        if (sumEventCounter == idxSum){
                            sumOperations.put(EventID,new ArrayList<UUID>());
                            sumQueue.add(output);
                            send_message_toPort(output);
                        }else{
                            if (sumOperations.get(String.valueOf((actualPort * 10) + idxSum)).size() < minSum){
                                sumOperations.put(EventID,new ArrayList<UUID>());
                                sumQueue.add(output);
                                send_message_toPort(sumQueue.peek());
                            }else{
                                idxSum++;
                            }
                        }
                        sumEventCounter++;
                    } else if (operation.contains(operators[1])) {
                        EventID = String.valueOf((actualPort * 10) + subEventCounter);
                        output += EventID + ",2," + operation.replace(operators[1], ",");

                        if (subEventCounter == idxSub){
                            subOperations.put(EventID,new ArrayList<UUID>());
                            subQueue.add(output);
                            send_message_toPort(output);
                        }else{
                            if (subOperations.get(String.valueOf((actualPort * 10) + idxSub)).size() < minSub){
                                subOperations.put(EventID,new ArrayList<UUID>());
                                subQueue.add(output);
                                //send_message_toPort(subQueue.peek());
                            }else{
                             idxSub++;
                            }
                        }
                        subEventCounter++;
                    } else if (operation.contains(operators[2])) {
                        EventID = String.valueOf((actualPort * 10) + multEventCounter);
                        output += EventID + ",3," + operation.replace(operators[2], ",");
                        System.out.println(multEventCounter + " " + idxMult);
                        if (multEventCounter == idxMult){
                            multOperations.put(EventID,new ArrayList<UUID>());
                            multQueue.add(output);
                            System.out.println(multOperations);
                            send_message_toPort(output);
                        }else{
                            if (multOperations.get(String.valueOf((actualPort * 10) + idxMult)).size() < minMult){
                                multOperations.put(EventID,new ArrayList<UUID>());
                                multQueue.add(output);
                                //send_message_toPort(multQueue.peek());
                            }else{
                                idxMult++;
                            }
                        }
                        multEventCounter++;
                    } else if (operation.contains(operators[3])) {
                        EventID = String.valueOf((actualPort * 10) + divEventCounter);
                        output += EventID + ",4," + operation.replace(operators[3], ",");

                        if (divEventCounter == idxDiv){
                            divOperations.put(EventID,new ArrayList<UUID>());
                            divQueue.add(output);
                            send_message_toPort(output);
                        }else{
                            if (divOperations.get(String.valueOf((actualPort * 10) + idxDiv)).size() < minDiv){
                                divOperations.put(EventID,new ArrayList<UUID>());
                                divQueue.add(output);
                                send_message_toPort(divQueue.peek());
                            }else{
                                idxDiv++;
                            }
                        }
                        divEventCounter++;
                    } else {
                        output = operation;
                    }
                    output = output;
                    System.out.println(output);
                    txtAreaHist.appendText(output+"\n");
                    //Opening a socket to send operation


                    //Starting listener thread in order to receive response
                    if(!thread.isAlive()){
                        thread.start();
                    }
            }
    }

    Thread thread = new Thread(new Runnable() {
        String sumDuplicate;
        String subDuplicate;
        String multDuplicate;
        String divDuplicate;
        int sumDupMax = 1;
        int sumTries = 0;
        int subDupMax = 1;
        int subTries = 0;
        int multDupMax = 1;
        int multTries = 0;
        int divDupMax = 1;
        int divTries = 0;

        final TimerTask sumSendMessage = new TimerTask() {
            @Override
            public void run() {
                //System.out.println("Sending");
                send_message_toPort(sumQueue.peek());
                if(sumTries > 3){
                    String outputDuplicate = actualPort +","+ sumDuplicate + "," + sumDupMax + ",11";
                    send_message_toPort(outputDuplicate);
                }else{
                    sumTries++;
                }

            }
        };
        final TimerTask subSendMessage = new TimerTask() {
            @Override
            public void run() {
                send_message_toPort(subQueue.peek());
                if(subTries > 3){
                    String outputDuplicate = actualPort +","+ subDuplicate + "," + subDupMax + ",11";
                    send_message_toPort(outputDuplicate);
                }else{
                    subTries++;
                }

            }
        };
        final TimerTask multSendMessage = new TimerTask() {
            @Override
            public void run() {
                send_message_toPort(multQueue.peek());
                if(multTries > 3){
                    String outputDuplicate = actualPort +","+ multDuplicate + "," + multDupMax + ",11";
                    send_message_toPort(outputDuplicate);
                }else{
                    multTries++;
                }

            }
        };
        final TimerTask divSendMessage = new TimerTask() {
            @Override
            public void run() {
                send_message_toPort(divQueue.peek());
                if(divTries > 3){
                    String outputDuplicate = actualPort +","+ divDuplicate + "," + divDupMax + ",11";
                    send_message_toPort(outputDuplicate);
                }else{
                    divTries++;
                }

            }
        };
        Timer sumTimer = new Timer();
        Timer subTimer = new Timer();
        Timer multTimer = new Timer();
        Timer divTimer = new Timer();

        @Override
        public void run() {
            while(true){
                String server_answer;
                String result="";

                try {
                    Socket nodeResponse = serverSocket.accept();
                    System.out.println("Connection at local port "+serverSocket.getLocalPort());
                    //message = lblResult.getText();
                    //Reading response from Node
                    InputStream input = nodeResponse.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    server_answer = reader.readLine();
                    //System.out.println(server_answer);
                    String[] nodeItems = server_answer.split(",");
                    //System.out.println(Arrays.toString(nodeItems));
                    if(server_answer.split(",")[1].equals("manager") && nodeItems.length == 6){
                        minSum = Integer.parseInt(nodeItems[2]); //sumAck update
                        minSub = Integer.parseInt(nodeItems[3]); //subAck update
                        minMult = Integer.parseInt(nodeItems[4]); //divAck update
                        minDiv = Integer.parseInt(nodeItems[5]); //multAck update
                        System.out.println(minSum + " " + minSub + " " + minMult + " " + minDiv);
                    } else if (!server_answer.split(",")[2].equals(String.valueOf(cellIdentifier))) {
                        continue;
                    }
                    server_answer = server_answer.replace(server_answer.split(",")[0]+",","");
                    server_answer = server_answer.substring(server_answer.indexOf(nodeItems[3])+nodeItems[3].length()+1);
                    System.out.println(server_answer);
                    if (!server_answer.contains("|")) {
                        if (server_answer.startsWith("5,")) {
                            //Receiving sum results from node
                            if (sumOperations.get(nodeItems[3]) != null){
                                //Checking if EventID exists in Hashtable
                                if(!sumOperations.get(nodeItems[3]).contains(UUID.fromString(nodeItems[1]))){
                                    //Checking if hashtable already has UUID from server response
                                    sumOperations.get(nodeItems[3]).add(UUID.fromString(nodeItems[1]));
                                    if(sumOperations.get(nodeItems[3]).size() < minSum){
                                        //Check how many receipts does this eventID has
                                        if (isReceivingSum){sumTimer = new Timer(); isReceivingSum=false;}
                                        sumDuplicate = String.valueOf(sumOperations.values().stream().findFirst().get().get(0));
                                        sumDupMax = minSum - sumOperations.get(nodeItems[3]).size();
                                        sumTimer.schedule(sumSendMessage,2000, 2500);
                                        continue;
                                    }else{
                                        Platform.runLater(() -> {
                                            txtAreaHist.appendText(String.valueOf(sumOperations));
                                            sumOperations.remove(nodeItems[3]);
                                            System.out.println(sumOperations);
                                        });
                                        //Cleaning the sending operation and restoring the queue.
                                        sumTimer.cancel();
                                        sumQueue.remove();
                                        idxSum++;
                                        sumTries++;
                                        result = server_answer.split(",")[1];
                                        System.out.println(result);
                                        isReceivingSum = true;
                                    }

                                }
                            }
                        } else if (server_answer.startsWith("6,")) {
                            if (subOperations.get(nodeItems[3]) != null){
                                if(!subOperations.get(nodeItems[3]).contains(UUID.fromString(nodeItems[1]))){
                                    subOperations.get(nodeItems[3]).add(UUID.fromString(nodeItems[1]));
                                    if(subOperations.get(nodeItems[3]).size() < minSub){
                                        if (isReceivingSub){subTimer = new Timer(); isReceivingSub=false;}
                                        subDuplicate = String.valueOf(subOperations.values().stream().findFirst().get().get(0));
                                        subDupMax = minSub - subOperations.get(nodeItems[3]).size();
                                        subTimer.schedule(subSendMessage,2000, 2500);
                                        continue;
                                    }else{
                                        Platform.runLater(() -> {
                                            txtAreaHist.appendText(subOperations + "\n");
                                            subOperations.remove(nodeItems[3]);
                                            System.out.println(subOperations);
                                        });
                                        //Cleaning the sending operation and restoring the queue.
                                        subTimer.cancel();
                                        subQueue.remove();
                                        subOperations.remove(nodeItems[3]);
                                        idxSub++;
                                        subTries++;
                                        result = server_answer.split(",")[1];
                                        System.out.println(result);
                                        isReceivingSub = true;
                                    }
                                }
                            }
                        } else if (server_answer.startsWith("7,")) {
                            if (multOperations.get(nodeItems[3]) != null){
                                if(!multOperations.get(nodeItems[3]).contains(UUID.fromString(nodeItems[1]))){
                                    multOperations.get(nodeItems[3]).add(UUID.fromString(nodeItems[1]));
                                    if(multOperations.get(nodeItems[3]).size() < minMult){
                                        if (isReceivingMult){multTimer = new Timer(); System.out.println("Initializing timer"); isReceivingMult=false;}
                                        multDuplicate = String.valueOf(multOperations.values().stream().findFirst().get().get(0));
                                        multDupMax = minMult - multOperations.get(nodeItems[3]).size();
                                        multTimer.schedule(multSendMessage,2000, 2500);
                                        continue;
                                    }else{
                                        Platform.runLater(() -> {
                                            txtAreaHist.appendText(multOperations + "\n");
                                            multOperations.remove(nodeItems[3]);
                                            System.out.println(multOperations);
                                        });
                                        //Cleaning the sending operation and restoring the queue.
                                        multTimer.cancel();
                                        System.out.println("Timer was cancelled");
                                        multQueue.remove();
                                        multOperations.remove(nodeItems[3]);
                                        idxMult++;
                                        multTries++;
                                        result = server_answer.split(",")[1];
                                        isReceivingMult = true;
                                    }
                                }
                            }
                        } else if (server_answer.startsWith("8,")) {
                            if (divOperations.get(nodeItems[3]) != null){
                                if(!divOperations.get(nodeItems[3]).contains(UUID.fromString(nodeItems[1]))){
                                    divOperations.get(nodeItems[3]).add(UUID.fromString(nodeItems[1]));
                                    if(divOperations.get(nodeItems[3]).size() < minDiv){
                                        if (isReceivingDiv){divTimer = new Timer(); System.out.println("Initializing timer"); isReceivingDiv=false;}
                                        divDuplicate = String.valueOf(divOperations.values().stream().findFirst().get().get(0));
                                        divDupMax = minDiv - divOperations.get(nodeItems[3]).size();
                                        divTimer.schedule(divSendMessage,2000, 2500);
                                        continue;
                                    }else{
                                        Platform.runLater(() -> {
                                            txtAreaHist.appendText(divOperations + "\n");
                                            divOperations.remove(nodeItems[3]);
                                            System.out.println(divOperations);
                                        });
                                        //Cleaning the sending operation and restoring the queue.
                                        divTimer.cancel();
                                        divQueue.remove();
                                        //divOperations.remove(nodeItems[3]);
                                        idxDiv++;
                                        divTries++;
                                        result = server_answer.split(",")[1];
                                        System.out.println(result);
                                        isReceivingDiv = true;
                                    }
                                }
                            }
                        }
                        final String toLblResult = result;
                        if(!result.isBlank()){
                            Platform.runLater(() -> {
                                lblResult.setText(toLblResult);
                                //txtAreaHist.appendText("Server UUID: "+nodeItems[1]+": "+toLblResult+"\n");
                            });
                        }



                    }
                } catch(Exception e){
                    //throw new RuntimeException("Continue");
                    e.printStackTrace();
                }

            }
        }

    });

}