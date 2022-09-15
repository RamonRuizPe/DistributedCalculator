import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Calculator1 {

        private String ip;
        private int port;
        private ServerSocket ReceiveSocket;


        public Calculator1(String ip, int port) throws IOException{
            this.ip = ip;
            this.port = port;
        }

    public static void main(String[] args){
        boolean connection = true;
        String[] operators = {"+","-","*","/"};
        int node_connections = 3;
        if(connection){
            try(Socket socket = new Socket(args[0], Integer.parseInt(args[1]))){
                OutputStream out = socket.getOutputStream();
                PrintWriter text = new PrintWriter(
                        new OutputStreamWriter(out, StandardCharsets.UTF_8), true
                );
                String message;
                String output;
                String last_result = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                Scanner keyboard = new Scanner(System.in);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String server_answer = reader.readLine();
                System.out.println(server_answer);
                try{
                    do{

                        System.out.println("Enter operation: ");
                        message = br.readLine();

                        if (message.contains(operators[0])){
                            output = "1,"+message.replace(operators[0],",");
                        }
                        else if (message.contains(operators[1])){
                            output = "2,"+message.replace(operators[1],",");
                        }
                        else if (message.contains(operators[2])){
                            output = "3,"+message.replace(operators[2],",");
                        }
                        else if (message.contains(operators[3])){
                            output = "4,"+message.replace(operators[3],",");
                        }else{
                            output = message;
                        }

                        text.println(output);

                        int i = 0;
                        while(i < node_connections ){
                            server_answer = reader.readLine();
                            if(!server_answer.contains("|")){
                                if(server_answer.startsWith("5,")){
                                    if(last_result.equals(server_answer)){
                                        System.out.print("");
                                    }else{
                                        last_result = server_answer;
                                        System.out.println(server_answer.split(",")[1]);
                                    }
                                }else if(server_answer.startsWith("6,")){
                                    if(last_result.equals(server_answer)){
                                        System.out.print("");
                                    }else{
                                        last_result = server_answer;
                                        System.out.println(server_answer.split(",")[1]);
                                    }
                                }else if(server_answer.startsWith("7,")){
                                    if(last_result.equals(server_answer)){
                                        System.out.print("");
                                    }else{
                                        last_result = server_answer;
                                        System.out.println(server_answer.split(",")[1]);
                                    }
                                }else if(server_answer.startsWith("8,")){
                                    if(last_result.equals(server_answer)){
                                        System.out.print("");
                                    }else{
                                        last_result = server_answer;
                                        System.out.println(server_answer.split(",")[1]);
                                    }
                                }

                            }
                            i++;
                        }


                    }while(!message.equals("end"));
                }catch(IOException e){
                    e.printStackTrace();
                }


            }catch (IOException e){
                e.printStackTrace();
            }
        }else{

        }



    }

}
