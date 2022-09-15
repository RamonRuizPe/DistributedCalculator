import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server1 {

    public static void main(String[] args){
            try(Socket socket = new Socket(args[0],Integer.parseInt(args[1]))){
                String port = args[2];
                String address = args[0];
                OutputStream out = socket.getOutputStream();
                PrintWriter text_out = new PrintWriter(
                        new OutputStreamWriter(out, StandardCharsets.UTF_8), true
                );
                text_out.println(port+"|"+address);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String server_answer = reader.readLine();
                System.out.println(server_answer);
                float response = 0;
                while(true){
                    server_answer = reader.readLine();
                    if(server_answer.startsWith("1,")){
                        System.out.println(server_answer);
                        response = Float.parseFloat(server_answer.split(",")[1]) + Float.parseFloat( server_answer.split(",")[2]);
                        String message = "5,"+response;
                        text_out.println(message);
                    } else if (server_answer.startsWith("2,")) {
                        System.out.println(server_answer);
                        response = Float.parseFloat(server_answer.split(",")[1]) - Float.parseFloat( server_answer.split(",")[2]);
                        String message = "6,"+response;
                        text_out.println(message);
                    } else if (server_answer.startsWith("3,")) {
                        System.out.println(server_answer);
                        response = Float.parseFloat(server_answer.split(",")[1]) * Float.parseFloat( server_answer.split(",")[2]);
                        String message = "7,"+response;
                        text_out.println(message);
                    } else if (server_answer.startsWith("4,")) {
                        System.out.println(server_answer);
                        response = Float.parseFloat(server_answer.split(",")[1]) / Float.parseFloat( server_answer.split(",")[2]);
                        String message = "8,"+response;
                        text_out.println(message);
                    }



                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }

}
