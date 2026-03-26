package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static void main(String[] args) {
        final int PORT = 8888;
        final int PLAYER_COUNT = 3;

        List<Socket> players = new ArrayList<>();
        List<PrintWriter> writers = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动，等待 " + PLAYER_COUNT + " 个客户端连接...");

            while (players.size() < PLAYER_COUNT) {
                Socket socket = serverSocket.accept();
                players.add(socket);

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writers.add(writer);

                System.out.println("第 " + players.size() + " 个客户端已连接："
                        + socket.getInetAddress() + ":" + socket.getPort());
            }

            System.out.println("3 个客户端已全部连接，开始通信...");

            // ⭐ 服务器输入线程
            new Thread(() -> {
                try {
                    BufferedReader console = new BufferedReader(
                            new InputStreamReader(System.in)
                    );

                    String input;
                    while ((input = console.readLine()) != null) {
                        for (PrintWriter w : writers) {
                            w.println("服务器：" + input);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // 客户端线程
            for (int i = 0; i < players.size(); i++) {
                Socket player = players.get(i);
                int playerId = i + 1;
                new Thread(() -> handleClient(player, playerId)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket player, int playerId) {
        try {
            System.out.println("开始处理客户端 " + playerId);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(player.getInputStream())
            );

            String name = reader.readLine();
            System.out.println("客户端 " + playerId + " 身份：" + name);

            String msg;
            while ((msg = reader.readLine()) != null) {
                System.out.println("客户端 " + name + " 说：" + msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}