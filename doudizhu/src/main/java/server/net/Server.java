package server.net;

import game.ActionType;
import game.GameActionResult;
import game.GameFlow;
import game.GameRoom;
import game.PlayerState;
import util.CardUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// 当前服务端入口：
// 1. 接收客户端连接
// 2. 创建房间并发牌
// 3. 等待当前玩家输入
// 4. 把输入交给外部已写好的逻辑处理
public class Server {

    /**
     * 已连接的玩家集合
     */
    private static final List<PlayerConnection> PLAYERS = new ArrayList<>();

    /**
     * 游戏流程对象
     */
    private static final GameFlow GAME_FLOW = GameFlow.getInstance();
    /**
     * 主流程等待输入、客户端线程提交输入，用同一把锁同步
     */
    private static final Object ACTION_LOCK = new Object();
    /**
     * 当前房间
     */
    private static GameRoom currentRoom;
    /**
     * 当前轮到操作的玩家ID，-1表示当前没有等待任何玩家输入
     */
    private static volatile int currentPlayerId = -1;
    /**
     * 当前等待到的玩家输入结果
     */
    private static volatile Result pendingResult = null;
    /**
     * 当前正在等待的消息类型：叫地主 / 抢地主 / 出牌
     */
    private static volatile MessageType currentWaitingMessageType = null;

