import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int Port;
    private String dayVoteNight;
    private String fileName = "history.txt";
    private ArrayList<String> userNames = new ArrayList<>();
    private ArrayList<Handler> handlers = new ArrayList<>();
    private ArrayList<Role> roles = new ArrayList<>();
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ExecutorService pool = Executors.newCachedThreadPool();

    public Server(int serverPort) {
        this.Port = serverPort;
        createRole();
    }


    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(Port)) {
            System.out.println("Chat Server is listening on port " + Port);
            long time = new Date().getTime();
            while (new Date().getTime() - time < 30000) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());


                Handler newHandler = new Handler(socket, handlers, this);

                handlers.add(newHandler);

                pool.execute(newHandler);

            }
            while (!(isReady())) ;
            introduction();

            while (true) {
                dayVoteNight = "day";
                serverMassages("you can chat for 5 minutes...");
                while (new Date().getTime() - time < 90000) {

                }
                serverMassages("you should vote in 5 minutes...");
                dayVoteNight = "vote";
                while (new Date().getTime() - time < 900000) {

                }
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public void createRole() {
        roles.add(new detective());
        roles.add(new dieHard());
        roles.add(new godFather());
        roles.add(new Lecter());
        roles.add(new Mayor());
        roles.add(new normalCitizen());
        roles.add(new normalMafia());
        roles.add(new psychologist());
        roles.add(new Sniper());
        roles.add(new townDoctor());
    }

    public Role sendingRole(DataOutputStream dout) throws IOException {
        Collections.shuffle(roles);
        dout.writeUTF("your role : " + roles.get(0).toString());
        return roles.get(0);
    }

    public void introduction() throws IOException {
        StringBuilder mafias = new StringBuilder();
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof godFather) {
                mafias.append("godfather : ").append(handler.getHandlerName()).append("\n");
            }
            if (handler.getPlayerRole() instanceof normalMafia) {
                mafias.append("normalMafia : ").append(handler.getHandlerName()).append("\n");
            }
            if (handler.getPlayerRole() instanceof Lecter) {
                mafias.append("lecter : ").append(handler.getHandlerName()).append("\n");
            }
        }
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof godFather ||
                    handler.getPlayerRole() instanceof Lecter ||
                    handler.getPlayerRole() instanceof normalMafia) {
                handler.write(mafias.toString());
            }
        }
        StringBuilder doc = new StringBuilder();
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof townDoctor) {
                doc.append("townDoctor : ").append(handler.getHandlerName());
            }
        }
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof Mayor) {
                handler.write(doc.toString());
            }
        }
        for (Handler handler : handlers) {
            handler.write("now we have passed the introduction night...");
        }
    }

    public void addUserName(String userName) {
        userNames.add(userName);
    }

    public boolean isReady() {
        for (Handler handler : handlers) {
            if (!handler.isReady()) {
                return false;
            }
        }
        return true;
    }

    public boolean nameIsThere(String name, DataOutputStream dataOutputStream) throws IOException {
        boolean bool = false;
        if (userNames.contains(name)) {
            dataOutputStream.writeUTF("name exists");
            bool = true;
        } else {
            addUserName(name);
            dataOutputStream.writeUTF("name added");
        }
        return bool;
    }

    public void removeUser(String userName, Handler aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            handlers.remove(aUser);
            System.out.println("The user " + userName + " quited");
        }
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

    public void serverMassages(String string) throws IOException {
        for (Handler handler : handlers) {
            handler.write(string);
        }
    }

    public void sendAll(String text) throws IOException {
        if (dayVoteNight.equalsIgnoreCase("day")) {
            appendStrToFile(fileName, text + "\n");
            for (Handler handler : handlers) {
                handler.write(text);
            }
        }
    }

    public void appendStrToFile(String fileName, String str) {
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public static void main(String[] args) {

        Server server = new Server(8585);
        server.execute();
    }


}