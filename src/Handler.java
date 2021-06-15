import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The type Handler.
 */
public class Handler extends Thread {
    private String name;
    private int voteToKill = 0;
    private Socket socket;
    private int voteNum = 0;
    private Role playerRole;
    private Server server;
    private boolean isReady;
    private boolean isAlive;
    private boolean canVote;
    private boolean canSpeak;
    private ArrayList<Handler> handlers;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;


    public Handler(Socket socket, ArrayList<Handler> handlers, Server server) throws IOException {
        this.socket = socket;
        this.handlers = handlers;
        this.server = server;
        this.isReady = false;
        this.isAlive = true;
        this.canVote = true;
        this.canSpeak = true;
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
                //chatroom and day mode..........................................................................
                text = dataInputStream.readUTF();
                if (text.equals("HISTORY") && server.getDayVoteNight().equals("day")) {
                    load(server.getFileName());
                } else if (text.equals("EXIT")) {
                    break;
                }
                //voting........................................................................................
                if (server.getDayVoteNight().equals("vote") && this.canVote
                        && isAlive) {
                    server.voteToKick(this, text);
                }
                if (server.getDayVoteNight().equals("vote") && (role instanceof Mayor)
                        && isAlive) {
                    server.mayorPermissionMethod(text);
                }
                //night mode.....................................................................................
                if (server.getDayVoteNight().equals("night") && ((role instanceof godFather && isAlive) ||
                        (role instanceof Lecter && isAlive) || (role instanceof normalMafia && isAlive))
                ) {
                    server.killPlayer(text);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof townDoctor)
                        && isAlive) {
                    server.heal(text);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof detective)
                        && isAlive) {
                    server.isHeMafia(text, this);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof Sniper && isAlive)) {
                    server.sniperShoot(text, this);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof Lecter && isAlive)) {
                    server.saveMafia(text);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof psychologist && isAlive)) {
                    server.mute(text);
                }
                if (server.getDayVoteNight().equals("night") && (role instanceof dieHard && isAlive)) {
                    server.killedRolesInfo(text);
                }


                //.................................................................................................
                if (isAlive && canSpeak) {
                    server.sendAll(name + ": " + text);
                }
            }
            server.sendAll(name + " exited from the game!!!");
            server.removeClientHandler(this);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //getters and setters........................................................................................

    /**
     * Gets player role.
     *
     * @return the player role
     */
    public Role getPlayerRole() {

        return playerRole;
    }

    /**
     * Gets vote num.
     *
     * @return the vote num
     */
    public int getVoteNum() {

        return voteNum;
    }

    /**
     * Gets vote to kill.
     *
     * @return the vote to kill
     */
    public int getVoteToKill() {

        return voteToKill;
    }

    /**
     * Gets handler name.
     *
     * @return the handler name
     */
    public String getHandlerName() {

        return name;
    }

    /**
     * Sets can vote.
     *
     * @param canVote the can vote
     */
    public void setCanVote(boolean canVote) {

        this.canVote = canVote;
    }

    /**
     * Sets can speak.
     *
     * @param canSpeak the can speak
     */
    public void setCanSpeak(boolean canSpeak) {

        this.canSpeak = canSpeak;
    }

    /**
     * Sets vote num.
     *
     * @param voteNum the vote num
     */
    public void setVoteNum(int voteNum) {

        this.voteNum = voteNum;
    }

    /**
     * Sets alive.
     *
     * @param alive the alive
     */
    public void setAlive(boolean alive) {

        isAlive = alive;
    }

    //booleans......................................................................................................

    /**
     * Is he alive boolean.
     *
     * @return the boolean
     */
    public boolean isHeAlive() {
        return isAlive;

    }

    /**
     * Is ready boolean.
     *
     * @return the boolean
     */
    public boolean isReady() {

        return isReady;
    }

    //file methods......................................................................................................

    /**
     * Write.
     *
     * @param string the string
     * @throws IOException the io exception
     */
    public void write(String string) throws IOException {

        dataOutputStream.writeUTF(string);
    }

    /**
     * Load.
     *
     * @param file the file
     */
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

    //voting and killing methods..........................................................................................

    /**
     * Increase vote num.
     */
    public void increaseVoteNum() {

        voteNum++;
    }

    /**
     * Increase kill vote num.
     */
    public void increaseKillVoteNum() {

        voteToKill++;
    }


}
