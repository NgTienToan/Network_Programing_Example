import java.io.*;
import java.net.*;

public class Client {
    private String hostname;
    private int port;
    private String userID;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");

            ReadThread readThread = new ReadThread(socket, this);
            readThread.start();
            new WriteThread(socket, this, readThread).start();

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
}
