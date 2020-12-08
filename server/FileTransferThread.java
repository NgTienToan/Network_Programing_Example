
import java.io.*;
import java.net.*;

public class FileTransferThread extends Thread {
    private Socket sendToSocket;
    private File file;
    private String filename;

    public FileTransferThread(Socket sendToSocket, File file, String fileName) {
        this.sendToSocket = sendToSocket;
        this.file = file;
        this.filename = fileName;
    }

    public void run() {
        try {
            OutputStream os = sendToSocket.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[Constant.BUFFER_FILE_TRANSFER];

            System.out.println("Start transfer file...");
            os.write(filename.getBytes(), 0, filename.length());
            System.out.println("file name: " + filename);

            int fileSize = (int) file.length(), current = 0;
            os.write(Integer.toString(fileSize).getBytes(), 0, String.valueOf(fileSize).length());
            System.out.println("file size:"  + fileSize);

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
            e.printStackTrace();
        }

        this.interrupt();
    }
}
