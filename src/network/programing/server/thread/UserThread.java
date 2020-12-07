package network.programing.server.thread;

import network.programing.server.util.Constant;

import java.io.*;
import java.net.Socket;

class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private String userID;

    public UserThread(Socket socket, Server server, String userID) {
        this.socket = socket;
        this.server = server;
        this.userID = userID;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            printUsers();
            String serverMessage = "New user connected: " + this.userID;
            server.broadcast(serverMessage, null);

            String clientMessage;

            do {
                clientMessage = reader.readLine();
                String toUser = null;
                StringBuffer strBuff = new StringBuffer();

                try {
                    String temp = clientMessage.toUpperCase();
                    if(clientMessage.toUpperCase().contains("[FILE LIST]")) {
                        server.logPublicFile(this.userID);
                    }
                    else if(clientMessage.toUpperCase().contains("[DOWNLOAD]")) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(clientMessage);
                        System.out.println(stringBuilder.length());
                        System.out.println(clientMessage.indexOf("[DOWNLOAD]"));
                        stringBuilder.delete(0, (clientMessage.indexOf("[DOWNLOAD]") + 11));
                        System.out.println(stringBuilder);

                        File file = new File(Constant.PUBLIC_SOURCE + "/" + stringBuilder.toString());
                        FileTransferThread fileTransferThread = new FileTransferThread(this.socket,file, stringBuilder.toString());
                        fileTransferThread.run();
                    }
                    else {
                        toUser = clientMessage.substring(clientMessage.indexOf('[') + 1, clientMessage.indexOf(']'));
                        strBuff.append(clientMessage);
                        strBuff.delete(clientMessage.indexOf('['), clientMessage.indexOf(']') + 1);
                    }
                }
                catch(StringIndexOutOfBoundsException e) {
                    if(!clientMessage.equals("bye")) sendMessage("Wrong syntax!");
                }
                catch(NullPointerException e) {
                    break;
                }

                if(!server.broadcast(strBuff.toString(), toUser)) writer.println("user not online");

            } while (!clientMessage.equals("bye"));

            server.removeUser(userID);
            socket.close();

            serverMessage = userID + " has quitted.";
            server.broadcast(serverMessage, null);
            server.logUserOnline();

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers(userID)) {
            writer.println("Connected users: " + userID);
            server.logUserOnline();
        } else {
            writer.println("No other users connected");
        }
    }

    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * get socket
     * @return socket
     */
    public Socket getSocket() {
        return socket;
    }
}
