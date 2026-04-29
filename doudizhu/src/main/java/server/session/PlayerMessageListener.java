package server.session;

import server.flow.InputAcceptance;
import server.flow.TurnInputCoordinator;

import java.net.SocketException;
import java.util.function.Consumer;

public class PlayerMessageListener implements Runnable {
    private final PlayerSession session;
    private final PlayerSessionRegistry registry;
    private final TurnInputCoordinator coordinator;
    private final Consumer<String> serverLogger;

    public PlayerMessageListener(PlayerSession session,
                                 PlayerSessionRegistry registry,
                                 TurnInputCoordinator coordinator,
                                 Consumer<String> serverLogger) {
        this.session = session;
        this.registry = registry;
        this.coordinator = coordinator;
        this.serverLogger = serverLogger;
    }

    @Override
    public void run() {
        boolean exitRequested = false;
        try {
            serverLogger.accept("开始处理客户端：玩家 " + session.getPlayerId());

            String message;
            while ((message = session.getReader().readLine()) != null) {
                message = message.trim();
                if ("exit".equalsIgnoreCase(message)) {
                    exitRequested = true;
                    serverLogger.accept(session.getPlayerName() + " 请求退出连接");
                    break;
                }
                System.out.println("[Input] 玩家 " + session.getPlayerId() + " 输入 = " + message);

                InputAcceptance acceptance = coordinator.submit(session.getPlayerId(), message);
                if (!acceptance.accepted()) {
                    session.send(acceptance.message());
                    serverLogger.accept("拒绝玩家输入：playerId=" + session.getPlayerId()
                            + "，原因=" + acceptance.message()
                            + "，currentPlayerId=" + coordinator.getCurrentPlayerId());
                }
            }

            if (!exitRequested) {
                serverLogger.accept(session.getPlayerName() + " 客户端正常关闭连接");
            }
        } catch (SocketException e) {
            serverLogger.accept(session.getPlayerName() + " 连接已断开：" + e.getMessage());
        } catch (Exception e) {
            serverLogger.accept(session.getPlayerName() + " 连接异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            registry.removeByPlayerId(session.getPlayerId());
            coordinator.handleDisconnect(session.getPlayerId());
            closeQuietly();
            serverLogger.accept(session.getPlayerName() + " 已从房间移除");
        }
    }

    private void closeQuietly() {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}
