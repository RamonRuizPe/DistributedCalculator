import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;


public class Server1 {
    static int maxConnectionAttempts = 30;
    static ServerSocket serverSocket = null;
    static int actualPort = 4050;
    static int nodeConnectionPort;
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
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        //Open a ServerSocket and communicate with random node
        initial_connection_handler();
        float response = 0;
        String server_answer;
        UUID cellIdentifier = UUID.randomUUID();
        URL[] jarFileURL;
        URLClassLoader urlClassLoader;
        Class<?> mainClass;
        String operationRequested = "";
        
        while(true){

            try {
                Socket client = serverSocket.accept();
                System.out.println("Connection at local port "+ client.getLocalPort());

                send_message_toPort("");

                //reading inputStream from node
                InputStream input = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String message = String.valueOf(actualPort) + "," + cellIdentifier + ",";
                server_answer = reader.readLine();
                System.out.println(server_answer);
                String[] messageItems = server_answer.split(",");
                message += messageItems[1] + "," + messageItems[2] + ",";
                server_answer = server_answer.substring(server_answer.indexOf(messageItems[2])+ messageItems[2].length() + 1);
                if(server_answer.startsWith("1,")){
                    operationRequested = "sum";
                } else if (server_answer.startsWith("2,")) {
                    operationRequested = "sub";
                } else if (server_answer.startsWith("3,")) {
                    operationRequested = "mult";
                } else if (server_answer.startsWith("4,")) {
                    operationRequested = "div";
                }

                try{
                    System.out.println("Loading "+operationRequested+".jar");
                    jarFileURL = new URL[]{new URL("file:////Users/benra/Documents/7mo_semestre/microservices"+operationRequested+".jar")};
                    urlClassLoader = new URLClassLoader(jarFileURL);
                    mainClass = urlClassLoader.loadClass(operationRequested);
                    //Expected constructor: message[port, thisCellID, senderCellID, EventID], FirstOperand, SecondOperand, port
                    Constructor<?> constructor = mainClass.getConstructor(String.class, float.class, float.class, int.class);
                    constructor.newInstance(message,Float.parseFloat(server_answer.split(",")[1]),Float.parseFloat( server_answer.split(",")[2]), nodeConnectionPort);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }

}