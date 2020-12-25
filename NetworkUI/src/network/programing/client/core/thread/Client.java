package network.programing.client.core.thread;

import network.programing.client.controller.MainController;
import network.programing.client.core.util.Constant;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Client {
    private String hostname;
    private int port;
    private String userID;
    private final MainController controller;
    List<String> userOnline;

    public Client(
            String hostname,
            int port,
            String username,
            MainController controller
    ) {
        this.hostname = hostname;
        this.port = port;
        this.userID = username;
        this.controller = controller;
    }

    public void execute() throws UnknownHostException, IOException {
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the chat server");
            OutputStream output = socket.getOutputStream();
            controller.setOutputStream(output);
            controller.setUserID(userID);
            ReadThread readThread = new ReadThread(socket, this, output, this.controller);
            readThread.start();

            PrintWriter writer = new PrintWriter(output, true);
            writer.println(userID);
    }

    void setuserID(String userID) {
        this.userID = userID;
    }

    public String getuserID() {
        return this.userID;
    }

    public List<String> getUserOnline() {
        return userOnline;
    }
}
