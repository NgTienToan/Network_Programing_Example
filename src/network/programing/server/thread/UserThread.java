package network.programing.server.thread;

import network.programing.server.util.Constant;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private String userID;
    private boolean downloading = false;

    public UserThread(Socket socket, Server server, String userID) {
        this.socket = socket;
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
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

            String clientMessage = null;
            String oldMessage = null;

            do {
                clientMessage = reader.readLine();

                if (oldMessage == null || (!oldMessage.equals(clientMessage))) {
                    oldMessage = clientMessage;
                    String toUser = null;
                    StringBuffer strBuff = new StringBuffer();

                    try {
                        String temp = clientMessage.toUpperCase();
                        if (clientMessage.toUpperCase().contains("[FILE LIST]")) {
                            server.logPublicFile(this.userID);
                        } else if (clientMessage.toUpperCase().contains("[DOWNLOAD]") && !downloading) {
                            downloading = true;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(clientMessage);

                            System.out.println(stringBuilder.length());
                            System.out.println(clientMessage.indexOf("[DOWNLOAD]"));
                            stringBuilder.delete(0, (clientMessage.indexOf("[DOWNLOAD]") + 11));
                            System.out.println(stringBuilder);
                            fileTransfer(stringBuilder.toString());
                        } else {
                            toUser = clientMessage.substring(clientMessage.indexOf('[') + 1, clientMessage.indexOf(']'));
                            strBuff.append(clientMessage);
                            strBuff.delete(clientMessage.indexOf('['), clientMessage.indexOf(']') + 1);
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        if (!clientMessage.equals("bye")) sendMessage("Wrong syntax!");
                    } catch (NullPointerException e) {
                        break;
                    }

                    if (!server.broadcast(strBuff.toString(), toUser)) writer.println("user not online");
                }
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
     *
     * @return socket
     */
    public Socket getSocket() {
        return socket;
    }

    private void fileTransfer(String filename) {
        try {
            File file = new File(Constant.PUBLIC_SOURCE + "/" + filename);
            socket.setTcpNoDelay(true);
            OutputStream os = socket.getOutputStream();
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
                DataOutputStream dataOutputStream = new DataOutputStream(os);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];
                os.write("\n".getBytes());

                System.out.println("Start transfer file...");
                writer.write(filename);
                writer.newLine();
                writer.flush();
                System.out.println("file name: " + filename);

                int fileSize = (int) file.length(), current = 0;
                dataOutputStream.writeInt(fileSize);
                dataOutputStream.flush();
                System.out.println("file size:" + fileSize);

                String recMessage = reader.readLine();

                if (recMessage.toUpperCase().contains("[START]")) {
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
            } catch (FileNotFoundException e) {
                os.write("File not found".getBytes(), 0, "File not found".length());
                os.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            downloading = false;
        }
    }

}
