package Server;

import java.util.*;

public class ChatRoom {
    /**
     * Handle to the server controller class
     */
    private Server server;
    /**
     * List of users in the chat room. This could have been a list of AccountHandlers we will use String
     */
    private ArrayList<String> users;
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
     * No empty constructor allowed   
     */
    private ChatRoom() {
    }

    /**
     * Constructor   
     * @param server  handle to the main server   
     * @param name   chatroom name   
     */
    public ChatRoom(Server server, String name) {
        this.server = server;
        this.name = name;
        users = new ArrayList<String>();
    }

    /**
     * Constructor   
     * @param server  handle to the main server   
     * @param name   chatroom name   
     * @param users  list of users to add
     */
    public ChatRoom(Server server, String name, ArrayList<String> users) {
        this(server, name);
        this.users = users;
    }

    /**
     * Send a message to all users   
     * @param s message to send
     */
    public synchronized void sendToAll(String s) {
        users.stream().forEach(user -> sendTo(s, user));
    }

    /**
     * Send a message to one user   
     * @param s  message to send   
     * @param user  username to send to
     */
    public void sendTo(String s, String user) {
        server.getAcct(user).sendData(s);
    }

    /**
     * Add a user to the list if it doesn't already exist   
     * @param user  username   
     * @return   true if added, false if already exists
     */
    public boolean addUser(String user) {
        if (!users.contains(user)) {
            users.add(user);
            return true;
        }
        return false;
    }

    /**
     * Remove a user from the list.   
     * @param user  username   
     * @return   true if removed, false if it did not exist
     */
    public synchronized boolean removeUser(String user) {
        return users.remove(user);
    }

    public String name() {
        return name;
    }

    public ArrayList<String> getUsers() {
        return users;
    }
}