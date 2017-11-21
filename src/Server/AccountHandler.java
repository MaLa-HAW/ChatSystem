package Server;

import java.util.*;
import java.net.*;
import java.io.*;

public class AccountHandler extends Thread implements Observer {
    /**
     * Username
     */
    private String name;

    /**
     * Socket to the client
     */
    private String pass;

    /**
     * Socket to the client
     */
    private Socket sock;

    /**
     * Output stream writer
     */
    private PrintWriter output;

    /**
     * Handle to the server controller
     */
    private Server server;

    /**
     * Input stream object reader
     */
    private BufferedReader input;

    /**
     * Closed or not
     */
    private boolean closed;

    /**
     * List of chatrooms
     */
    private ArrayList<ChatRoom> chatrooms;


    public AccountHandler(String name, String pass, Socket sock, Server server) {
        closed = false;
        this.name = name;
        this.pass = pass;
        this.sock = sock;
        this.server = server;

        try {
            this.input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        chatrooms = server.getChatrooms();

        System.out.println("Server has connected to " + name + "!");
        try {
            output = new PrintWriter(new OutputStreamWriter(this.sock.getOutputStream()));
        } catch (IOException e) {
            close();
            System.out.println("Can't get outputstream");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Running");
        try {
            while(!closed) {
                String message = input.readLine();
                System.out.println(message);
                processMessage(message);
            }
        } catch (IOException e) {
            close();
        }
        System.out.println("End");
    }

    /**
     * Do the proper action for the message, depending on its splitlength   
     * @param s String
     */
    public void processMessage(String s) {

        String[] parts = s.split("<!>");
        String commandPart = "";
        String detailPart = "";

        if(parts.length == 1) {
            commandPart = parts[0].trim();
        } else if(parts.length == 2){
            commandPart = parts[0].trim();
            detailPart = parts[1].trim();
        }

        switch (commandPart) {
            case "$DISCONNECT":
                System.out.println("I'm in disc");
                sendToChatroom(name + ": left Server");
                close();
                break;

            case "$USERLIST": // username = ipaddress ....
                System.out.println("I'm in userlist");
                for (int i = 0; i < chatrooms.size(); i++){
                    List<String> userList = chatrooms.get(i).getUsers();
                    if(userList.contains(name)){
                        sendData("Userlist:");
                        for (String u : userList) {
                            sendData(u);
                        }
                        sendData("End");

                    }
                }
                sendData("End");
                break;

            case "$ROOMLIST":
                System.out.println("I'm in roomlist");
                sendData("Available Chatrooms: ");
                for (ChatRoom chatroom : chatrooms) {
                    sendData(chatroom.name());
                }
                sendData("End");
                break;

            case "$ROOMJOIN":
                changeRoom(detailPart);
                break;

            case "$SETNAME":
                setUserName(detailPart);
                break;

            default: // send text
                System.out.println("I'm in message");
                sendToChatroom(name + ": " + s);
                break;
        }
    }

    /**
     * Puts the user in another chatroom
     * @param roomName new Chatroom
     * @return true, if user is in the new chatroom, false if not
     */
    private boolean changeRoom(String roomName) {
        boolean changedRoom = false;
        System.out.println("I'm in roomjoin");
        if(!roomName.isEmpty()) {
            for (ChatRoom room: chatrooms){
                if(room.name().equals(roomName)){
                    changeChatroom(room);
                    sendToChatroom(name + " Joined "+ room.name());
                    changedRoom = true;
                    break;
                }
            }
        } else {
            sendData("wrong syntax: $ROOMJOIN<!>CHATROOM ");
        }
        if (!changedRoom) {
            sendData("ROOMJOIN FAILED");
        }
        return changedRoom;
    }


    /**
     * Sets a new User Name
     * @param newName new UserName
     * @return true if user could be renamed, false if not
     */
    private boolean setUserName(String newName) {
        if (newName.isEmpty()) {
            sendData("wrong syntax: $SETNAME<!>USERNAME");
            return false;
        }
        System.out.println("I'm in setname");
        for(ChatRoom cr : server.getChatrooms()) {
            for (String username :cr.getUsers()) {
                if (username.equals(newName)) {
                    sendData("SETNAME FAILED");
                    return false;
                }
            }
        }
        sendToChatroom(name + " changed Name to " + newName);
        name = newName;
        return true;
    }

    /**
     * Write a message to all user in the same chatroom.
     */
    private void sendToChatroom(String s) {
        chatrooms.stream().filter(room -> room.getUsers().contains(name)).forEach(room -> room.sendToAll(s));
    }

    /**
     * @param data: Send data into the stream
     *
     */
    public void sendData(String data) {
        if (!closed && output != null) {
            System.out.println("Sending " + name + " '" + data + "'");
            output.println(data);
            output.flush();
        }
    }

    /**
     * Send a notification of disconnect to all chatrooms currently in   
     * Close the account thread.   
     * Close all streams and the socket and request to be removed.
     */
    private void close() {
        if (!closed) {
            for (int i = 0; i < chatrooms.size(); i++) {
                chatrooms.get(i).sendToAll(Server.NAME + Server.getTime() + name + " has disconnected." + chatrooms.get(i).name());
                chatrooms.get(i).removeUser(this);
            }
            try {
                System.out.println("Closing socket to " + name);
                output.close();
                sock.close();
                input.close();
            } catch (IOException e) {
                System.out.println("Error closing datastream");
            } finally {
                server.remove(this);
            }
            closed = true;
        }
    }

    /**
     * Add a user to the chatroom
     */
    public void addUserToRoom(ChatRoom room) {
        room.addUser(this);
    }

    /**
     * Accessor methods - get name
     */
    public String getAccName() {
        return name;
    }

    /**
     * Change the Chatroom.
     * @param room: change the chatroom to room
     */
    public void changeChatroom(ChatRoom room){
        for(int i = 0; i<chatrooms.size(); i++){
            if(chatrooms.get(i).getUsers().contains(name)){
                chatrooms.get(i).removeUser(this);
                room.addUser(this);
            }
        }
    }

    /**
     * String representation
     */
    public String toString() {
        return name + ", " + pass;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ChatRoom && arg instanceof String) {
            sendData((String)arg);
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (char c: name.toCharArray()) {
            hashCode += c;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AccountHandler) {
            return ((AccountHandler)o).getName().equals(name);
        }
        return false;
    }
}
