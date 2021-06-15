import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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

                //.................................................................................................
                if (isAlive) {
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


    public boolean isReady() {

        return isReady;
    }

    public Role getPlayerRole() {

        return playerRole;
    }

    public int getVoteNum() {

        return voteNum;
    }

    public int getVoteToKill() {

        return voteToKill;
    }

    public void setCanVote(boolean canVote) {

        this.canVote = canVote;
    }

    public void setVoteNum(int voteNum) {

        this.voteNum = voteNum;
    }

    public String getHandlerName() {

        return name;
    }

    public boolean isHeAlive() {
        return isAlive;

    }

    public void setAlive(boolean alive) {

        isAlive = alive;
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

    public void increaseVoteNum() {

        voteNum++;
    }

    public void increaseKillVoteNum() {

        voteToKill++;
    }


}
