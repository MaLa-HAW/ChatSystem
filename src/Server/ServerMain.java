package Server;

import java.util.Scanner;

public class ServerMain {

    public Server server;

    public Scanner scanner;

    public ServerMain() {
        scanner = new Scanner(System.in);
    }

    public void createServer() {
        if (server == null) {
            server = new Server();
            if (server != null) {
                System.out.println("Server created");
                return;
            }
        }
        System.out.println("Server could not be created");
    }

    public void createServer(int port) {
        if (server == null) {
            server = new Server(port);
            if (server != null) {
                System.out.println("Server created");
                return;
            }
        }
        System.out.println("Server could not be created");
    }

    public void killServer() {
        if (server != null) {
            server.closeServer();
            server = null;
            System.out.println("Server closed");
        }
        System.out.println("No Server to kill");
    }

    public void runServer() {
        if (server != null) {
            server.run();
            System.out.println("Server runs now");
        } else {
            System.out.println("You need to create a server first");
        }

    }

    public void createChatroom(String name) {
        if (server != null) {
            if (server.createRoom(name)) {
                System.out.println("Room created");
            } else {
                System.out.println("Could not create room");
            }
        } else {
            System.out.println("No server available");
        }
    }

    public void closeRoom(String name) {
        if (server != null) {
            ChatRoom room = server.getChatroomByName(name);
            if (room != null) {
                room.closeRoom();
                System.out.println("Room closed");
            } else {
                System.out.println("No room to close");
            }
        } else {
            System.out.println("No Server available");
        }
    }

    public void printHelp() {
        System.out.println("kill - Beendet den Server");
        System.out.println("create - Erstellt den Server");
        System.out.println("run - Startet den Server");
        System.out.println("exit - Beendet den Server und das Programm");
        System.out.println("add - Erstellt einen neuen Chatraum");
        System.out.println("close - Schließt einen Chatraum");
    }

    public void run() {
        boolean closed = false;
        while (!closed) {
            System.out.println("Bitte Auswahl eingeben (help für Hilfe)");
            String choice = scanner.nextLine();
            switch (choice) {
                case "close":
                    System.out.println("Bitte Namen eingeben");
                    closeRoom(scanner.nextLine());
                    break;

                case "add":
                    System.out.println("Bitte Namen eingeben");
                    createChatroom(scanner.nextLine());
                    break;

                case "kill":
                    killServer();
                    break;

                case "exit":
                    killServer();
                    closed = true;
                    break;

                case "create":
                    createServer();
                    break;

                case "run":
                    runServer();
                    break;

                default:
                    printHelp();
                    break;
            }
        }
        if (server != null) {
            server.closeServer();
        }
        scanner.close();
    }

    public static void main(String... args) {
        ServerMain main = new ServerMain();
        main.run();
    }

}
