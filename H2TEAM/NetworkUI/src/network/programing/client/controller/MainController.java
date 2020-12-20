package network.programing.client.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import network.programing.client.core.thread.ReadThread;
import network.programing.client.model.CellFile;
import network.programing.client.model.Message;
import network.programing.client.core.util.Constant;
import network.programing.client.model.CellRenderer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
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
    @FXML
    public ListView chatPane;
    @FXML
    public Label usernameLabel;
    @FXML
    public AnchorPane chatBoxContainer;
    @FXML
    public Button downloadButton;

    private String currentUserSelected;
    private Hashtable<String, List> usersChatMessage;
    private double xOffset = 0;
    private double yOffset = 0;
    private OutputStream outputStream;
    private String userID;

    public void LoadUserOnline() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userListBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                StringBuilder user = new StringBuilder().append(newValue);
                currentUserSelected = newValue;

                Task<VBox> chatContainerRemove = new Task<VBox>() {
                    @Override
                    protected VBox call() throws Exception {
                        chatPane.getItems().clear();
                        List<Message> allMessage = usersChatMessage.get(user.toString());
                        if(allMessage != null) {
                            allMessage.stream().forEach(e -> {
                                addToChat(user.toString(), e.getContent(), e.isMe(), false);
                            });
                        }
                        return null;
                    }
                };

                Thread deleteItemThread = new Thread(chatContainerRemove);
                deleteItemThread.setDaemon(true);
                Platform.runLater(deleteItemThread);
            }
        });

        fileListBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.isEmpty()) {
                    downloadButton.setDisable(true);
                }
                else {
                    downloadButton.setDisable(false);
                }
            }
        });

        usersChatMessage = new Hashtable<>();
        chatPane.setMouseTransparent(true);
        chatPane.setFocusTraversable(false);
    }

    // send button clicked
    public void sendButtonAction(MouseEvent mouseEvent) {
        String message = messsageInput.getText();

        PrintWriter writer = new PrintWriter(outputStream, true);
        StringBuilder toServerMessage = new StringBuilder();
        if(currentUserSelected != null) {
            toServerMessage.append("~").append(userID).append("~");
            toServerMessage.append("[").append(currentUserSelected).append("] ");
        }
        toServerMessage.append(message);
        System.out.println("message: " + toServerMessage.toString());
        messsageInput.clear();

        addToChat(currentUserSelected, message, true, true);

        writer.println(toServerMessage.toString());
    }

    public void setUserList(List<String> userList) {
        Platform.runLater(() -> {
            ObservableList<String> users = FXCollections.observableList(userList);
            userListBox.setItems(users);
            userListBox.setCellFactory(new CellRenderer());
        });

        userList.stream().forEach(e -> {
            usersChatMessage.put(e, new ArrayList<String>());
        });
    }

    public void onCloseMainWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) closeMainButton.getScene().getWindow();
        stage.close();
    }

    public synchronized void addToChat(String fromUser, String message, boolean me, boolean newMessage) {

        List<Message> messageList = usersChatMessage.get(fromUser);
        if(messageList == null) {
            usersChatMessage.put(fromUser, new ArrayList());
            messageList = usersChatMessage.get(fromUser);
        }

        if(newMessage)
        {
            messageList.add(new Message(fromUser, message, me));
        }

        if(me) {
            if(currentUserSelected != null && currentUserSelected.equals(fromUser)) {
                Task<HBox> othersMessages = new Task<HBox>() {
                    @Override
                    protected HBox call() throws Exception {
                        Image image = new Image(getClass().getClassLoader().getResource("avatar.png").toString());
                        ImageView avatarImage = new ImageView(image);
                        avatarImage.setFitHeight(32);
                        avatarImage.setFitWidth(32);
                        Label label = new Label();
                        label.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                        label.setPadding(new Insets(5.0,5.0,
                                2.0,
                                5.0));
                        label.setText(message);
                        label.setFont(new Font("", 20));

                        HBox hBox = new HBox();
                        hBox.setAlignment(Pos.TOP_RIGHT);
                        hBox.getChildren().addAll(label, avatarImage);
                        return hBox;
                    }
                };

                othersMessages.setOnSucceeded(event -> {
                    chatPane.getItems().add(othersMessages.getValue());
                });

                Thread t = new Thread(othersMessages);
                t.setDaemon(true);
                t.start();
            }
        }
        else
        {
            if(currentUserSelected != null && currentUserSelected.equals(fromUser)) {
                Task<HBox> othersMessages = new Task<HBox>() {
                    @Override
                    protected HBox call() throws Exception {
                        Image image = new Image(getClass().getClassLoader().getResource("avatar.png").toString());
                        ImageView avatarImage = new ImageView(image);
                        avatarImage.setFitHeight(32);
                        avatarImage.setFitWidth(32);
                        Label label = new Label();
                        label.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                        label.setPadding(new Insets(5.0,5.0,
                                2.0,
                                5.0));
                        label.setText(message);
                        label.setFont(new Font("", 20));

                        HBox hBox = new HBox();
                        hBox.getChildren().addAll(avatarImage, label);
                        return hBox;
                    }
                };

                othersMessages.setOnSucceeded(event -> {
                    chatPane.getItems().add(othersMessages.getValue());
                });

                Thread t = new Thread(othersMessages);
                t.setDaemon(true);
                t.start();
            }
        }
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setFileList(List<String> files) {
        Platform.runLater(() -> {
            ObservableList<String> filesList = FXCollections.observableList(files);
            fileListBox.setItems(filesList);
            fileListBox.setCellFactory(new CellFile());
        });
    }

    public void onChangeSelectionUser(MouseEvent mouseEvent) {
    }

    public void onClickFetchFile(MouseEvent mouseEvent) {
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(Constant.FILE_LIST);
    }

    public void onClickDownload(MouseEvent mouseEvent) {
        StringBuilder file = new StringBuilder().append(fileListBox.getSelectionModel().getSelectedItems());
        file.delete(0, 1).delete(file.length() - 1, file.length());

        StringBuilder message = new StringBuilder().append("~").append(userID).append("~")
                .append(Constant.DOWNLOAD).append(" ").append(file);

        PrintWriter writer = new PrintWriter(outputStream, true);
        System.out.println(message);
        ReadThread.downloadFileFlag = true;
        writer.println(message.toString());
    }

    public void setUserID(String userID) {
        this.userID = userID;
        usernameLabel.setText(userID);
    }

    public void downloadedSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setContentText("Tải file thành công! hãy kiểm tra trong folder của chương trình!");
        alert.showAndWait();
    }

    public void showWarningFileNotFound() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thất bại");
        alert.setContentText("Không tìm thấy file!");
        alert.showAndWait();
    }
}

