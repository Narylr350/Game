package client.bootstrap;

import client.auth.ClientAuthState;
import client.console.ConsoleInputLoop;
import client.net.ClientConnection;
import client.net.ServerMessageReader;

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

            ClientAuthState authState = new ClientAuthState();
            Thread readerThread = new Thread(new ServerMessageReader(connection, authState));
            Thread inputThread = new Thread(() -> new ConsoleInputLoop(scanner, connection).run());
            inputThread.setDaemon(true);

            readerThread.start();
            inputThread.start();
            readerThread.join();
            connection.closeQuietly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