    public static void main(String[] args) {
        final int port = 8888;
        final int playerCount = 3;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，等待 " + playerCount + " 个客户端连接...");

            // 接收玩家连接
            acceptPlayers(serverSocket, playerCount);

            System.out.println(playerCount + " 个客户端已全部连接，开始游戏...");

            // 创建房间
            currentRoom = GAME_FLOW.startRoom(collectPlayerNames());

            // 给每个玩家发送手牌
            sendOpeningHands(currentRoom);

            broadcast("系统：发牌完成，游戏开始！");
            System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));

            // 启动服务端控制台线程，可手动广播消息
            startConsoleThread();

            // 启动每个客户端的监听线程
            for (PlayerConnection player : PLAYERS) {
                new Thread(() -> handleClient(player)).start();
            }

            // 这里只保留流程入口，不写具体规则
            // 下面这个方法内部只负责：
            // 1. 按房间状态找到当前操作玩家
            // 2. 发提示
            // 3. 等输入
            // 4. 调用外部已经写好的处理逻辑
            runGameFlow();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主流程驱动入口。
     * <p>
     * 这里只做“流程调度”，不写规则判断。
     * 具体的叫地主 / 抢地主 / 出牌 / 结算逻辑，都预留给外部逻辑层处理。
     */
    private static void runGameFlow() {
        while (true) {





            if (currentRoom == null) {
                System.out.println("当前房间为空，流程结束");
                return;
            }

            Integer playerId = currentRoom.getCurrentTurnPlayerId();
            if (playerId == null) {
                System.out.println("当前没有可操作玩家，流程结束");
                return;
            }

            MessageType messageType = resolveCurrentMessageType(currentRoom);
            if (messageType == null) {
                System.out.println("当前阶段没有对应提示类型，流程结束。当前阶段：" + currentRoom.getPhase());
                return;
            }
//            MessageType messageType = currentWaitingMessageType;


            // 给当前玩家发提示，并等待他的输入
            Result result = waitPlayerAction(playerId, messageType);
            if (result == null) {
                System.out.println("等待玩家输入失败，流程结束");
                return;
            }


            System.out.println("收到玩家 " + result.getPlayerId() + " 输入：" + result.getMessage());

            // 输入转动作
            ActionType actionType = parseAction(result.getMessage(), messageType);
            if (actionType == null) {
                broadcast(result.getPlayerId(), "输入无效");
                continue;
            }

            System.out.println("阶段: " + currentRoom.getPhase());
            System.out.println("当前操作人: " + currentRoom.getCurrentTurnPlayerId());

            // 调用外部逻辑

            GameActionResult gameActionResult = GAME_FLOW.handlePlayerAction(
                    currentRoom,
                    result.getPlayerId(),
                    actionType
            );

            if (gameActionResult == null) {
                System.out.println("动作处理返回空，流程结束");
                return;
            }

            // 广播外部逻辑返回的消息
            if (gameActionResult.getDisplayMessage() != null && !gameActionResult.getDisplayMessage()
                    .isEmpty()) {
                broadcast(gameActionResult.getDisplayMessage());
            }

//            processingStatus(result,currentRoom);
            // 打印处理后的房间状态，确认有没有切到抢地主
            System.out.println(gameActionResult.getDisplayMessage());
            System.out.println("处理后阶段: " + currentRoom.getPhase());
            System.out.println("处理后当前操作人: " + currentRoom.getCurrentTurnPlayerId());
            System.out.println("处理后地主: " + currentRoom.getLandLordPlayerId());
            System.out.println("----------");

            // 地主已经确定，可以结束当前流程
            if (currentRoom.getLandLordPlayerId() != null) {
                broadcast("地主已确定: 玩家 " + currentRoom.getLandLordPlayerId());
                sendOpeningHands(currentRoom);
                System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
                return;
            }

            //重开
            if (gameActionResult.isNeedRedeal()){
                currentRoom = GAME_FLOW.reDeal(currentRoom);
                sendOpeningHands(currentRoom);
                System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));

            }

            // 不 return，不 break，继续 while
            // 下一轮会重新读取 currentRoom.getPhase()
            // 如果外部逻辑已改成 ROB_LANDLORD，就会自动给下一位发“抢地主”
        }
    }

    /**
     * 根据房间当前阶段，决定本轮该给玩家发什么提示。
     * 这里只做阶段到消息类型的映射，不写业务规则。
     */
    private static MessageType resolveCurrentMessageType(GameRoom room) {
        if (room == null || room.getPhase() == null) {
            return null;
        }


        switch (room.getPhase()) {
            case CALL_LANDLORD:
                return MessageType.CALL_LANDLORD;
            case ROB_LANDLORD:
                return MessageType.ROB_LANDLORD;
            case PLAYING:
                return MessageType.PLAY_CARD;
            default:
                return null;
        }
    }

    /**
     * 接收客户端连接，并创建 PlayerConnection 放进集合。
     */
    private static void acceptPlayers(ServerSocket serverSocket, int playerCount) throws IOException {
        while (PLAYERS.size() < playerCount) {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // 客户端连接后第一行默认发玩家名字
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

    /**
     * 收集玩家名称，用于开局创建房间。
     */
    private static List<String> collectPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (PlayerConnection player : PLAYERS) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }

    /**
     * 按连接编号找到对应玩家，把各自手牌发回客户端。
     */
    private static void sendOpeningHands(GameRoom room) {
        for (PlayerConnection connection : PLAYERS) {
            PlayerState playerState = room.findPlayerById(connection.getPlayerId());
            if (playerState == null) {
                continue;
            }
            connection.send("你的手牌： " + CardUtil.cardsToString(playerState.getCards()));
        }
    }

    /**
     * 监听某个客户端发来的消息。
     * 这里只负责收输入，不做游戏规则判断。
     */
    private static void handleClient(PlayerConnection player) {
        try {
            System.out.println("开始处理客户端 " + player.getPlayerId());

            String msg;
            while ((msg = player.getReader()
                    .readLine()) != null) {
                msg = msg.trim();
                System.out.println("收到玩家 " + player.getPlayerId() + " 输入：" + msg);

                synchronized (ACTION_LOCK) {
                    // 不是当前轮到的玩家，直接拒绝
                    if (player.getPlayerId() != currentPlayerId) {
                        player.send("现在还没轮到你操作");
                        continue;
                    }

                    // 空输入不处理
                    if (msg.isEmpty()) {
                        player.send("输入不能为空");
                        continue;
                    }

                    // 记录当前玩家输入
                    pendingResult = new Result(player.getPlayerId(), msg, currentWaitingMessageType);

                    // 唤醒等待中的主流程
                    ACTION_LOCK.notifyAll();
                }
            }

            System.out.println(player.getName() + " 客户端正常关闭连接");

        } catch (Exception e) {
            System.out.println(player.getName() + " 连接异常");
            e.printStackTrace();
        }
    }

    /**
     * 启动服务端控制台线程。
     * 你在服务端输入的内容会广播给所有客户端。
     */
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

    /**
     * 给所有玩家广播消息。
     */
    private static void broadcast(String msg) {
        for (PlayerConnection player : PLAYERS) {
            player.send(msg);
        }
    }

    /**
     * 给除自己以外的玩家广播消息。
     */
    private static void broadcast(String msg, int id) {
        for (PlayerConnection player : PLAYERS) {
            if (player.getPlayerId() != id) {
                player.send(msg);
            }
        }
    }

    /**
     * 给指定ID的玩家发消息。
     */
    private static void broadcast(int id, String msg) {
        for (PlayerConnection player : PLAYERS) {
            if (player.getPlayerId() == id) {
                player.send(msg);
                break;
            }
        }
    }

    /**
     * 根据消息类型，返回给客户端的提示文本。
     */
    public static String getMessage(MessageType type) {
        switch (type) {
            case CALL_LANDLORD:
                return "1.叫地主 2.不叫";
            case ROB_LANDLORD:
                return "1.抢地主 2.不抢";
            case PLAY_CARD:
                return "请输入要出的牌";
            case PASS:
                return "不出";
            default:
                return "";
        }
    }

    /**
     * 等待指定玩家输入。
     * 这里只负责：
     * 1. 指定当前轮到谁
     * 2. 发提示消息
     * 3. 阻塞等待该玩家输入
     * 4. 返回输入结果
     */
    public static Result waitPlayerAction(int playerId, MessageType type) {
        synchronized (ACTION_LOCK) {
            // 设置当前轮到的玩家
            currentPlayerId = playerId;

            // 记录当前等待的消息类型
            currentWaitingMessageType = type;

            // 清空上一次残留结果
            pendingResult = null;

            // 通知所有玩家当前轮到谁
            broadcast("系统：当前轮到玩家 " + playerId + " 操作");

            // 只提示当前玩家输入
            broadcast(playerId, getMessage(type));

            // 主线程阻塞等待，直到客户端线程提交结果
            while (pendingResult == null) {
                try {
                    ACTION_LOCK.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread()
                            .interrupt();
                    return null;
                }
            }

            // 拿到结果后，清理当前操作状态
            Result result = pendingResult;
            currentPlayerId = -1;
            currentWaitingMessageType = null;
            pendingResult = null;

            return result;
        }
    }

    private static ActionType parseAction(String input, MessageType type) {
        if (input == null || type == null) {
            return null;
        }

        input = input.trim();

        switch (type) {
            case CALL_LANDLORD:
                if ("1".equals(input) || "叫".equals(input) || "叫地主".equals(input)) {
                    return ActionType.CALL;
                }
                if ("2".equals(input) || "不叫".equals(input)) {
                    return ActionType.PASS;
                }
                break;

            case ROB_LANDLORD:
                if ("1".equals(input) || "抢".equals(input) || "抢地主".equals(input)) {
                    return ActionType.CALL;
                }
                if ("2".equals(input) || "不抢".equals(input)) {
                    return ActionType.PASS;
                }
                break;
        }

        return null;
    }
    //清空控制台
    public static void clearConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}