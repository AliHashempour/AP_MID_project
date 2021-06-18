import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Server.
 *
 * @author ALi.Hashempour
 */
public class Server {
    private int Port;
    private int mafiaNumber = 0;
    private int citizenNumber = 0;
    private boolean dieHardRequest;
    private boolean mayorPermission;
    private String dayVoteNight;
    private String fileName = "history.txt";
    private ArrayList<String> userNames = new ArrayList<>();
    private ArrayList<Handler> handlers = new ArrayList<>();
    private ArrayList<Role> roles = new ArrayList<>();
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private String ANSI_BLUE = "\u001B[34m";
    private String ANSI_RED = "\u001B[31m";
    private String ANSI_GREEN = "\u001B[32m";
    private String ANSI_PURPLE = "\u001B[35m";
    private String ANSI_RESET = "\u001B[0m";

    /**
     * Instantiates a new Server.
     *
     * @param serverPort the server port
     */
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

            while (!endGame()) {
                //DAY.........................................................................................
                dayVoteNight = "day";
                serverMassages(ANSI_BLUE + "now its day and you can chat for 5 minutes..." + ANSI_RESET);

                chatRoom();

                //voting time.................................................................................

                serverMassages(ANSI_BLUE + "we passed the day you should vote in 5 minutes..." + ANSI_RESET);
                resetCanSpeaks();
                Thread.sleep(5000);
                dayVoteNight = "vote";
                votingMassage();
                voteRoom();
                showVotes();
                Thread.sleep(5000);
                massageToMayor();
                if (mayorPermission) {
                    kickPlayer();
                } else {
                    serverMassages("mayor vetoed the voting!!!");
                }
                mayorPermission = true;
                resetVoteStatus();
                Thread.sleep(5000);

                //night time...................................................................................

                dayVoteNight = "night";
                nightMode();
                Thread.sleep(5000);

                //after night..................................................................................
                tellingStatusOfNight();
                Thread.sleep(2000);
                if (dieHardRequest) {
                    deadRolesStatus();
                }
                dieHardRequest = false;
                Thread.sleep(10000);
            }

