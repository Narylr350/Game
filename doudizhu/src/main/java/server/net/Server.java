package server.net;

import client.Player;
import server.model.GameSession;
import server.model.PlayerState;
import server.service.GameSessionManager;
import server.util.CardUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final List<PlayerConnection> PLAYERS = new ArrayList<>();
    private static final GameSessionManager SESSION_MANAGER = new GameSessionManager();

    public static void main(String[] args) {
        final int port = 8888;
        final int playerCount = 3;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，等待 " + playerCount + " 个客户端连接...");

            acceptPlayers(serverSocket, playerCount);

            System.out.println(playerCount + " 个客户端已全部连接，开始游戏...");
            GameSession session = SESSION_MANAGER.startGame(collectPlayerNames());
            sendOpeningHands(session);

            broadcast("系统：发牌完成，游戏开始！");
            broadcast("系统：底牌已生成，等待后续抢地主逻辑接入。");
            System.out.println("系统：底牌已生成:"+CardUtil.cardsToString(SESSION_MANAGER.getCurrentSession().getHoleCards()));
            startConsoleThread();

            for (PlayerConnection player : PLAYERS) {
                new Thread(() -> handleClient(player)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void acceptPlayers(ServerSocket serverSocket, int playerCount) throws IOException {
        while (PLAYERS.size() < playerCount) {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String name = reader.readLine();
            int playerId = PLAYERS.size() + 1;

            PlayerConnection player = new PlayerConnection(playerId, name, socket, reader, writer);
            PLAYERS.add(player);

            System.out.println("第 " + playerId + " 个客户端已连接："
                    + socket.getInetAddress() + ":" + socket.getPort()
                    + "，名字：" + name);

            player.send("欢迎你，" + name + "，你的编号是：" + playerId);
        }
    }

    private static List<String> collectPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (PlayerConnection player : PLAYERS) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }

    private static void sendOpeningHands(GameSession session) {
        for (PlayerConnection connection : PLAYERS) {
            PlayerState playerState = session.findPlayerById(connection.getPlayerId());
            if (playerState == null) {
                continue;
            }

            connection.send("你的手牌： " + CardUtil.cardsToString(playerState.getCards()));
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

    private static void startConsoleThread() {
        new Thread(() -> {
            try {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                String input;
                while ((input = console.readLine()) != null) {
                    broadcast("服务器：" + input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void broadcast(String msg) {
        for (PlayerConnection player : PLAYERS) {
            player.send(msg);
        }
    }

    private static void broadcastOthers(PlayerConnection self, String msg) {
        for (PlayerConnection player : PLAYERS) {
            if (player != self) {
                player.send(msg);
            }
        }
    }
}
