package network.programing.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import network.programing.client.core.thread.Client;

import java.io.IOException;

public class LoginController {
    @FXML
    public TextField hostInput;
    @FXML
    public TextField userNameInput;
    @FXML
    public TextField portInput;
    @FXML
    public Button btnExit;

    public static MainController mainController;

    private double xOffset;
    private double yOffset;

    /**
     * close login form
     * @param mouseEvent
     */
    public void exitButtonOnclick(MouseEvent mouseEvent) {
        Stage stage = (Stage) btnExit.getScene().getWindow();
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

        String username = userNameInput.getText();
        String host = hostInput.getText();
        Integer port = Integer.parseInt(portInput.getText());

        // Load main screen
        FXMLLoader root = new FXMLLoader(getClass().getResource("../fxml/main.fxml"));
        Parent parent = null;
        try {
            parent = root.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainController = root.getController();

        // Run Client thread
        new Client(host, port, username, mainController).execute();

        parent.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        parent.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(parent);

        //set transparent
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
