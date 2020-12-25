import java.io.*;
import java.net.*;

public class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    public String userID;
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

    public String getUserId() {
        return this.userID;
    }
    public void run() {
        try {

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            printUsers();
            String serverMessage = "New user connected: " + this.userID + "\n";
            server.broadcast(serverMessage, null);

            String clientMessage = null;
            String oldMessage = null;

            do {
                clientMessage = reader.readLine();

                if (oldMessage == null || ( !oldMessage.equals(clientMessage) )) {
                    oldMessage = clientMessage;
                    String toUser = null;
                    StringBuffer strBuff = new StringBuffer();
                    System.out.println(clientMessage);
                    try {
                        String _clientMessage = clientMessage.toUpperCase();
                        if(_clientMessage.toUpperCase().contains("[LIST GROUP]")) {
                            this.server.listGroupUser(this);
                            continue;
                        }else if(_clientMessage.toUpperCase().contains("[GROUP]")) {
                            System.out.println("VAO ROI");
                            strBuff.append(clientMessage);
                            strBuff.delete(strBuff.toString().indexOf('~'), 1);
                            strBuff.delete(0, strBuff.toString().indexOf('~') + 1);
                            strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                            
                            if(strBuff.toString().toUpperCase().contains("[CREATE]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String groupName = strBuff.toString();
                                if(groupName.indexOf('@') != -1) {
                                    this.sendMessage("[SERVER] NAME GROUP INVALID");
                                    continue;
                                } 
                                this.server.createGroup(groupName, this);
                                System.out.println("CREATE");
                            }else if(_clientMessage.toUpperCase().contains("[ADD]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String message = strBuff.toString();
                                System.out.println(message);
                                String groupName = message.split("@")[0];
                                String userName = message.split("@")[1];
                                if(this.server.userThreads.get(userName) != null ) {
                                    this.server.addUserToGroup(groupName, this, this.server.userThreads.get(userName));
                                }
                                System.out.println("ADD");
                            }else if(strBuff.toString().toUpperCase().contains("[REMOVE]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String message = strBuff.toString();
                                System.out.println(message);
                                String groupName = message.split("@")[0];
                                String userName = message.split("@")[1];
                                if(this.server.userThreads.get(userName) != null ) {
                                    this.server.removeUserFromGroup(groupName, this, this.server.userThreads.get(userName));
                                }
                                System.out.println("REMOVE");
                            }else if(strBuff.toString().toUpperCase().contains("[DELETE]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String groupName = strBuff.toString();
                                this.server.deleteGroup(groupName, this);
                                System.out.println("DELETE");
                            }else if(strBuff.toString().toUpperCase().contains("[OUT]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String groupName = strBuff.toString();
                                this.server.outGroup(groupName, this);
                                System.out.println("OUT");
                            }else if(strBuff.toString().toUpperCase().contains("[SEND]")) {
                                strBuff.delete(strBuff.toString().indexOf('['), strBuff.toString().indexOf(']') + 2);
                                String message = strBuff.toString();
                                System.out.println(message);         
                                String groupName = message.split("@")[0];
                                System.out.println(groupName);         
                                String _message = message.split("@")[1];
                                System.out.println(_message);         
                                this.server.sendMessageGroup(groupName, this, _message);
                                System.out.println("SEND");         
                            }
                            continue;
                        }else if (clientMessage.toUpperCase().contains("[FILE LIST]")) {
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
