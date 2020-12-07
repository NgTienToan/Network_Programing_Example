package network.programing.client.thread;

import network.programing.client.util.Constant;

import java.io.*;
import java.net.Socket;

class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Client client;
    private InputStream is = null;
    public boolean downloadFileFlag = false;

    public ReadThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

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
            if(downloadFileFlag) receiveFile();
            else {
                try {
                    String response = reader.readLine();
                    System.out.println(response);
                    if(response == null) {
                        System.out.println("disconect to server");
                        break;
                    }
                } catch (IOException ex) {
                    System.out.println("Error reading from server: " + ex.getMessage());
                    break;
                }
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
            System.out.println("file name: " + filename);
            fileSize = dis.readInt();
            System.out.println("size_file: " + fileSize);

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeUTF(filename);
            dos.writeInt(fileSize);
            dos.flush();
            byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];
            int byteRead, current = 0;
            do {
                byteRead = dis.read(data);
                fos.write(data, 0, byteRead);
                if (byteRead >= 0) {
                    current += byteRead;
                }

            } while (current != fileSize);
            System.out.println("RECEIVE FILE DONE!");
            fos.flush();
            dis.close();
            is.close();
            dos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            downloadFileFlag = false;
        }
    }
}
