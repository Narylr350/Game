package server.net;

import server.service.GameService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final List<PlayerConnection> players = new ArrayList<>();

    public static void main(String[] args) {
        final int PORT = 8888;
        final int PLAYER_COUNT = 3;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动，等待 " + PLAYER_COUNT + " 个客户端连接...");

            while (players.size() < PLAYER_COUNT) {
                Socket socket = serverSocket.accept();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                String name = reader.readLine();
                int playerId = players.size() + 1;

                PlayerConnection player = new PlayerConnection(
                        playerId, name, socket, reader, writer
                );

                players.add(player);

                System.out.println("第 " + playerId + " 个客户端已连接："
                        + socket.getInetAddress() + ":" + socket.getPort()
                        + "，名字：" + name);

                writer.println("欢迎你，" + name + "，你的编号是：" + playerId);
            }

            System.out.println("3 个客户端已全部连接，开始游戏...");

            // ====== 这里调用 Game 发牌 ======
            GameService game = new GameService();
            game.dealCards();

            // 给每个玩家发送自己的牌
            for (PlayerConnection player : players) {
                String cardMsg = "你的手牌： " + GameService.cardsToString(player.getCards());
                player.getWriter().println(cardMsg);
            }

            // 给所有玩家发系统消息
            broadcast("系统：发牌完成，游戏开始！");
            // 如果你想临时测试底牌，也可以发出来
//            broadcast("底牌： " + Game.cardsToString(game.getHoleCards()));

            // 控制台输入线程
            new Thread(() -> {
                try {
                    BufferedReader console = new BufferedReader(
                            new InputStreamReader(System.in)
                    );

                    String input;
                    while ((input = console.readLine()) != null) {
                        broadcast("服务器：" + input);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // 客户端线程
            for (PlayerConnection player : players) {
                new Thread(() -> handleClient(player)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(PlayerConnection player) {
        try {
            System.out.println("开始处理客户端 " + player.getPlayerId());

            String msg;
            while ((msg = player.getReader().readLine()) != null) {
                System.out.println(player.getName() + " 说：" + msg);
                broadcastOthers(player, player.getName() + " 说：" + msg);
            }

        } catch (Exception e) {
            System.out.println(player.getName() + " 断开连接");
        }
    }

    private static void broadcast(String msg) {
        for (PlayerConnection player : players) {
            player.getWriter().println(msg);
        }
    }

    private static void broadcastOthers(PlayerConnection self, String msg) {
        for (PlayerConnection player : players) {
            if (player != self) {
                player.getWriter().println(msg);
            }
        }
    }
}
