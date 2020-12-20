
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public int port;
    public Hashtable<String, UserThread> userThreads = new Hashtable<>();
    public Hashtable<String, GroupChat> groupChats = new Hashtable<>();

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

    public void createGroup(String groupName, UserThread user) {
        if(groupChats.get(groupName) != null) {
            user.sendMessage("[SERVER] GROUP EXISTED!");
            return;
        }else {
            GroupChat groupChat = new GroupChat(groupName);
            groupChat.userMember.put(user.getUserId(), user);
            groupChat.adminMember.put(user.getUserId(), user);
            groupChats.put(groupName, groupChat);
            user.sendMessage("[SERVER] CREATE GROUP SUCCESS");
        }
    }

    public void listGroupUser(UserThread user) {
        groupChats.entrySet().stream().forEach(element -> {
            GroupChat groupChat = element.getValue();
            if(groupChat.userMember.get(user.userID) != null) {
                user.sendMessage(groupChat.groupName + "\n");
            }
        });
        user.sendMessage("[SERVER] END LIST GROUP");
    }

    public void sendMessageGroup(String groupName, UserThread user, String message) {
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.userMember.get(user.getUserId()) != null ) {
                groupChat.userMember.entrySet().stream().forEach(element -> {
                    // if(element.getValue().userID != user.getUserId()) {
                        element.getValue().sendMessage("[" + groupName + "] "+ message);
                    // }
                });
            }else {
                user.sendMessage("[SERVER] USER NOT ALLOW GROUP");
            }
        }else {
            user.sendMessage("[SERVER] GROUP NOT EXISTED!");
        }
    }

    public void addUserToGroup(String groupName, UserThread user, UserThread newUser) {
        if(newUser == null) {
            user.sendMessage("[SERVER] " + newUser.userID + " NOT EXISTED");
            return;
        }
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.adminMember.get(user.getUserId()) != null && groupChat.userMember.get(newUser.getUserId()) == null) {
                groupChat.userMember.put(newUser.getUserId(), newUser);
                user.sendMessage("[SERVER] ADD USER SUCCESS");
                newUser.sendMessage("[SERVER] YOU HAVE BEEN ADDED TO GROUP " + groupName);
            }else {
                user.sendMessage("[SERVER] USER NOT ADMIN OR USER EXIST");
            }
        }else {
            user.sendMessage("[SERVER] GROUP NOT EXISTED!");
        }
    }

    public void removeUserFromGroup(String groupName, UserThread user, UserThread oldUser) {
        if(oldUser == null) {
            user.sendMessage("[SERVER] " + oldUser.userID + " NOT EXISTED");
            return;
        }
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.adminMember.get(user.getUserId()) != null && groupChat.userMember.get(oldUser.userID) != null) {
                groupChat.userMember.remove(oldUser.userID);
                if(groupChat.adminMember.get(oldUser.userID) != null) {
                    groupChat.adminMember.remove(oldUser.userID);
                }
                user.sendMessage("[SERVER] REMOVE USER SUCCESS");
                oldUser.sendMessage("[SERVER] YOU HAVE BEEN REMOVE FROM GROUP " + groupName);
            }else {
                user.sendMessage("USER NOT ADMIN OR USER EXIST");
            }
        }else {
            user.sendMessage("GROUP NOT EXISTED!");
        }
    }

    public void deleteGroup(String groupName, UserThread user) {
        
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.adminMember.get(user.getUserId()) != null) {

                groupChats.remove(groupName);
                user.sendMessage("[SERVER] DELETE GROUP SUCCESS");

            }else {
                user.sendMessage("USER NOT ADMIN OR USER EXIST");
            }
        }else {
            user.sendMessage("GROUP NOT EXISTED!");
        }

    }

    public void outGroup(String groupName, UserThread user) {
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.adminMember.get(user.getUserId()) != null) {

                groupChats.remove(groupName);
                user.sendMessage("[SERVER] OUT GROUP SUCCESS");
            }else if(groupChat.userMember.get(user.getUserId()) != null) {
                groupChat.userMember.remove(user.getUserId());
            }
        }else {
            user.sendMessage("GROUP NOT EXISTED!");
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

//    /**
//     * send file method
//     */
//    private void sendFile(String toUser, String fileName) {
//        Socket sendToSocket = null;
//        String fileSource = Constant.PUBLIC_SOURCE + "/" + fileName;
//
//        try {
//            if(hasUsers(toUser)) {
//                sendToSocket = userThreads.get(toUser).getSocket();
//                OutputStream os = sendToSocket.getOutputStream();
//
//                File file = new File(fileSource);
//                FileInputStream fis = new FileInputStream(file);
//                byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];
//
//                System.out.println("Start transfer file...");
//                int fileSize = (int) file.length(), current = 0;
//                int byteRead;
//                do {
//                    byteRead = fis.read(data);
//                    os.write(data, 0, byteRead);
//                    os.flush();
//                    if (byteRead >= 0) {
//                        current += byteRead;
//                    }
//                } while (current != fileSize);
//
//                System.out.println("Transfer Done");
//                fis.close();
//            }
//        } catch (Exception e) {
//
//        }
//    }

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

class GroupChat {
    public String groupName;
    public Hashtable<String, UserThread> userMember = new Hashtable<>();
    public Hashtable<String, UserThread> adminMember = new Hashtable<>();
    public boolean adminAccept = true;
    public GroupChat(String groupName) {
        this.groupName = groupName;
    }
}