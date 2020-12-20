package network.programing.server.thread;

import network.programing.server.util.Constant;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Objects;

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

        userOnline.append("[ONLINE USER] [");
        System.out.println("[ONLINE USER]");

        userThreads.entrySet().stream().forEach(element -> {
            System.out.println(element.getKey());
            userOnline.append(element.getKey()).append(",");
        });

        userOnline.delete(userOnline.length() - 1, userOnline.length());
        userOnline.append("]");
        broadcast(userOnline.toString(), null);
    }

    /**
     * log all file user upload
     */
    public void logPublicFile(String toUser) {
        StringBuilder allFiles = new StringBuilder();
        allFiles.append("Files: ");
        //Creating a File object for directory
        File directoryPath = new File(Constant.PUBLIC_SOURCE);
        //List of all files and directories
        File[] filesList = directoryPath.listFiles();
        System.out.println("List of files and directories in the specified directory:");
        for(File file : Objects.requireNonNull(filesList)) {
            System.out.println("File name: "+file.getName());
            System.out.println("File path: "+file.getAbsolutePath());
            System.out.println("Size :"+file.getTotalSpace());
            System.out.println(" ");
            allFiles.append("\n").append(file.getName());
        }

        broadcast(allFiles.toString(), toUser);
    }
}
