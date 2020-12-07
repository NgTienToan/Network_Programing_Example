package network.programing.client.thread;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private Client client;

    public WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

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
                FileDownloadThread fileDownloadThread = new FileDownloadThread(this.socket);
                fileDownloadThread.run();
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
