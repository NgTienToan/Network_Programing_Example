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
            if(groupChat.userMember.get(user.getUserId()) != null) {
                user.sendMessage(groupChat.groupName + "\n");
            }
        });
        user.sendMessage("[SERVER] END LIST GROUP");
    }

    public void sendMessageGroup(String groupName, UserThread user, String message) {
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.userMember.get(user.getUserId()) != null ) {
                groupChat.userMember.entrySet().forEach(element -> {
//                        element.getValue().sendMessage("[" + groupName + "] "+ message);
                });

                groupChat.userMember.entrySet().stream().forEach(element -> {});
            }else {
                user.sendMessage("[SERVER] USER NOT ALLOW GROUP");
            }
        }else {
            user.sendMessage("[SERVER] GROUP NOT EXISTED!");
        }
    }

    public void addUserToGroup(String groupName, UserThread user, UserThread newUser) {
        if(newUser == null) {
            user.sendMessage("[SERVER] " + newUser.getUserId() + " NOT EXISTED");
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
            user.sendMessage("[SERVER] " + oldUser.getUserId() + " NOT EXISTED");
            return;
        }
        if(groupChats.get(groupName) != null) {
            GroupChat groupChat = groupChats.get(groupName);
            if( groupChat.adminMember.get(user.getUserId()) != null && groupChat.userMember.get(oldUser.getUserId()) != null) {
                groupChat.userMember.remove(oldUser.getUserId());
                if(groupChat.adminMember.get(oldUser.getUserId()) != null) {
                    groupChat.adminMember.remove(oldUser.getUserId());
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
    public boolean broadcast(String message, String toUser) {
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
    public void removeUser(String userID) {
        System.out.println("The user " + userID + " quitted");
        userThreads.remove(userID);
    }


    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    public boolean hasUsers(String userID) {
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
        allFiles.append("[FILES] ");
        //Creating a File object for directory
        File directoryPath = new File(Constant.PUBLIC_SOURCE);
        //List of all files and directories
        File[] filesList = directoryPath.listFiles();
        System.out.println("List of files and directories in the specified directory:");
        allFiles.append("[");

        for(File file : Objects.requireNonNull(filesList)) {
            allFiles.append(file.getName()).append(",");
        }

        allFiles.delete(allFiles.length() - 1, allFiles.length());
        allFiles.append("]");
        broadcast(allFiles.toString(), toUser);
    }
}


