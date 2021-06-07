import java.io.DataInputStream;
import java.io.IOException;

public class ThreadReader extends Thread {
    private DataInputStream dataInputStream;

    public ThreadReader(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }


    public void run() {
        try {
            while (true) {

                System.out.println(dataInputStream.readUTF());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
