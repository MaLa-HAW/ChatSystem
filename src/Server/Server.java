package Server;

import textFileLogger.TextFileLogger;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server extends Thread {

    private final int MAX_USERS = 255;

    private int userCount;

    /**
     * Messages generated and sent from the server have this name
     **/
    public static final String NAME = "@SERVER";

    /**
     * Map of usernames to accounts
     */
    private HashMap<String, AccountHandler> accMap;

    /**
     * Socket acceptor
     */
    private ServerSocket socketAcceptor;

    /** Chatroom list */
    private ArrayList<ChatRoom> chatrooms;

    private TextFileLogger logger;

    private boolean closed;

    /**
     * Constructor   
     * Create a map & list of users   
     * Initialize socket listener   
     * Initialize thread pool executor   
     * Initialize chatroom   
     * Accept connections   
     * Close on error
     */
    public Server(int port) {
        userCount = 0;
        accMap = new HashMap<String, AccountHandler>();
        chatrooms = new ArrayList<ChatRoom>();
        chatrooms.add(new ChatRoom(this, "DEFAULT"));
        chatrooms.add(new ChatRoom(this, "TODO"));
        chatrooms.add(new ChatRoom(this, "HUMMEL"));


        System.out.println("Initializing server.");
        try {
            this.logger = new TextFileLogger("/home/andre/git/ChatSystem/src/Server/logging.txt");
            socketAcceptor = new ServerSocket(port);
            log("Initializing server.");
            log(socketAcceptor.toString());
		    System.out.println(socketAcceptor);
        } catch (Exception e) {
            System.out.println("socketAcceptor wrong");
            e.printStackTrace();
        }

        closed = false;
    }

    public Server() {
        this(7777);
    }

    @Override
    public void run() {
        while (!closed) {
            acceptConnections();
        }

    }

    /**
     * Accept connections   
     */
    private void acceptConnections() {
            try {

                Socket sock = socketAcceptor.accept();
                String username = sock.getInetAddress().toString().substring(1);
                AccountHandler userHandler = new AccountHandler(username, "", sock, this);
                if (userCount < MAX_USERS) {
                    if (!accMap.containsKey(username)) {
                        userCount++;
                        userHandler.start();
                        accMap.put(username, userHandler);
                        accMap.keySet().stream().forEach(key -> System.out.println(key));
                        userHandler.addUserToRoom(getChatroomByName("DEFAULT"));
                    } else {
                        Thread.sleep(100);
                        userHandler.sendData("You are already connected");
//                        Thread.sleep(100);
                        userHandler.close();
                    }
                } else {
                    Thread.sleep(100);
                    userHandler.sendData("Server is full");
//                    Thread.sleep(100);
                    userHandler.close();
                }
            } catch (Exception e) {
                //System.out.println("Did not work somewhere");
                //e.printStackTrace();
            }
    }


    public synchronized void log(String s) {
        try {
            this.logger.log(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Creates a new Chatroom is no one exists with the given name
     * @param roomName
     * @return true is new chatroom is created, false if not
     */
    public boolean createRoom(String roomName) {
        if (getChatroomByName(roomName) == null) {
            return chatrooms.add(new ChatRoom(this, roomName));
        }
        return false;
    }


    public boolean closeRoom(String roomName) {
        ChatRoom room = getChatroomByName(roomName);
        if (room != null) {
            room.closeRoom();
            return true;
        }
        return false;
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

    /**
     * Closes the Socket from this server
     *
     * @return true - socket is closed
     *         false - socket could not be closed
     */
    public boolean closeServer() {
        if (socketAcceptor != null && !socketAcceptor.isClosed()) {
            try {
                socketAcceptor.close();
                logger.close();
                socketAcceptor = null;
                closed = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        new Server().start();
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

