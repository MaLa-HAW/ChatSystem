package Server;

import java.util.*;
import java.net.*;
import java.io.*;

public class AccountHandler implements Runnable {
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
     * Output stream, handles Packets
     */
    private OutputStream output;
    /**
     * Buffer of messages to send
     */
    private ArrayList<String> messages;
    /**
     * Handle to the server controller
     */
    private Server server;
    /**
     * Running or closed? Running by default, closed when finished/disconnected
     */
    private boolean running;
    /**
     * Input stream object reader
     */
    private InputStream input;
    /**
     * Closed or not
     */
    private boolean closed;
    /**
     * List of chatrooms
     */
    private ArrayList<ChatRoom> chatrooms;
    /**
     * List of chatrooms
     */
    private String commands = "COMMANDLIST:\n$COMMANDLIST\n$DISCONNECT\n$USERLIST\n$ROOMLIST\n$ROOMJOIN<!>CHATROOM\n$CURRENTROOM\n$SETNAME<!>newname";


    /**
     * No empty account handlers, otherwise big errors
     */
    private AccountHandler() {
    }

    public AccountHandler(String name, String pass, Socket sock, Server server) {
        messages = new ArrayList<String>();
        chatrooms = new ArrayList<ChatRoom>();
        closed = false;
        this.name = name;
        this.pass = pass;
        this.sock = sock;
        this.server = server;

        chatrooms = server.getChatrooms();

        System.out.println("Server has connected to " + name + "!");
        try {
            output = this.sock.getOutputStream();
        } catch (IOException e) {
            close();
            System.out.println("Can't get outputstream");
            e.printStackTrace();
        }
    }

    /**
     * does whatever is needed (current: sends messages)
     * closes when done
     */
    public void run() {
        running = true;
        try {
            input = sock.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        while (running) {
            write();
            try {
                int available = input.available();
                while (available != 0 && running) {
                    available = input.available();
                    byte[] bytes = new byte[available];
                    input.read(bytes);
                    String msg = new String(bytes);


                    System.out.println("Receiving: '" + msg + "'");
                    processMessage(msg);
                    available = input.available();
                }
            } catch (SocketException e) {
                System.out.println("Manual disconnect close for user " + name + ".");
                close();
            } catch (EOFException e) {
                close();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
        close();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Do the proper action for the message, depending on its splitlength
     *
     * @param s String
     */
    private void processMessage(String s) {

        String[] parts = s.split("<!>");
        String commandPart = "";
        String detailPart = "";

        if (parts.length == 1) {
            commandPart = parts[0].trim();
        } else {
            commandPart = parts[0].trim();
            detailPart = parts[1].trim();
        }


        switch (commandPart) {
            case "$CURRENTROOM":
                for (ChatRoom room : chatrooms) {
                    ArrayList<String> userList = room.getUsers();
                    if (userList.contains(name)) {
                        sendData("CURRENTROOM:\n" + room.name());
                    }
                }
                break;
            case "$SETNAME":
                if (parts.length == 2) {
                    Boolean nameInUse = false;
                    for (ChatRoom room : chatrooms) {
                        ArrayList<String> userList = room.getUsers();
                        if (userList.contains(detailPart)) {
                            nameInUse = true;
                        }
                    }
                    if (!nameInUse) {
                        server.changeUsername(this, name, detailPart);
                        sendData("changed your username to " + detailPart);

                    } else {
                        sendData("username " + detailPart + " is already in use ");
                    }
                } else {
                    sendData("wrong syntax: SETNAME<!>newname ");
                }
                break;
            case "$COMMANDLIST":
                sendData(commands);
                break;
            case "$DISCONNECT":
                close();
                break;
            case "$USERLIST": // username = ipaddress ....
                for (int i = 0; i < chatrooms.size(); i++) {
                    ArrayList<String> userList = chatrooms.get(i).getUsers();
                    if (userList.contains(name)) {
                        String userListString = "USERLIST:\n";
                        for (String u : userList) {
                            userListString = userListString + u + "\n";
                        }
                        sendData(userListString);
                    }
                }
                break;
            case "$ROOMLIST":
                String chatroomListString = "AVAILABLE CHATROOMS: \n";
                for (ChatRoom chatroom : chatrooms) {
                    chatroomListString = chatroomListString + chatroom.name() + "\n";
                }
                sendData(chatroomListString);
                break;
            case "$ROOMJOIN":
                if (parts.length == 2) {
                    for (int i = 0; i < chatrooms.size(); i++) {
                        if (chatrooms.get(i).name().equals(detailPart)) {
                            changeChatroom(chatrooms.get(i));
                            sendToChatroom(name + " Joined " + chatrooms.get(i).name());
                        }
                    }
                } else {
                    sendData("wrong syntax: $ROOMJOIN<!>CHATROOM ");
                }
                break;
            default: // send text
                sendToChatroom(name + ": " + s);
                break;
        }
    }

    /**
     * Write a message to all user in the same chatroom.
     */
    private void sendToChatroom(String s) {
        for (int i = 0; i < chatrooms.size(); i++) {
            ArrayList<String> userList = chatrooms.get(i).getUsers();
            if (userList.contains(name)) {
                System.out.println(chatrooms.get(i).name() + " : " + name);
                chatrooms.get(i).sendToAll(s);
            }
        }
    }

    /**
     * Write all messages in the buffer.
     */
    private void write() {
        while (messages.size() > 0) {
            sendData(messages.remove(0));
        }
    }

    /**
     * @param data: Add data to the buffer
     */
    private void addData(String data) {
        messages.add(data);
    }

    /**
     * @param data: Send data into the stream
     */
    public synchronized void sendData(String data) {
        if (!running || closed) {
            return;
        }
        if (output == null || running == false) {
            messages.add(data);
            return;
        }
        try {
            System.out.println("Sending " + name + " '" + data + "'");

            output.write(new String(data).getBytes());
            output.flush();
        } catch (IOException e) {
            System.out.println("Can't write bytes");
            e.printStackTrace();
            close();
        }
    }

    /**
     * Send a notification of disconnect to all chatrooms currently in
     * Close the account thread.
     * Close all streams and the socket and request to be removed.
     */
    private synchronized void close() {
        if (closed) {
            return;
        }
        setRunning(false);
        /*for (int i = 0; i < chatrooms.size(); i++) {
            chatrooms.get(i).sendToAll(Server.NAME + Server.getTime() + name + " has disconnected." + chatrooms.get(i).name());
            chatrooms.get(i).removeUser(name);
        }*/
        try {
            System.out.println("Closing socket to " + name);
            output.close();
            input.close();
            sock.close();
        } catch (IOException e) {
            System.out.println("Error closing datastream");
            e.printStackTrace();
        } finally {
            server.remove(this);
        }
        closed = true;
    }

    /**
     * Set running to a value
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Add a user to the chatroom
     */
    public void addUserToRoom(ChatRoom room) {
        room.addUser(name);
    }

    /**
     * Change the Chatroom.
     *
     * @param room: change the chatroom to room
     */
    public void changeChatroom(ChatRoom room) {
        for (int i = 0; i < chatrooms.size(); i++) {
            if (chatrooms.get(i).getUsers().contains(name)) {
                chatrooms.get(i).removeUser(name);
                room.addUser(name);
            }
        }
    }

    /**
     * String representation
     */
    public String toString() {
        return name + ", " + pass;
    }
}
