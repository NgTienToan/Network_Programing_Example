package network.programing.client.thread;

import network.programing.client.util.Constant;

import java.io.*;
import java.net.Socket;

public class FileDownloadThread extends Thread {
    Socket sock_to_server = null;
    InputStream is = null;

    FileDownloadThread(Socket socket) {
        sock_to_server = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("hello");
            is = sock_to_server.getInputStream();
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
            sock_to_server.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
