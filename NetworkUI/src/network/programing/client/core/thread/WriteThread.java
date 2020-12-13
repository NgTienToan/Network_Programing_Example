package network.programing.client.core.thread;

import network.programing.client.controller.MainController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private Client client;
    private ReadThread readThread;
    private String userID;
    private InputStream inputStream;
    private MainController controller;

    public WriteThread(
            Socket socket,
            Client client,
            ReadThread readThread,
            String username,
            InputStream inputStream,
            MainController controller
    ) {
        this.socket = socket;
        this.client = client;
        this.readThread = readThread;
        this.userID = username;
        this.inputStream = inputStream;
        this.controller = controller;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        writer.println(userID);

        String text;
        String message;

        do {
            text = scanner.nextLine();
            if(text.toUpperCase().startsWith("[DOWNLOAD]")) {
                message = "~" + userID + "~" + text;
                readThread.downloadFileFlag = true;
                writer.println(message);
            }
            message = "~" + userID + "~" + text;
            writer.println(message);
        } while (!text.equals("bye"));

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}
