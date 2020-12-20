import java.util.*;

public class ClientApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server host: ");
        // String hostname = scanner.nextLine();
        String hostname = "127.0.0.1";
        int port = 9000;

        Client client = new Client(hostname, port);
        client.execute();
    }
}
