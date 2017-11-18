package Client;

import Server.SocketListener;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.Observer;

public class Client extends Observable implements Observer {
    /**
     * Socket handle
     */
    private Socket sock;
    /**
     * Output stream, writes a Packet to the socket
     */
    private PrintWriter output;
    /**
     * Reads from socket, runs as its own Thread
     */
    private SocketListener listener;

    /**
     * Username
     */
    private String username;

    /**
     * Constructor
     * Create socket, read from it, close it
     */
    public Client(String serverAddress, int port) {
        try {
            //hostaddress = JOptionPane.showInputDialog(null, "choose a Serveraddress ");
            username = InetAddress.getLocalHost().getHostAddress();

            /**
             * Create a new socket. Server localhost (192.168.2.127), port 7777
             */
            sock = new Socket(InetAddress.getByName(serverAddress), port);
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();
            if (out != null && in != null) {
                output = new PrintWriter(new OutputStreamWriter(out));
                listener = new SocketListener(in);
            }
            listener.addObserver(this);
            new Thread(listener).start();
        } catch (ConnectException e) {
            System.out.println("ConnectException. Chances are the server is turned off or the port is blocked or wrong.");
            close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }

        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sendData(address + " has Joined.");
    }

    /**
     * Close the input stream and the socket.
     */
    public void close() {
        try {
            output.close();
            sock.close();
            setChanged();
            notifyObservers(new CloseRequest());
        } catch (SocketException e) {
            // Do nothing here
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("Closed chat.");
    }

    /**
     * Send the server information   
     * @param data  String to be sent.
     */

    public void sendData(String data){
        System.out.println("Sending " + username + " '" + data + "'");
        output.println(data);
        output.flush();
        if (data.startsWith("$DISCONNECT")) {
            close();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof SocketListener) {
            setChanged();
            notifyObservers((String) arg);
        }

    }
}
