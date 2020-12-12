package network.programing.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import network.programing.client.core.thread.Client;
import network.programing.client.model.CellRenderer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    public TextField txtUsername;
    @FXML
    public Button btnSignin;
    @FXML
    public Button btnExit;
    @FXML
    public Button closeMainButton;
    @FXML
    public VBox onlinePersonBox;
    @FXML
    public TextField portInput;
    @FXML
    public TextField hostInput;
    @FXML
    public TextField userNameInput;
    @FXML
    public ListView userListBox;
    @FXML
    public ListView fileListBox;
    @FXML
    public TextField messsageInput;
    @FXML
    private Client client;

    private double xOffset = 0;
    private double yOffset = 0;


    /**
     * close login form
     * @param mouseEvent
     */
    public void exitButtonOnclick(MouseEvent mouseEvent) {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }

    /**
     * close main window
     * @param mouseEvent
     */
    public void onCloseMainWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) closeMainButton.getScene().getWindow();
        stage.close();
    }

    /**
     * on login event
     * @param mouseEvent
     */
    public void Login(MouseEvent mouseEvent) {
        Stage primaryStage = new Stage();
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
        Parent root = null;

        String username = userNameInput.getText();
        String host = hostInput.getText();
        Integer port = Integer.parseInt(portInput.getText());

        // Run Client thread
        new Client(host, port, username).execute();

        // Load main screen
        try {
            root = FXMLLoader.load(getClass().getResource("../fxml/main.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);

        //set transparent
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void LoadUserOnline() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void sendButtonAction(MouseEvent mouseEvent) {
    }

    public void setUserList(Client client) {
        Platform.runLater(() -> {
            ObservableList<String> users = FXCollections.observableList(client.getUserOnline());
            userListBox.setItems(users);
            userListBox.setCellFactory(new CellRenderer());
        });
    }
}
