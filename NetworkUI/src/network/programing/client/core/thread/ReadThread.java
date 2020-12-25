package network.programing.client.core.thread;

import javafx.application.Platform;
import network.programing.client.controller.MainController;
import network.programing.client.core.util.Constant;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Client client;
    private InputStream is;
    private OutputStream outputStream;
    public static boolean downloadFileFlag = false;
    private final MainController controller;

    public ReadThread(
            Socket socket,
            Client client,
            OutputStream outputStream,
            MainController controller
    ) {
        this.socket = socket;
        this.client = client;
        this.outputStream = outputStream;
        this.controller = controller;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println("response from server: " + response);
                if(response.equals(Constant.FILE_NOT_FOUND)) {
                    downloadFileFlag = false;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            controller.showWarningFileNotFound();
                        }
                    });
                }
                else if(!downloadFileFlag) {
                    if (response != null && response.contains("~")) {
                        String fromUser = response.substring(response.indexOf('~') + 1, response.indexOf('~', 1));

                        StringBuilder strBuff= new StringBuilder().append(response);
                        strBuff.delete(response.indexOf('~'), response.indexOf('~', 1) + 1);
                        strBuff.delete(0,1);
                        controller.addToChat(fromUser, strBuff.toString(), false, true);
                    }
                    else {
                        if(response.contains("[ONLINE USER]")) {
                            String[] listUser = response.substring(15, response.length() - 1).split(",");
                            List<String> onlineUsers = new ArrayList<>(Arrays.asList(listUser));
                            controller.setUserList(onlineUsers);
                        }
                        else if(response.contains("[FILES]")) {
                            String[] listFile =  response.substring(9, response.length() - 1).split(",");
                            List<String> allFile = new ArrayList<>(Arrays.asList(listFile));
                            controller.setFileList(allFile);
                        }
                    }
                }
                else {
                    receiveFile();
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                break;
            }
        }
    }

    private void receiveFile() {
        try {
            is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            String filename;
            int fileSize;

            filename = dis.readLine();
            if(!filename.equals(Constant.FILE_NOT_FOUND)) {
                fileSize = dis.readInt();
                File file = new File(filename);
                FileOutputStream fos = new FileOutputStream(file);
                DataOutputStream dos = new DataOutputStream(fos);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);


                writer.println("[START]");
                byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];
                int byteRead, current = 0;
                do {
                    byteRead = dis.read(data);
                    System.out.println(new String(data));
                    fos.write(data, 0, byteRead);
                    if (byteRead >= 0) {
                        current += byteRead;
                    }

                } while (current <= fileSize);
                System.out.println("RECEIVE FILE DONE!");
                fos.flush();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.downloadedSuccess();
                    }
                });
            }
            else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.showWarningFileNotFound();
                    }
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            downloadFileFlag = false;
        }
    }
}
