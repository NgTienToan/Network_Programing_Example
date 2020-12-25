public class ServerApplication {
    public static void main(String[] args) {
        Server server = new Server(Constant.LISTEN_PORT);
        server.execute();
    }
}
