package network.programing.client.core.thread;

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
    private String username;
    private InputStream inputStream;

    public WriteThread(
            Socket socket,
            Client client,
            ReadThread readThread,
            String username,
            InputStream inputStream
    ) {
        this.socket = socket;
        this.client = client;
        this.readThread = readThread;
        this.username = username;
        this.inputStream = inputStream;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        String userID;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name: ");

        userID = scanner.nextLine();
        client.setuserID(userID);
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
