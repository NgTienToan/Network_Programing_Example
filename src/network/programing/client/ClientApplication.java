package network.programing.client;

import network.programing.client.thread.Client;

import java.util.Scanner;

public class ClientApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server host: ");
        String hostname = scanner.nextLine();
        int port = 9000;

        Client client = new Client(hostname, port);
        client.execute();
    }
}
