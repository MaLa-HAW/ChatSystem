package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;
import static java.lang.Thread.sleep;

public class Client extends JFrame{
    /**
     * Socket handle
     */
    private Socket sock;
    /**
     * Output stream, writes a Packet to the socket
     */
    private OutputStream output;
    /**
     * Reads from socket, runs as its own Thread
     */
    private ClientReader reader;
    /**
     * Username
     */
    private String username;
    /**
     * Password
     */
    private String password;
    /**
     * Is everything working properly?
     */
    private boolean running;

    private String hostaddress = "192.168.2.127";



    JTextArea _textArea;
    JTextField  _textField;
    JScrollPane _scrollPane;

    /**
     * Constructor
     * Create socket, read from it, close it
     */
    public Client() {
        try {
            running = true;

            /**
             * Create a new socket. Server localhost (192.168.2.127), port 7777
             */
            sock = new Socket(InetAddress.getLocalHost(), 7777);
            output = sock.getOutputStream();
            reader = new ClientReader(sock, this);
            Thread readerThread = new Thread(reader);
            readerThread.start();

            if(running()){
                //sendData("FREITEXT");
            }

        } catch (ConnectException e) {
            System.out.println("ConnectException. Chances are the server is turned off or the port is blocked or wrong.");
            close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }


        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                sendData(" has Left.");
                try {
                   sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                close();
            }
        });

        setLayout(new BorderLayout());

        _textArea = new JTextArea(20, 40);
        _textArea.setLineWrap(true);
        _textArea.setEditable(false);

        _scrollPane = new JScrollPane(_textArea);
        add(_scrollPane, BorderLayout.CENTER);

        _textField = new JTextField(30);
        _textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                sendData(_textField.getText());
                _textField.setText("");
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                sendData(_textField.getText());
                _textField.setText("");
            }
        });

        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.add(_textField, BorderLayout.CENTER);
        entryPanel.add(sendButton, BorderLayout.EAST);
        add(entryPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);

        String address    = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        _textArea.append("Connecting to: " + address + "\n");
        _textArea.append("Connected\n");

        sendData(address + " has Joined.");
    }

    /**
     * Close the input stream and the socket.
     */
    public void close() {
        if (running) {
            running = false;
            try {
                reader.close();
                sock.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                // closed before initialized, this is OK
            }
            System.out.println("Closed chat.");
        }
    }

    /**
     * Send the server information   
     * @param data  String to be sent.
     */

    private void sendData(String data) {
        if (!running) { return; }
        try {
            System.out.println("Sending " + username + " '" + data + "'");
            output.write((data).getBytes());
            output.flush();
        } catch (IOException e) {
            System.out.println("Can't write bytes"); e.printStackTrace();
            close();
        }
    }

    public void setUser(String user) {
        username = user;
    }

    public void setPass(String pass) {
        password = pass;
    }

    public String getUser() {
        return username;
    }

    public String getPass() {
        return password;
    }

    public boolean running() {
        return running;
    }

    public static void main(String[] args) {
        new Client();
    }
}
