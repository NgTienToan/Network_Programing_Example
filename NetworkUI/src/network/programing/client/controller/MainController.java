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
import javafx.stage.Stage;
import network.programing.client.core.thread.Client;
import network.programing.client.model.CellRenderer;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public Button closeMainButton;
    @FXML
    public VBox onlinePersonBox;
    @FXML
    public ListView userListBox;
    @FXML
    public ListView fileListBox;
    @FXML
    public TextField messsageInput;

    private Client client;

    private double xOffset = 0;
    private double yOffset = 0;

    public void LoadUserOnline() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void sendButtonAction(MouseEvent mouseEvent) {
    }

    public void setUserList(List userList) {
        Platform.runLater(() -> {
            ObservableList<String> users = FXCollections.observableList(userList);
            userListBox.setItems(users);
            userListBox.setCellFactory(new CellRenderer());
        });
    }

    public void onCloseMainWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) closeMainButton.getScene().getWindow();
        stage.close();
    }
}
