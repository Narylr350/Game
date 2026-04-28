package client.console;

import client.net.ClientConnection;

import java.util.Scanner;

public class ConsoleInputLoop {
    private final Scanner scanner;
    private final ClientConnection connection;

    public ConsoleInputLoop(Scanner scanner, ClientConnection connection) {
        this.scanner = scanner;
        this.connection = connection;
    }

    public void run() {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            connection.send(input);
            if ("exit".equalsIgnoreCase(input)) {
                connection.closeQuietly();
                return;
            }
        }
    }
}
