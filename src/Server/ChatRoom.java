package Server;

import java.util.*;

public class ChatRoom extends Observable {
    /**
     * Handle to the server controller class
     */
    private Server server;
    /**
     * List of users in the chat room. This could have been a list of AccountHandlers we will use String
     */
    private Map<String, AccountHandler> users;
    /**
     * Chat room name
     */
    private String name;
    /**
     * Chat room password - not used yet
     */
    private String pass;

    /**
     * Constructor   
     * @param server  handle to the main server   
     * @param name   chatroom name   
     */
    public ChatRoom(Server server, String name) {
        this.server = server;
        this.name = name;
        users = new HashMap<>();
    }

    /**
     * Send a message to all users   
     * @param s message to send
     */
    public synchronized void sendToAll(String s) {
        setChanged();
        notifyObservers(s);
    }

    /**
     * Add a user to the list if it doesn't already exist   
     * @param user  username   
     * @return   true if added, false if already exists
     */
    public boolean addUser(AccountHandler user) {
        if (!users.containsKey(user)) {
            users.put(user.getName(), user);
            addObserver(user);
            return true;
        }
        return false;
    }

    /**
     * Remove a user from the list.   
     * @param user  username   
     */
    public synchronized void removeUser(AccountHandler user) {
        deleteObserver(user);
        server.remove(user);
    }

    public String name() {
        return name;
    }

    public List<String> getUsers() {
        return new ArrayList<>(users.keySet());
    }
}