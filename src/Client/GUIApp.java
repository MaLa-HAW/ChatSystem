package Client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class GUIApp extends Application {

    private final String DEF_PORT = "7777";

    private TextField serverAddress;

    private TextField port;

    private Button connect;

    private GridPane pane;

    private static final int GAP_SIZE = 10;

    @Override
    public void init() {
        this.pane = new GridPane();
        this.serverAddress = new TextField();
        this.port = new TextField();
        this.connect = new Button("Connect");
        this.connect.setOnAction(event -> connect());

        pane.add(new Label("Server:"), 0, 1);
        pane.add(serverAddress, 1, 1);
        try {
            serverAddress.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {}
        pane.add(new Label("Port:"), 0, 2);
        pane.add(port, 1, 2);
        port.setText(DEF_PORT);
        pane.add(connect, 1, 3);
        pane.setHgap(GAP_SIZE);
        pane.setVgap(GAP_SIZE);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat");
        primaryStage.show();
    }

    private void connect() {
        try {
            Client client = new Client(serverAddress.getText(), Integer.parseInt(port.getText()));
            GUI gui = new GUI(client);
            gui.show();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public static void main(String... args) {
        Application.launch();
    }
}