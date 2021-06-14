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
    private int mafiaNumber = 0;
    private int citizenNumber = 0;
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

            Thread.sleep(5000);
            while (true) {
                //DAY.........................................................................................
                dayVoteNight = "day";
                serverMassages("now its day and you can chat for 5 minutes...");

                chatRoom();

                //voting time.................................................................................

                serverMassages("we passed the day you should vote in 5 minutes...");
                Thread.sleep(5000);
                dayVoteNight = "vote";
                votingMassage();
                voteRoom();
                showVotes();
                Thread.sleep(5000);
                kickPlayer();
                resetVoteStatus();
                Thread.sleep(5000);

                //night time...................................................................................

                dayVoteNight = "night";
                nightMode();
                Thread.sleep(5000);

                //after night..................................................................................
                tellingStatusOfNight();
                Thread.sleep(10000);

            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    //getters...........................................................................................

    public String getDayVoteNight() {

        return dayVoteNight;
    }

    public String getFileName() {

        return fileName;
    }

    public ArrayList<Role> getRoles() {

        return roles;
    }

    //before starting game methods.......................................................................

    public void addUserName(String userName) {

        userNames.add(userName);
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
        if (roles.get(0) instanceof godFather ||
                roles.get(0) instanceof Lecter ||
                roles.get(0) instanceof normalMafia) {
            mafiaNumber++;
        } else {
            citizenNumber++;
        }
        return roles.get(0);
    }

    public boolean isReady() {
        for (Handler handler : handlers) {
            if (!handler.isReady()) {
                return false;
            }
        }
        return true;
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

    //massaging methods ..................................................................................

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

    //chatroom methods....................................................................................

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

    public void removeClientHandler(Handler handler) {
        if (handler.getPlayerRole() instanceof normalMafia ||
                handler.getPlayerRole() instanceof Lecter ||
                handler.getPlayerRole() instanceof godFather) {
            mafiaNumber--;
        } else {
            citizenNumber--;
        }
        handlers.remove(handler);
    }

    //voting time methods..................................................................................

    public void votingMassage() throws IOException {
        StringBuilder voteNames = new StringBuilder();
        for (Handler handler : handlers) {
            if (handler.isHeAlive()) {
                voteNames.append(handler.getHandlerName()).append("\n");
            }
        }
        serverMassages("who do you want to be kicked???");
        serverMassages(voteNames.toString());
    }

    public void voteToKick(Handler theHandler, String name) throws IOException {
        if (name.equals("none")) {
            serverMassages(theHandler.getHandlerName() + " voted" +
                    " to no one...");
            theHandler.setCanVote(false);
        } else {
            for (Handler handler : handlers) {
                if (handler.getHandlerName().equals(name)) {
                    handler.increaseVoteNum();
                    serverMassages(theHandler.getHandlerName() +
                            " voted to " + handler.getHandlerName());
                    theHandler.setCanVote(false);
                }
            }
        }
    }

    public void showVotes() throws IOException, InterruptedException {
        String votes = "";
        for (Handler handler : handlers) {
            if (handler.isHeAlive()) {
                votes += handler.getHandlerName() + ": " + handler.getVoteNum() + "\n";
            }
        }
        serverMassages("now you can see the vote chart!!!\n");
        Thread.sleep(4000);
        serverMassages(votes);
    }

    public void resetVoteStatus() {
        for (Handler handler : handlers) {
            handler.setCanVote(true);
            handler.setVoteNum(0);
        }
    }

    public void kickPlayer() throws IOException {
        int playerNum = (mafiaNumber + citizenNumber) / 2;
        for (Handler handler : handlers) {
            if (handler.getVoteNum() >= playerNum) {
                handler.setAlive(false);
                if (handler.getPlayerRole() instanceof normalMafia ||
                        handler.getPlayerRole() instanceof Lecter ||
                        handler.getPlayerRole() instanceof godFather) {
                    mafiaNumber--;
                } else {
                    citizenNumber--;
                }
                serverMassages(handler.getHandlerName() + " got kicked ");
            }
        }
    }

    //night mode methods ...................................................................................

    public void massageToMafia() throws IOException {
        for (Handler handler : handlers) {
            if ((handler.getPlayerRole() instanceof godFather && handler.isHeAlive()) ||
                    (handler.getPlayerRole() instanceof Lecter && handler.isHeAlive()) ||
                    (handler.getPlayerRole() instanceof normalMafia && handler.isHeAlive())) {
                handler.write("\nplease choose someone to kill :)");
            }
        }
        serverMassages("mafia are choosing some one to fuck him up");
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
        serverMassages("they made they're choice...\n");
    }

    public void killPlayer(String name) throws IOException {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name)) {
                handler.increaseKillVoteNum();
            }
        }
    }

    public void killConfiguration() throws IOException {
        if (mafiaNumber == 1) {
            for (Handler handler : handlers) {
                if (handler.getVoteToKill() == 1) {
                    handler.getPlayerRole().decreaseHealth();
                }
            }
        } else if (mafiaNumber > 1) {
            for (Handler handler : handlers) {
                if (handler.getVoteToKill() > 1) {
                    handler.getPlayerRole().decreaseHealth();
                }
            }
        }
    }

    public void massageToDoctor() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof townDoctor && handler.isHeAlive()) {
                handler.write("please choose some one to heal him :D");
            }
        }
        serverMassages("\ndoctor is choosing some one to heal");
        long time = System.currentTimeMillis();
        long time2 = time + 30000;
        while (System.currentTimeMillis() < time2) {

        }
        serverMassages("doctor made his choice...\n");
    }

    public void heal(String name) {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name)) {
                if (handler.getPlayerRole().getHealthBar() == 0) {
                    handler.getPlayerRole().increaseHealth();
                }
            }
        }
    }

    public void massageToDetective() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof detective && handler.isHeAlive()) {
                handler.write("hey man who do you wanna know that is mafia or not? ask me :D");
            }
        }
        serverMassages("\ndetective is choosing some one to ask if he is mafia or not!!!");
        long time = System.currentTimeMillis();
        long time2 = time + 30000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    public void isHeMafia(String name, Handler detective) throws IOException {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name)) {
                if (handler.getPlayerRole() instanceof normalMafia ||
                        handler.getPlayerRole() instanceof Lecter) {
                    detective.write("Yes " + handler.getHandlerName() + " is a mafia :)");
                } else {
                    detective.write("No " + handler.getHandlerName() + " is not a mafia :)");
                }
            }
        }
    }

    //after night...........................................................................................
    public void tellingStatusOfNight() throws IOException {

        for (Handler handler : handlers) {
            if (handler.getPlayerRole().getHealthBar() == 0) {

                serverMassages(handler.getHandlerName() + " got killed in the night!!!!\n");
                handler.setAlive(false);
                if (handler.getPlayerRole() instanceof godFather ||
                        handler.getPlayerRole() instanceof Lecter ||
                        handler.getPlayerRole() instanceof normalMafia) {
                    mafiaNumber--;
                } else {
                    citizenNumber--;
                }
            }
        }
    }

    //the game mod methods..................................................................................

    public void chatRoom() {
        long time = System.currentTimeMillis();
        long time2 = time + 30000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    public void voteRoom() {
        long time = System.currentTimeMillis();
        long time2 = time + 30000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    public void nightMode() throws IOException, InterruptedException {
        serverMassages("we have reached to night...please wait for server massage....");
        Thread.sleep(2000);
        massageToMafia();
        killConfiguration();
        Thread.sleep(2000);
        serverMassages("doctor wants to heal some one!!!");
        Thread.sleep(2000);
        massageToDoctor();
        Thread.sleep(2000);
        serverMassages("doctor played his role!!!");
        Thread.sleep(2000);
        serverMassages("now detective should ask for mafias!!!");
        Thread.sleep(2000);
        massageToDetective();
        Thread.sleep(1000);
        serverMassages("detective did the questioning!!!");
        long time = System.currentTimeMillis();
        long time2 = time + 1000;
        while (System.currentTimeMillis() < time2) {
        }
    }

    public static void main(String[] args) {

        Server server = new Server(8585);
        server.execute();
    }


}