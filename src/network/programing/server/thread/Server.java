package network.programing.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server {
    private int port;
    private Hashtable<String, UserThread> userThreads = new Hashtable<>();

    public Server(int port) {
        this.port = port;
    }

    public synchronized void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String userID = reader.readLine();

                UserThread newUser = new UserThread(socket, this, userID);
                userThreads.put(userID, newUser);
                newUser.start();
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Delivers a message from one user to others (broadcasting)
     */
    boolean broadcast(String message, String toUser) {
        System.out.println("send to " + toUser + " message: " + message);
        if(toUser != null) {
            if(userThreads.get(toUser) != null) {
                userThreads.get(toUser).sendMessage(message);
                return true;
            }
            else return false;
        }
        else {
            userThreads.entrySet().stream().forEach(element -> {
                element.getValue().sendMessage(message);
            });
            return true;
        }
    }


    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userID) {
        System.out.println("The user " + userID + " quitted");
        userThreads.remove(userID);
    }


    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    boolean hasUsers(String userID) {
        return (userThreads.get(userID) != null);
    }

    /**
     * log user online
     */
    public void logUserOnline() {
        StringBuffer userOnline = new StringBuffer();
        userOnline.append("user online:\n");
        System.out.println("user online:");
        userThreads.entrySet().stream().forEach(element -> {
            System.out.println(element.getKey());
            userOnline.append(element.getKey() + "\n");
        });
        broadcast(userOnline.toString(), null);
    }
}
