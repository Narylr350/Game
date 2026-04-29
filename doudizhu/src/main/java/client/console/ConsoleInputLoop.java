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
        try {
            while (!connection.isClosed() && scanner.hasNextLine()) {
                String input = scanner.nextLine();
                connection.send(input);
            }
        } catch (IllegalStateException ignored) {
            // 客户端退出时主线程会关闭 Scanner，输入线程安静结束即可。
        }
    }
}
