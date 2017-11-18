package Client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Observable;
import java.util.Observer;

public class GUI implements Observer {

    private TextField msgField;

    private TextArea msgHistory;

    private Button sendMsg;

    private GridPane pane;

    private Stage stage;

    private Client client;

    public GUI(Client client) {
        stage = new Stage();
        pane = new GridPane();

        msgField = new TextField();
        msgField.setPrefWidth(200);

        msgHistory = new TextArea();
        msgHistory.setEditable(false);
        msgHistory.setPrefColumnCount(40);
        msgHistory.setPrefRowCount(20);

        sendMsg = new Button("Send Message");

        pane.add(msgHistory, 0,0);
        pane.add(msgField, 0,1);
        pane.add(sendMsg, 1,1);
        GridPane.setColumnSpan(msgHistory, 2);

        this.client = client;
        client.addObserver(this);

        msgField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (msgField.getText() != null && event.getCode().equals(KeyCode.ENTER)) {
                    sendMessage();
                }
            }
        });
        sendMsg.setOnAction(event -> sendMessage());
        stage.setOnCloseRequest(event -> this.client.close());
    }

    public void show() {
        stage.setScene(new Scene(pane));
        stage.show();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Client) {
            if (arg instanceof String) {
                msgHistory.appendText((String) arg + "\n");
            } else if (arg instanceof CloseRequest) {
                stage.close();
            }
        }
    }

    private void sendMessage() {
        if (msgField.getText() != null) {
            client.sendData(msgField.getText());
            msgField.clear();
        } else {
            System.out.println(msgField);
            System.out.println(msgField.getText());
        }
    }
}
