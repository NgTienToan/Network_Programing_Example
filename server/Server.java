
import java.io.*;
import java.net.*;
import java.util.*;

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
     * send file method
     */
    private void sendFile(String toUser, String fileName) {
        Socket sendToSocket = null;
        String fileSource = Constant.PUBLIC_SOURCE + "/" + fileName;

        try {
            if(hasUsers(toUser)) {
                sendToSocket = userThreads.get(toUser).getSocket();
                OutputStream os = sendToSocket.getOutputStream();

                File file = new File(fileSource);
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];

                System.out.println("Start transfer file...");
                int fileSize = (int) file.length(), current = 0;
                int byteRead;
                do {
                    byteRead = fis.read(data);
                    os.write(data, 0, byteRead);
                    os.flush();
                    if (byteRead >= 0) {
                        current += byteRead;
                    }
                } while (current != fileSize);

                System.out.println("Transfer Done");
                fis.close();
            }
        } catch (Exception e) {

        }
    }

    /**
     * log user online
     */
    public void logUserOnline() {
        StringBuffer userOnline = new StringBuffer();
        userOnline.append("user online:");
        System.out.println("user online:");
        userThreads.entrySet().stream().forEach(element -> {
            System.out.println(element.getKey());
            userOnline.append("\n").append(element.getKey());
        });
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
