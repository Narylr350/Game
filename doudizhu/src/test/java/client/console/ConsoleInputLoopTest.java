package client.console;

import client.net.ClientConnection;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConsoleInputLoopTest {

    @Test
    void should_send_exit_without_closing_connection_locally() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Socket[] acceptedSocket = new Socket[1];
            Thread acceptThread = new Thread(() -> {
                try {
                    acceptedSocket[0] = serverSocket.accept();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            acceptThread.start();

            ClientConnection connection = ClientConnection.connect("127.0.0.1", serverSocket.getLocalPort());
            acceptThread.join(1_000);

            try (Socket serverSideSocket = acceptedSocket[0];
                 BufferedReader reader = new BufferedReader(new InputStreamReader(serverSideSocket.getInputStream()));
                 Scanner scanner = new Scanner("exit\n")) {
                new ConsoleInputLoop(scanner, connection).run();

                assertEquals("exit", reader.readLine());
                assertFalse(connection.isClosed());
            } finally {
                connection.closeQuietly();
            }
        }
    }
}