            serverMassages(ANSI_BLUE + "i have to say that the game is finished:))))" + ANSI_RESET);
            Thread.sleep(2000);
            if (mafiaNumber == citizenNumber) {
                serverMassages(ANSI_PURPLE + "MAFIA TEAM WON!!!" + ANSI_RESET);
            } else if (mafiaNumber == 0) {
                serverMassages(ANSI_GREEN + "CITIZEN TEAM WON!!!" + ANSI_RESET);
            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    //getters...........................................................................................

    /**
     * Gets day vote night.
     *
     * @return the day vote night
     */
    public String getDayVoteNight() {

        return dayVoteNight;
    }

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {

        return fileName;
    }


    /**
     * Gets roles.
     *
     * @return the roles
     */
    public ArrayList<Role> getRoles() {

        return roles;
    }

    //before starting game methods.......................................................................

    /**
     * Add user name.
     *
     * @param userName the user name
     */
    public void addUserName(String userName) {

        userNames.add(userName);
    }

    /**
     * Name is there boolean.
     *
     * @param name             the name
     * @param dataOutputStream the data output stream
     * @return the boolean
     * @throws IOException the io exception
     */
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

    /**
     * Create role.
     */
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

    /**
     * Sending role role.
     *
     * @param dout the dout
     * @return the role
     * @throws IOException the io exception
     */
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

    /**
     * Is ready boolean.
     *
     * @return the boolean
     */
    public boolean isReady() {
        for (Handler handler : handlers) {
            if (!handler.isReady()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Introduction.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Server massages.
     *
     * @param string the string
     * @throws IOException the io exception
     */
    public void serverMassages(String string) throws IOException {
        for (Handler handler : handlers) {
            handler.write(string);
        }
    }

    /**
     * Send all.
     *
     * @param text the text
     * @throws IOException the io exception
     */
    public void sendAll(String text) throws IOException {
        if (dayVoteNight.equalsIgnoreCase("day")) {
            appendStrToFile(fileName, text + "\n");
            for (Handler handler : handlers) {
                handler.write(text);

            }
        }
    }

    //chatroom methods....................................................................................

    /**
     * Append str to file.
     *
     * @param fileName the file name
     * @param str      the str
     */
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

    /**
     * Remove client handler.
     *
     * @param handler the handler
     */
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

    /**
     * Reset can speaks.
     */
    public void resetCanSpeaks() {
        for (Handler handler : handlers) {
            if (handler.isHeAlive()) {
                handler.setCanSpeak(true);
            }
        }
    }

    /**
     * Voting massage.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Vote to kick.
     *
     * @param theHandler the the handler
     * @param name       the name
     * @throws IOException the io exception
     */
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

    /**
     * Show votes.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
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

    /**
     * Reset vote status.
     */
    public void resetVoteStatus() {
        for (Handler handler : handlers) {
            handler.setCanVote(true);
            handler.setVoteNum(0);
        }
    }

    /**
     * Massage to mayor.
     *
     * @throws IOException the io exception
     */
    public void massageToMayor() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof Mayor && handler.isHeAlive()) {
                handler.write("do you want to continue the voting????");
            }
        }
        long time = System.currentTimeMillis();
        long time2 = time + 30000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Mayor permission method.
     *
     * @param permission the permission
     */
    public void mayorPermissionMethod(String permission) {
        if (permission.equals("no")) {
            mayorPermission = false;
        } else {
            mayorPermission = true;
        }
    }

    /**
     * Kick player.
     *
     * @throws IOException the io exception
     */
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
                serverMassages(ANSI_RED + handler.getHandlerName() + " got kicked!!!" + ANSI_RESET);
            }
        }
    }

    //night mode methods ...................................................................................

    /**
     * Massage to mafia.
     *
     * @throws IOException the io exception
     */
    public void massageToMafia() throws IOException {
        for (Handler handler : handlers) {
            if ((handler.getPlayerRole() instanceof godFather && handler.isHeAlive()) ||
                    (handler.getPlayerRole() instanceof Lecter && handler.isHeAlive()) ||
                    (handler.getPlayerRole() instanceof normalMafia && handler.isHeAlive())) {
                handler.write("\n" + ANSI_RED + "please choose someone to kill :)" + ANSI_RESET);
            }
        }
        serverMassages("mafia are choosing some one to kill him!!");
        long time = System.currentTimeMillis();
        long time2 = time + 60000;
        while (System.currentTimeMillis() < time2) {

        }
        serverMassages("they made they're choice...\n");
    }

    /**
     * Kill player.
     *
     * @param name the name
     * @throws IOException the io exception
     */
    public void killPlayer(String name) throws IOException {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name)) {
                handler.increaseKillVoteNum();
            }
        }
    }

    /**
     * Kill configuration.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Massage to doctor.
     *
     * @throws IOException the io exception
     */
    public void massageToDoctor() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof townDoctor && handler.isHeAlive()) {
                handler.write(ANSI_GREEN + "please choose some one to heal him :D" + ANSI_RESET);
            }
        }
        serverMassages("\ndoctor is choosing some one to heal");
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
        serverMassages("doctor made his choice...\n");
    }

    /**
     * Heal.
     *
     * @param name the name
     */
    public void heal(String name) {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name)) {
                if (handler.getPlayerRole().getHealthBar() == 0) {
                    handler.getPlayerRole().increaseHealth();
                }
            }
        }
    }

    /**
     * Massage to detective.
     *
     * @throws IOException the io exception
     */
    public void massageToDetective() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof detective && handler.isHeAlive()) {
                handler.write(ANSI_GREEN + "hey man who do you wanna know that is mafia or not? ask me :D" + ANSI_RESET);
            }
        }
        serverMassages("\ndetective is choosing some one to ask if he is mafia or not!!!");
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Is he mafia.
     *
     * @param name      the name
     * @param detective the detective
     * @throws IOException the io exception
     */
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

    /**
     * Massage to sniper.
     *
     * @throws IOException the io exception
     */
    public void massageToSniper() throws IOException {
        serverMassages("\nsniper is choosing some one to kill !!!");

        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof Sniper && handler.isHeAlive()) {
                handler.write(ANSI_GREEN + " who do you wanna kill that you think he is a mafia???" + ANSI_RESET);
            }
        }
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Sniper shoot.
     *
     * @param mafiaName the mafia name
     * @param sniper    the sniper
     */
    public void sniperShoot(String mafiaName, Handler sniper) {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(mafiaName) && handler.isHeAlive()) {
                if (handler.getPlayerRole() instanceof normalMafia ||
                        handler.getPlayerRole() instanceof Lecter ||
                        handler.getPlayerRole() instanceof godFather) {
                    handler.getPlayerRole().decreaseHealth();
                } else {
                    sniper.getPlayerRole().decreaseHealth();
                }
            }
        }
    }

    /**
     * Massage to lecter.
     *
     * @throws IOException the io exception
     */
    public void massageToLecter() throws IOException {
        serverMassages("\nLecter is healing a mafia!!!");

        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof Lecter && handler.isHeAlive()) {
                handler.write(ANSI_PURPLE + "who do you wanna heal from mafia team???" + ANSI_RESET);
            }
        }
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Save mafia.
     *
     * @param mafiaName the mafia name
     */
    public void saveMafia(String mafiaName) {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(mafiaName) && handler.isHeAlive()) {
                if (handler.getPlayerRole() instanceof normalMafia ||
                        handler.getPlayerRole() instanceof Lecter ||
                        handler.getPlayerRole() instanceof godFather) {
                    if (handler.getPlayerRole().getHealthBar() == 0) {
                        handler.getPlayerRole().increaseHealth();
                    }
                }
            }
        }
    }

    /**
     * Massage to psychologist.
     *
     * @throws IOException the io exception
     */
    public void massageToPsychologist() throws IOException {
        serverMassages("\npsychologist is muting a person!!!");

        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof psychologist && handler.isHeAlive()) {
                handler.write("who do you want to shut him up???");
            }
        }
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Mute.
     *
     * @param name the name
     * @throws IOException the io exception
     */
    public void mute(String name) throws IOException {
        for (Handler handler : handlers) {
            if (handler.getHandlerName().equals(name) && handler.isHeAlive()) {
                handler.setCanSpeak(false);
                handler.write(ANSI_RED + "you are muted for next day mate:(" + ANSI_RESET);
            }
        }

    }

    /**
     * Massage to die hard.
     *
     * @throws IOException the io exception
     */
    public void massageToDieHard() throws IOException {
        serverMassages("\ndie hard is talking to the GOD !!!");

        for (Handler handler : handlers) {
            if (handler.getPlayerRole() instanceof dieHard && handler.isHeAlive()) {
                handler.write("do you want to show what roles are dead??? yes or no?");
            }
        }
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Killed roles info.
     *
     * @param yesNo the yes no
     */
    public void killedRolesInfo(String yesNo) {
        if (yesNo.equals("yes")) {
            dieHardRequest = true;
        } else {
            dieHardRequest = false;
        }
    }


    //after night...............................................................................................

    /**
     * Telling status of night.
     *
     * @throws IOException the io exception
     */
    public void tellingStatusOfNight() throws IOException {

        for (Handler handler : handlers) {
            if (handler.getPlayerRole().getHealthBar() == 0 && handler.isHeAlive()) {

                serverMassages(ANSI_RED + handler.getHandlerName() + " IS DEAD!!!!" + ANSI_RESET);
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

    /**
     * Dead roles status.
     *
     * @throws IOException the io exception
     */
    public void deadRolesStatus() throws IOException {
        for (Handler handler : handlers) {
            if (handler.getPlayerRole().getHealthBar() == 0) {
                serverMassages(ANSI_RED + handler.getPlayerRole() + " is dead!!!" + ANSI_RESET);
            }
        }
    }

    //the game mod methods..................................................................................

    /**
     * Chat room.
     */
    public void chatRoom() {
        long time = System.currentTimeMillis();
        long time2 = time + 50000;
        while (System.currentTimeMillis() < time2) {

        }
    }

    /**
     * Vote room.
     */
    public void voteRoom() {
        long time = System.currentTimeMillis();
        long time2 = time + 60000;
        while (System.currentTimeMillis() < time2) {

        }
    }


    /**
     * Night mode.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public void nightMode() throws IOException, InterruptedException {
        serverMassages(ANSI_BLUE + "we have reached to night...please wait for server massage...." + ANSI_RESET);
        Thread.sleep(2000);
        massageToMafia();
        killConfiguration();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "doctor wants to heal some one!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToDoctor();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "doctor played his role!!!" + ANSI_RESET);
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "now detective should ask for mafias!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToDetective();
        Thread.sleep(1000);
        serverMassages(ANSI_BLUE + "detective did the questioning!!!" + ANSI_RESET);
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "now its time for Sniper to kill a mafia!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToSniper();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "sniper shot at some body!!!" + ANSI_RESET);
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "it is lecter time to play his role!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToLecter();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "lecter played his turn!!!" + ANSI_RESET);
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "now its time for psychologist to mute some body!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToPsychologist();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "psychologist did his role!!!" + ANSI_RESET);
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "its die hard turn!!!" + ANSI_RESET);
        Thread.sleep(2000);
        massageToDieHard();
        Thread.sleep(2000);
        serverMassages(ANSI_BLUE + "die hard did his role!!!" + ANSI_RESET);
        long time = System.currentTimeMillis();
        long time2 = time + 1000;
        while (System.currentTimeMillis() < time2) {
        }
    }

    /**
     * End game boolean.
     *
     * @return the boolean
     */
    public boolean endGame() {
        return mafiaNumber == citizenNumber || mafiaNumber == 0;

    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        Server server = new Server(9945);
        server.execute();
    }


}