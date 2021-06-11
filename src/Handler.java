import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Handler extends Thread {
    private String name;
    private Socket socket;
    private Role playerRole;
    private Server server;
    private boolean isReady;
    private boolean isMafia;
    private ArrayList<Handler> handlers;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;


    public Handler(Socket socket, ArrayList<Handler> handlers, Server server) throws IOException {
        this.socket = socket;
        this.handlers = handlers;
        this.server = server;
        this.isReady = false;
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());

    }

    public void run() {
        try {
            String text;
            dataOutputStream.writeUTF("hello  please enter your name...");
            String str = dataInputStream.readUTF();
            while (server.nameIsThere(str, dataOutputStream)) {
                str = dataInputStream.readUTF();
            }
            name = str;
            Role role = server.sendingRole(dataOutputStream);
            playerRole = role;
            server.getRoles().remove(0);
            while (!(text = dataInputStream.readUTF()).equalsIgnoreCase("READY")) ;
            isReady = true;


            while (true) {
                text = dataInputStream.readUTF();
                if (text.equals("HISTORY")) {
                    load(server.getFileName());
                }
                else if(text.equals("EXIT")){
                    break;
                }
                server.sendAll(name + ": " + text);
            }

            server.removeClientHandler(this);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public Role getPlayerRole() {
        return playerRole;
    }


    public String getHandlerName() {
        return name;
    }

    public boolean isMafia() {
        return isMafia;
    }

    public void write(String string) throws IOException {
        dataOutputStream.writeUTF(string);
    }

    public void load(String file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String mystring;
            while ((mystring = in.readLine()) != null) {
                dataOutputStream.writeUTF(mystring);
            }
        } catch (IOException e) {
            System.out.println("Exception Occurred" + e);
        }
    }
}
