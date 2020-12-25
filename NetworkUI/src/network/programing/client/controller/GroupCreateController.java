package network.programing.client.controller;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GroupCreateController {
    public Button closeButton;
    public TextField groupName;
    private MainController mainController;

    public void onCloseButtonClick(MouseEvent mouseEvent) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void onCreateButtonClick(MouseEvent mouseEvent) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        mainController.setGroupList(groupName.getText());
        stage.close();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
