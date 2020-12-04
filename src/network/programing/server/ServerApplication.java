package network.programing.server;

import network.programing.server.thread.Server;
import network.programing.server.util.Constant;

public class ServerApplication {
    public static void main(String[] args) {
        Server server = new Server(Constant.LISTEN_PORT);
        server.execute();
    }
}
