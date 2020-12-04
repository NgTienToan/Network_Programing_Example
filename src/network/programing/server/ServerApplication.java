package network.programing;

import network.programing.thread.Server;

public class ServerApplication {
    public static void main(String[] args) {
        int port = 9000;

        Server server = new Server(port);
        server.execute();
    }
}
