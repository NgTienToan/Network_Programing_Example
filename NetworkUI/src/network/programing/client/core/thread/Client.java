package network.programing.client.core.thread;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Client {
    private String hostname;
    private int port;
    private String userID;
    private InputStream inputStream;
    private OutputStream outputStream;
    List<String> userOnline;

    public Client(
            String hostname,
            int port,
            String username,
            InputStream inputStream,
            OutputStream outputStream
    ) {
        this.hostname = hostname;
        this.port = port;
        this.userID = username;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the chat server");

            ReadThread readThread = new ReadThread(socket, this, outputStream);
            readThread.start();
            new WriteThread(socket, this, readThread, userID, inputStream).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }

    void setuserID(String userID) {
        this.userID = userID;
    }

    String getuserID() {
        return this.userID;
    }

    public List<String> getUserOnline() {
        return userOnline;
    }
}
