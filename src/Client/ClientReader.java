package Client;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ClientReader implements Runnable {
    /** Input stream object reader as ObjectInputStream */
    private InputStream input;
    /** Handle to the socket */
    private Socket sock;
    /** Whether or not we're running */
    private boolean running;
    /** Handle to the Client */
    private Client client;

    public ClientReader(Socket sock, Client client) {
        this.sock = sock;
        this.client = client;
        running = true;
    }

    /**
     * Read from the socket while the socket is open   
     * Echo out all information
     */
    private void read() {
        try {
            input = sock.getInputStream();

            int available = input.available();
            while (available != 0) {
                byte[] bytes = new byte[available];
                input.read(bytes);
                String msg = new String(bytes);

                client._textArea.append(new String(bytes) + "\n");

                JScrollBar scroll = client._scrollPane.getVerticalScrollBar();
                scroll.setValue(scroll.getMaximum());

                System.out.println("Receiving: '" + msg + "'");
            }
        } catch (SocketException e) {
            client.close();
        } catch (EOFException e) {
            client.close();
        } catch (StreamCorruptedException e) {
            System.out.println(e);
            client.close();
        } catch (Exception e) { e.printStackTrace(); close(); }
    }

    /**
     * As long as this thread has not been requested to stop, tries to read message from server
     */
    public void run() {
        while (running) {
            read();
        }
    }

    /**
     * Set no running flag   
     * Attempt to close input   
     * Socket is NOT closed from here
     */
    public void close() {
        setRunning(false);
        try {
            input.close();
        } catch (IOException e) {
            System.out.println("Error closing input");
            e.printStackTrace();
        }
    }

    /**
     * @param r running
     */
    private void setRunning(boolean r) {
        running = r;
    }
}