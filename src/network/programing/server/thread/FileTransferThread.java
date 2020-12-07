package network.programing.server.thread;

import network.programing.server.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileTransferThread extends Thread {
    private Socket sendToSocket;
    private File file;


    public FileTransferThread(Socket sendToSocket, File file) {
        this.sendToSocket = sendToSocket;
        this.file = file;
    }

    public void run() {
        try {
            System.out.println("asdasd");
            OutputStream os = sendToSocket.getOutputStream();
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
        } catch (Exception e) {

        }

        this.interrupt();
    }
}
