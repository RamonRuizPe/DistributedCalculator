package com.example.nodegui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private Pane mainPane;

    @FXML private Label lblPort;
    @FXML public TextArea txtAreaHist;
    @FXML private Button btnClear;

    private double x, y;

    static List<Integer> cellPorts = new ArrayList<>();
    static List<Integer> nodePorts = new ArrayList<>();
    //static List<Integer> usedPorts = new ArrayList<>();
    static int actual_port = 4000;
    static ServerSocket serverSocket = null;


    static synchronized void broadcast(int senderPort, int nodePort, String message) throws IOException {
        String toSend = message.replaceFirst(message.split(",")[0],String.valueOf(nodePort));
        System.out.println(message);
        if (nodePorts.contains(senderPort)){
            System.out.println("Node communication");
            cellPorts.forEach((port) -> {
                if (senderPort != port){
                    try (Socket socket = new Socket("127.0.0.1", port)){
                        PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
                        p.println(toSend);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }else if (cellPorts.contains(senderPort)){
            System.out.println("Cell communication");
            cellPorts.forEach((port) -> {
                if (senderPort != port){
                    try (Socket socket = new Socket("127.0.0.1", port)){
                        PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
                        p.println(toSend);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            nodePorts.forEach((port) -> {
                if (senderPort != port){
                    try (Socket socket = new Socket("127.0.0.1", port)){
                        PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
                        p.println(toSend);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //operations with clientSockets
    static synchronized void remove_cells(Integer s){
        cellPorts.remove(s);
    }
    static synchronized void add_cells(Integer s){cellPorts.add(s);}

    static synchronized void remove_node(Integer s){nodePorts.remove(s);}
    static synchronized void add_node(Integer s){nodePorts.add(s);}
    static synchronized List<Integer> get_cells(){return cellPorts;}
    static synchronized List<Integer> get_nodes(){return nodePorts;}

    static void initial_connection_handler(){
        while(true){
            try {
                serverSocket = new ServerSocket(actual_port);
                nodePorts.forEach((temp_port) -> {
                    try{
                        Socket socket = new Socket("127.0.0.1", temp_port);
                        OutputStream out = socket.getOutputStream();
                        PrintWriter text = new PrintWriter(
                                new OutputStreamWriter(out, StandardCharsets.UTF_8), true
                        );
                        final String sent = "Node" + "," + actual_port;
                        text.println(sent);
                        socket.close();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                });
                break;
            }catch (Exception e){
                nodePorts.add(actual_port);
                actual_port++;
            }

        }
    }

    @FXML
    protected void onBtnClick() {
        txtAreaHist.setText("");
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
        lblPort.setText(lblPort.getText()+" "+actual_port);
        if (!thread.isAlive()){
            thread.start();
        }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try{
                    Socket client = serverSocket.accept();
                    System.out.println("Connection at local port "+ client.getLocalPort());
                    new ClientHandler(client, txtAreaHist).start();
                    //clientSockets.add(client);
                    //nodeSockets.add(client);

                }catch (IOException e){
                    System.out.println(e);
                }

            }
        }
    });
}

class ClientHandler extends Thread{
    private final Socket toClient;
    private TextArea txtAreaHist;

    ClientHandler(Socket socket, TextArea txtAreaHist){
        toClient = socket;
        this.txtAreaHist = txtAreaHist;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
            String line = in.readLine();
            //Check if connection is node
            if (line.startsWith("Node")){
                HelloController.add_node(Integer.parseInt(line.split(",")[1]));
                Platform.runLater(()-> txtAreaHist.setText("Connected cells: "+HelloController.get_cells() + "\n" + "Connected nodes: "+HelloController.get_nodes() + "\n"));
                System.out.println("Connected cells: "+HelloController.get_cells());
                System.out.println("Connected nodes: "+HelloController.get_nodes());
            }
            else if (line.startsWith("Cell")){
                HelloController.add_cells(Integer.parseInt(line.split(",")[1]));
                Platform.runLater(()-> txtAreaHist.setText("Connected cells: "+HelloController.get_cells() + "\n" + "Connected nodes: "+HelloController.get_nodes() + "\n"));
                System.out.println("Connected cells: "+HelloController.get_cells());
                System.out.println("Connected nodes: "+HelloController.get_nodes());
            }else{
                int senderPort = Integer.parseInt(line.split(",")[0]);
                Platform.runLater(()->txtAreaHist.appendText("Communicating from port: "+senderPort + "\n" + "From cell: "+ line.split(",")[1] + "\n\n"));
                //System.out.println("Communicating from port "+senderPort);
                HelloController.broadcast(senderPort,toClient.getLocalPort(),line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
