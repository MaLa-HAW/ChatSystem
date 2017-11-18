package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;

/**
 * Listens to the Socket an send Messages to the Observers
 */
public class SocketListener extends java.util.Observable implements Runnable {

    private BufferedReader input;

    public SocketListener(InputStream in) {
        this.input = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = input.readLine();
                setChanged();
                notifyObservers(message);
            }
        } catch (SocketException e) {
            //Do nothing here
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
