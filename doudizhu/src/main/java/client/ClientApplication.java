package client;

import java.util.Scanner;

public class ClientApplication {

    public void start(String host, int port) {
        Scanner scanner = new Scanner(System.in);
        try {
            ClientConnection connection = ClientConnection.connect(host, port);
            if (connection == null) {
                System.out.println("连不上");
                return;
            }
            System.out.println("已经连接服务器");

            Thread readerThread = new Thread(() -> readServerMessages(connection));
            Thread inputThread = new Thread(() -> readConsoleInput(scanner, connection));
            inputThread.setDaemon(true);

            readerThread.start();
            inputThread.start();
            readerThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private void readServerMessages(ClientConnection connection) {
        try {
            String message;
            while ((message = connection.getReader().readLine()) != null) {
                System.out.println(message);
            }
        } catch (Exception e) {
            if (!connection.isClosed()) {
                e.printStackTrace();
            }
        } finally {
            connection.closeQuietly();
            System.out.println("服务器连接已关闭");
            System.exit(0);
        }
    }

    private void readConsoleInput(Scanner scanner, ClientConnection connection) {
        try {
            while (!connection.isClosed() && scanner.hasNextLine()) {
                connection.send(scanner.nextLine());
            }
        } catch (IllegalStateException ignored) {
            // 程序退出时 Scanner 可能已关闭，输入线程直接结束。
        }
    }
}
