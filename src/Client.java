import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private String hostname;
    private int port;
    private String userName;
    private static Socket socket;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            socket = new Socket(hostname, port);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }


    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        Client client = new Client("127.0.0.1", 8585);
        client.execute();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        Thread thread = new ThreadReader(dataInputStream);
        thread.start();
        while (true) {
            String str = scanner.nextLine();
            dataOutputStream.writeUTF(str);
            if (str.equals("EXIT")) {
                System.exit(0);
            }
        }
    }
}
