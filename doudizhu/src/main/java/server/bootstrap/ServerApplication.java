package server.bootstrap;

import server.auth.AuthenticationService;
import server.auth.JdbcUserRepository;
import server.auth.SocketAuthenticator;
import server.flow.TurnInputCoordinator;
import server.game.GameServerRunner;
import server.session.PlayerMessageListener;
import server.session.PlayerSession;
import server.session.PlayerSessionRegistry;
import util.CredentialPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerApplication {
    private final PlayerSessionRegistry registry = new PlayerSessionRegistry();
    private final TurnInputCoordinator coordinator = new TurnInputCoordinator();
    private final SocketAuthenticator authenticator =
            new SocketAuthenticator(new AuthenticationService(new JdbcUserRepository(), new CredentialPolicy()));

    public void start(int port, int playerCount) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logServer("服务器启动，等待 " + playerCount + " 个客户端连接...");
            acceptPlayers(serverSocket, playerCount);
            logServer(playerCount + " 个客户端已全部连接，开始游戏...");

            startConsoleThread();
            new GameServerRunner(registry, coordinator).run();
        } catch (IOException e) {
            logServer("服务器启动或运行异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptPlayers(ServerSocket serverSocket, int playerCount) throws IOException {
        int originalTimeout = serverSocket.getSoTimeout();
        serverSocket.setSoTimeout(200);
        try {
            while (registry.size() < playerCount) {
                try {
                    Socket socket = serverSocket.accept();
                    startAuthenticationThread(socket, playerCount);
                } catch (SocketTimeoutException ignored) {
                    // 周期性检查当前在线玩家数，候场玩家断开时不会占住名额。
                }
            }
        } finally {
            serverSocket.setSoTimeout(originalTimeout);
        }
    }

    private void startAuthenticationThread(Socket socket, int playerCount) {
        Thread thread = new Thread(() -> authenticateAndRegister(socket, playerCount));
        thread.setName("auth-" + socket.getPort());
        thread.setDaemon(true);
        thread.start();
    }

    private void authenticateAndRegister(Socket socket, int playerCount) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String playerName = authenticator.authenticate(reader, writer);
            if (playerName == null) {
                socket.close();
                return;
            }

            PlayerSession session = registry.registerAuthenticated(playerName, socket, reader, writer, playerCount);
            if (session == null) {
                writer.println("房间已满，请稍后再试");
                socket.close();
                return;
            }

            logServer("第 " + session.getPlayerId() + " 个客户端已连接："
                    + socket.getInetAddress() + ":" + socket.getPort()
                    + "，名字：" + playerName);

            session.send("欢迎你，" + playerName + "，你的编号是：" + session.getPlayerId());
            startPlayerMessageListener(session);
        } catch (IOException e) {
            logServer("客户端认证异常：" + e.getMessage());
            try {
                socket.close();
            } catch (IOException closeException) {
                logServer("关闭异常连接失败：" + closeException.getMessage());
            }
        }
    }

    private void startPlayerMessageListener(PlayerSession session) {
        Thread thread = new Thread(new PlayerMessageListener(session, registry, coordinator, this::logServer));
        thread.setName("player-" + session.getPlayerId());
        thread.start();
    }

    private void startConsoleThread() {
        new Thread(() -> {
            try {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                String input;
                while ((input = console.readLine()) != null) {
                    registry.broadcast("服务器：" + input);
                }
            } catch (Exception e) {
                logServer("控制台线程异常：" + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void logServer(String message) {
        System.out.println("[Server] " + message);
    }
}
