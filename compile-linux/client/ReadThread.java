import java.io.*;
import java.net.*;

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
            try {
                String response = reader.readLine();
                System.out.println(response);
                if(!downloadFileFlag) {
                    if (response == null) {
                        System.out.println("disconect to server");
                        break;
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
            System.out.println("file name: " + filename);
            fileSize = dis.readInt();
            System.out.println("file size: " + fileSize);

            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

//            dos.writeUTF(filename);
//            dos.writeInt(fileSize);
//            dos.flush();
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            downloadFileFlag = false;
        }
    }
}
