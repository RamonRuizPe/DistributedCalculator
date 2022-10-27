import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Nodo {
    static List<Integer> cellPorts = new ArrayList<>();
    static List<Integer> nodePorts = new ArrayList<>();
    //static List<Integer> usedPorts = new ArrayList<>();
    static int actual_port = 4000;
    static ServerSocket serverSocket = null;
    static synchronized void broadcast(int senderPort, int nodePort, String message) throws IOException{
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

    public static void main(String[] args) throws IOException{
        //int port = Integer.parseInt(args[1]);
        //Initialize node connections
        initial_connection_handler();

        System.out.println("Running port in " + actual_port);
        while(true){
            try{
                Socket client = serverSocket.accept();
                System.out.println("Connection at local port "+ client.getLocalPort());
                new ClientHandler(client).start();
                //clientSockets.add(client);
                //nodeSockets.add(client);

            }catch (IOException e){
                System.out.println(e);
            }

        }
    }
}

class ClientHandler extends Thread{
    private BufferedReader in;
    private PrintWriter out;
    private Socket toClient;
    private String last_message = "";

    ClientHandler(Socket socket){
        toClient = socket;
    }

    public void run(){
        try{
            in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
            //String port = in.readLine().split("|")[0];

            out = new PrintWriter(toClient.getOutputStream(), true);
            //System.out.println("Connection is successful");
            //out.println("Welcome");
            String line = in.readLine();
            //Check if connection is node
            if (line.startsWith("Node")){
                Nodo.add_node(Integer.parseInt(line.split(",")[1]));
                System.out.println(line);
                System.out.println("Connected cells: "+Nodo.get_cells());
                System.out.println("Connected nodes: "+Nodo.get_nodes());
            }
            else if (line.startsWith("Cell")){
                Nodo.add_cells(Integer.parseInt(line.split(",")[1]));
                System.out.println(line);
                System.out.println("Connected cells: "+Nodo.get_cells());
                System.out.println("Connected nodes: "+Nodo.get_nodes());
            }else{
                int senderPort = Integer.parseInt(line.split(",")[0]);
                System.out.println("Communicating from port "+senderPort);
                Nodo.broadcast(senderPort,toClient.getLocalPort(),line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}


