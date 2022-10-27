import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class mult {
    String sumCode = "7,";
    public mult(String message, float firstOperand, float secondOperand, int nodeConnectionPort){
        message += sumCode;
        message += String.valueOf(firstOperand*secondOperand);

        try(Socket socket = new Socket("127.0.0.1", nodeConnectionPort)){
            PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
            p.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }
}
