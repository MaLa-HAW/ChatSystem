package Server;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    /** Messages generated and sent from the server have this name */
    public static final String NAME = "@SERVER";
    /**
     * Map of usernames to accounts
     */
    private HashMap<String, AccountHandler> accMap;
    /**
     * Socket acceptor
     */
    private ServerSocket socketAcceptor;

    /**
    /** Chatroom list */
    private ArrayList<ChatRoom> chatrooms;

    /**
     * Constructor   
     * Create a map & list of users   
     * Initialize socket listener   
     * Initialize thread pool executor   
     * Initialize chatroom   
     * Accept connections   
     * Close on error
     */
    public Server() {
        accMap = new HashMap<String, AccountHandler>();
        chatrooms = new ArrayList<ChatRoom>();
        chatrooms.add(new ChatRoom(this, "DEFAULT"));
        chatrooms.add(new ChatRoom(this, "TODO"));
        chatrooms.add(new ChatRoom(this, "HUMMEL"));

        System.out.println("Initializing server.");
        try {
            socketAcceptor = new ServerSocket(7777);
		System.out.println(socketAcceptor);
        } catch (Exception e) {
            System.out.println("socketAcceptor wrong");
            e.printStackTrace();
        }
        acceptConnections();
        try {
            socketAcceptor.close();
        } catch (Exception e) {
        }
    }

    /**
     * Accept connections   
     */
    private void acceptConnections() {
        while (socketAcceptor != null && !socketAcceptor.isClosed()) {
            try {
                Socket sock = socketAcceptor.accept();
                String username = sock.getInetAddress().toString() + " [" + accMap.size() + "]";
                AccountHandler userHandler = new AccountHandler(username, "", sock, this);
                userHandler.start();
                accMap.put(username, userHandler);
                userHandler.addUserToRoom(chatrooms.get(getDefaultChatroomIndex()));
            } catch (Exception e) {
                System.out.println("Did not work somewhere");
                e.printStackTrace();
            }
        }
    }

    private int getDefaultChatroomIndex() {
        int defaultChatroomIndex = 0;
        for (int i = 0; i<chatrooms.size(); i++){
            if (chatrooms.get(i).name().equals("DEFAULT")){
                defaultChatroomIndex = i;
            }
        }
        return defaultChatroomIndex;
    }

    /**
     * Remove an account from the account list and map   
     * @param acctName: Account name, as String   
     */
    public synchronized void remove(String acctName) {


        chatrooms.stream().filter(room -> room.getUsers().contains(acctName))
                .forEach(room -> room.removeUser(accMap.get(acctName)));
        accMap.remove(acctName);
    }

    public synchronized void remove(AccountHandler acct) {
        this.remove(acct.getAccName());
    }

    /**
     * @param name username of account we need   
     * @return   AccountHandler with username name
     */
    public AccountHandler getAcct(String name) {
        return accMap.get(name);
    }

    /**
     *	@return time in format hours:minutes:days
     */
    public static String getTime() {
        Calendar c = Calendar.getInstance();
        String time = ( c.get(c.HOUR_OF_DAY) < 10 ? "0" + c.get(c.HOUR_OF_DAY) : c.get(c.HOUR_OF_DAY) )
                + ":" +
                ( c.get(c.MINUTE) < 10 ? "0" + c.get(c.MINUTE) : c.get(c.MINUTE) )
                + ":" +
                ( c.get(c.SECOND) < 10 ? "0" + c.get(c.SECOND) : c.get(c.SECOND) );
        return time;
    }


    public static void main(String[] args) {
        new Server();
    }

    public ArrayList<ChatRoom> getChatrooms() {
        return chatrooms;
    }

    /**
     * Returns the chatroom with the given name
     *
     * @param name Name of the chatroom
     * @return  chatroom with the given name, null if there is no room with that name
     */
    public ChatRoom getChatroomByName(String name) {
        for (ChatRoom room : chatrooms) {
            if (room.name().equals(name)) {
                return room;
            }
        }
        return null;
    }
}


/*
ExecutorService ist eine Schnittstelle, die Executor erweitert. Unter anderem sind hier Operationen zu finden, die die Ausführer herunterfahren. Im Falle von Thread-Pools ist das nützlich, da die Threads ja sonst nicht beendet würden, weil sie auf neue Aufgaben warten.


Eine wichtige statische Methode der Klasse Executors ist newCachedThreadPool(). Das Ergebnis ist ein ExecutorService-Objekt, eine Implementierung von Executor mit der Methode execute(Runnable):




ServerSocket
This class implements server sockets. A server socket waits for requests to come in over the network. It performs some operation based on that request, and then possibly returns a result to the requester.

The actual work of the server socket is performed by an instance of the SocketImpl class. An application can change the socket factory that creates the socket implementation to configure itself to create sockets appropriate to the local firewall.

accept()
Listens for a connection to be made to this socket and accepts it.
*/

