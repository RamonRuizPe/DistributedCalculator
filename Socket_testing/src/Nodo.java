import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Nodo {
    private static List<Socket> clientSockets = new ArrayList<Socket>();

    static synchronized void broadcast(Socket socketSender, String message) throws IOException{
        Socket socket;
        PrintWriter p;
        for (int i = 0; i < clientSockets.size(); i++){
            socket = clientSockets.get(i);
                if (socket != socketSender){
                    p = new PrintWriter(socket.getOutputStream(), true);
                    p.println(message);
                }
        }
    }

    static synchronized void remove(Socket s){
        clientSockets.remove(s);
    }

    public static void main(String[] args) throws IOException{
        int port = Integer.parseInt(args[1]);
        ServerSocket listener = new ServerSocket(port);
        System.out.println("Running port in " + port);
        while(true){
            Socket client = listener.accept();
            new ClientHandler(client, port).start();
            System.out.println("Connection at port "+ client.getPort());
            clientSockets.add(client);
        }
    }
}

class ClientHandler extends Thread{
    private BufferedReader in;
    private PrintWriter out;
    private Socket toClient;
    private int port;
    private String last_message = "";
    ClientHandler(Socket socket, int port){
        toClient = socket;
        this.port = port;
    }

    public void run(){
        try{
            in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
            //String port = in.readLine().split("|")[0];

            out = new PrintWriter(toClient.getOutputStream(), true);
            out.println("Connection is successful");
            while (true){
                String line = in.readLine();
                System.out.println(line);
                if (!line.equals(last_message)){
                    if(line.equals("end")){
                        Nodo.broadcast(toClient,"Disconnected");
                        break;
                    }
                    last_message = line;
                    Nodo.broadcast(toClient,line);
                }

            }
            Nodo.remove(toClient);
            toClient.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}


