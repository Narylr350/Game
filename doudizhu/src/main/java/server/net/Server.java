package server.net;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GameEventType;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.LandlordState;
import game.state.PlayerState;
import util.CardUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 斗地主游戏服务端入口类。
 * <p>
 * 负责管理整个服务端的生命周期，包括：
 * <ul>
 *   <li>接收客户端连接</li>
 *   <li>创建游戏房间并发牌</li>
 *   <li>驱动游戏流程（叫地主 / 抢地主 / 出牌）</li>
 *   <li>处理玩家输入并广播结果</li>
 * </ul>
 * </p>
 */
public class Server {

    /** 已连接的玩家集合 */
    private static final List<PlayerConnection> PLAYERS = new ArrayList<>();

    /** 游戏流程对象 */
    private static final GameFlow GAME_FLOW = new GameFlow();

    /** 主流程等待输入、客户端线程提交输入，用同一把锁同步 */
    private static final Object ACTION_LOCK = new Object();

    /** 当前房间 */
    private static GameRoom currentRoom;

    /** 当前轮到操作的玩家ID，-1 表示当前没有等待任何玩家输入 */
    private static volatile int currentPlayerId = -1;

    /** 当前等待到的玩家输入结果 */
    private static volatile Result pendingResult = null;

    /** 当前正在等待的消息类型：叫地主 / 抢地主 / 出牌 */
    private static volatile GamePhase currentWaitingMessageType = null;

    /** 当前房间的地主阶段状态，重开或建房后需要重新取值 */
    private static LandlordState landlordState = null;

    /**
     * 服务端主方法。
     * <p>
     * 启动服务端并等待 3 个客户端连接，连接完成后自动开始游戏流程。
     * 游戏默认监听 8888 端口。
     * </p>
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        final int port = 8888;
        final int playerCount = 3;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logServer("服务器启动，等待 " + playerCount + " 个客户端连接...");

            // 阻塞等待所有玩家连接完成。
            acceptPlayers(serverSocket, playerCount);
            logServer(playerCount + " 个客户端已全部连接，开始游戏...");

            // 创建房间并记录初始地主状态，后续重开时也要同步刷新 landlordState。
            currentRoom = GAME_FLOW.startRoom(collectPlayerNames());
            landlordState = currentRoom.getLandlordState();
            logRoomState("房间创建完成");

            // 开局阶段只在服务端打印底牌，不提前发给客户端。
            logServer("底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));

            // 启动服务端控制台线程，可手动广播消息。
            startConsoleThread();

            // 每个客户端单独一个监听线程，只负责接收输入，不处理规则。
            for (PlayerConnection player : PLAYERS) {
                new Thread(() -> handleClient(player)).start();
            }

            // 主线程驱动游戏流程。
            runGameFlow();
        } catch (IOException e) {
            logServer("服务器启动或运行异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 运行当前房间主流程。
     * <p>
     * 这里只做流程编排：读取当前操作玩家、等待输入、组装动作、调用 GameFlow、处理结果。
     * 具体规则判断仍放在 GameFlow 和规则类中。
     * </p>
     */
    private static void runGameFlow() {
        landlordState = currentRoom.getLandlordState();

        //先发手牌
        sendOpeningHands(currentRoom);
        while (true) {
            if (currentRoom == null) {
                logServer("当前房间为空，流程结束");
                return;
            }

            Integer playerId = currentRoom.getCurrentPlayerId();
            if (playerId == null) {
                logServer("当前没有可操作玩家，流程结束");
                return;
            }
            // 这里每次重新获取阶段，而不是用旧变量
            GamePhase gamePhase = currentRoom.getCurrentPhase();

            // 只在每轮开始时打印必要状态，避免散落多处的临时调试输出。
            logTurnStart(playerId, gamePhase);


            //阻断主进程
            Result result = waitPlayerAction(playerId, gamePhase);
            if (result == null) {
                logServer("等待玩家输入失败，流程结束");
                return;
            }

            ActionType actionType = ActionType.parseAction(result.getMessage(), gamePhase);
            if (actionType == null) {
                broadcast(playerId, "输入无法识别，请重新输入");
                logServer("玩家 " + playerId + " 输入无法识别：" + result.getMessage());
                continue;
            }

            if (gamePhase == GamePhase.SETTLE){
                GameResult gameResult = GAME_FLOW.handlePlayerAction(currentRoom, null);
                broadcast(currentPlayerId,gameResult.getPlayerMessages().get(currentPlayerId));
                System.out.println("结束了");
                return;
            }
            GameAction action;
            if (gamePhase == GamePhase.PLAYING) {
                action = buildPlayingAction(playerId, actionType, result.getMessage());
                if (action == null) {
                    // 牌面解析失败时当前玩家需要重新输入，不推进流程。
                    continue;
                }

                GameResult gameResult = GAME_FLOW.handlePlayerAction(currentRoom, action);
                handlePlayingResult(playerId, action, gameResult);
            } else {

                action = new GameAction(result.getPlayerId(), actionType, null);

            }



            if (!handleGameResult(playerId, action)) {
                return;
            }
        }
    }

    /**
     * 构造出牌阶段动作。
     * <p>
     * PASS 不需要解析牌面；出牌时才把客户端输入转换为手牌 ID。
     * </p>
     *
     * @param playerId   当前玩家ID
     * @param actionType 当前动作类型
     * @param input      客户端原始输入
     * @return 可交给 GameFlow 的动作，解析失败时返回 null
     */
    private static GameAction buildPlayingAction(int playerId, ActionType actionType, String input) {
        if (actionType == ActionType.PASS_CARD) {
            return new GameAction(playerId, actionType, List.of());
        }

        PlayerState playerState = currentRoom.getPlayerById(playerId);
        if (playerState == null) {
            logServer("出牌失败：玩家不存在，playerId=" + playerId);
            return null;
        }

        try {
            List<Integer> cards = new ArrayList<>(CardUtil.stringToCards(input, playerState.getCards()));
            logPlayPreview(playerId, input, cards);
            return new GameAction(playerId, actionType, cards);
        } catch (IllegalArgumentException e) {
            broadcast(playerId, "输入无效：" + e.getMessage());
            logServer("玩家 " + playerId + " 出牌输入无效：" + e.getMessage());
            return null;
        }
    }

    /**
     * 处理出牌阶段返回结果。
     * <p>
     * 出牌阶段已经在 runGameFlow 中调用过 GameFlow，这里只负责广播和打印结果，
     * 避免后续 handleGameResult 再重复处理动作。
     * </p>
     *
     * @param playerId   当前玩家ID
     * @param action      客户端原始输入
     * @param gameResult GameFlow 返回结果
     */
    private static void handlePlayingResult(int playerId, GameAction action, GameResult gameResult) {
        if (gameResult == null) {
            logServer("出牌处理返回空，流程继续等待下一轮");
            return;
        }

        logGameResult(playerId, gameResult);

        PlayerState playerState = currentRoom.getPlayerById(playerId);
        if (playerState != null) {

            if (gameResult.getEventType() == GameEventType.ACTION_ACCEPTED) {
                Collection<Integer> playedCards = action.getCards();

                broadcast(
                        "玩家id:" + playerId
                                + " name:" + PLAYERS.get(playerId - 1).getName()
                                + "：\n" + CardUtil.cardsToString(playedCards),
                        playerId
                );

            } else if (gameResult.getEventType() == GameEventType.ACTION_REJECTED) {
                broadcast(playerId, gameResult.getMessage());
            }

            if (playerState.getCards().isEmpty()) {
                Set<Integer> set = gameResult.getPlayerMessages().keySet();
                for (Integer key : set) {
                    String val = gameResult.getPlayerMessages().get(key);
                    broadcast(key, val);
                }
            } else {
                broadcast(playerId, "你的手牌：\n" + CardUtil.cardsToString(playerState.getCards()));
                logPlayerCards(playerState, "玩家处理后手牌");
            }
        }

        logRoomState("出牌处理后");
    }

    /**
     * 统一处理叫地主 / 抢地主阶段动作，并根据结果控制主循环。
     * <p>
     * PLAYING 阶段已在 runGameFlow 中单独处理，这里不再重复调用 GameFlow。
     * </p>
     *
     * @param playerId 当前动作发起玩家ID
     * @param action   当前动作
     * @return true 表示继续流程，false 表示结束流程
     */
    private static boolean handleGameResult(Integer playerId, GameAction action) {
        GameResult gameResult = null;
        GamePhase phase = currentRoom.getCurrentPhase();

        if (phase == GamePhase.CALL_LANDLORD || phase == GamePhase.ROB_LANDLORD) {
            gameResult = GAME_FLOW.handlePlayerAction(currentRoom, action);

            if (gameResult == null) {
                logServer("动作处理返回空，流程结束");
                return false;
            }

            broadcastResult(playerId, gameResult);
            logGameResult(playerId, gameResult);
            logRoomState("叫抢地主处理后");
        }

        /*
         * 地主已确认：
         * 1. 广播地主信息和底牌；
         * 2. 给所有玩家重新发送手牌，地主手牌此时已经包含底牌；
         * 3. 后续进入出牌阶段。
         */
        if (gameResult != null && gameResult.getEventType() == GameEventType.LANDLORD_DECIDED) {
            broadcast("地主已确定：玩家 " + currentRoom.getLandlordPlayerId());
            broadcast("地主底牌：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
            sendOpeningHands(currentRoom);
            logLandlordState("地主确认后");
        }


        /*
         * 需要重开：
         * 三个玩家都不叫等场景会触发重新发牌，重开后必须刷新 landlordState 引用。
         */
        if (gameResult != null && gameResult.getEventType() == GameEventType.REDEAL_REQUIRED) {
            currentRoom = GAME_FLOW.reDeal(currentRoom);
            landlordState = currentRoom.getLandlordState();
            broadcast(gameResult.getMessage());
            sendOpeningHands(currentRoom);
            logRoomState("重新发牌后");
            return true;
        }

        return true;
    }

    /**
     * 按结果类型向玩家广播消息。
     * <p>
     * ACTION_ACCEPTED：操作者和其他玩家都会收到动作结果；
     * ACTION_REJECTED：只提示操作者本人。
     * </p>
     *
     * @param playerId   动作发起玩家ID
     * @param gameResult 动作处理结果
     */
    private static void broadcastResult(Integer playerId, GameResult gameResult) {
        if (gameResult.getEventType() == GameEventType.ACTION_ACCEPTED) {
            broadcast(gameResult.getMessage());

            if (playerId >= 1 && playerId <= PLAYERS.size()) {
                broadcast(PLAYERS.get(playerId - 1).getName() + " " + gameResult.getMessage(), playerId);
            }
        } else if (gameResult.getEventType() == GameEventType.ACTION_REJECTED) {
            broadcast(playerId, gameResult.getMessage());
        }
    }

    /**
     * 接收客户端连接，并创建 PlayerConnection 放进集合。
     *
     * @param serverSocket 服务端 Socket
     * @param playerCount  需要接收的玩家数量
     * @throws IOException 接收连接失败时抛出
     */
    private static void acceptPlayers(ServerSocket serverSocket, int playerCount) throws IOException {
        while (PLAYERS.size() < playerCount) {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // 客户端连接后第一行默认发玩家名字。
            String name = reader.readLine();
            int playerId = PLAYERS.size() + 1;

            PlayerConnection player = new PlayerConnection(playerId, name, socket, reader, writer);
            PLAYERS.add(player);

            logServer("第 " + playerId + " 个客户端已连接："
                    + socket.getInetAddress() + ":" + socket.getPort()
                    + "，名字：" + name);

            player.send("欢迎你，" + name + "，你的编号是：" + playerId);
        }
    }

    /**
     * 收集玩家名称，用于开局创建房间。
     *
     * @return 包含所有已连接玩家名称的列表
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
     *
     * @param room 游戏房间对象，包含玩家手牌信息
     */
    private static void sendOpeningHands(GameRoom room) {
        for (PlayerConnection connection : PLAYERS) {
            PlayerState playerState = room.getPlayerById(connection.getPlayerId());
            if (playerState == null) {
                continue;
            }
            connection.send("你的手牌：\n" + CardUtil.cardsToString(playerState.getCards()));
            logPlayerCards(playerState, "发送手牌");
        }
    }

    /**
     * 监听某个客户端发来的消息。
     * <p>
     * 客户端线程只负责收输入和唤醒主流程，不在这里做游戏规则判断。
     * </p>
     *
     * @param player 要监听的玩家连接对象
     */
    private static void handleClient(PlayerConnection player) {
        try {
            logServer("开始处理客户端：玩家 " + player.getPlayerId());

            String msg;
            while ((msg = player.getReader().readLine()) != null) {
                msg = msg.trim();
                logClientInput(player, msg);

                synchronized (ACTION_LOCK) {
                    // 不是当前轮到的玩家，直接拒绝，不唤醒主流程。
                    if (player.getPlayerId() != currentPlayerId) {
                        player.send("现在还没轮到你操作");
                        logServer("拒绝非当前玩家输入：playerId=" + player.getPlayerId()
                                + "，currentPlayerId=" + currentPlayerId);
                        continue;
                    }

                    // 记录当前玩家输入，并带上主流程正在等待的阶段。
                    pendingResult = new Result(player.getPlayerId(), msg, currentWaitingMessageType);

                    // 唤醒等待中的主流程。
                    ACTION_LOCK.notifyAll();
                }
            }

            logServer(player.getName() + " 客户端正常关闭连接");
        } catch (Exception e) {
            logServer(player.getName() + " 连接异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            PLAYERS.removeIf(p -> p.getPlayerId() == player.getPlayerId());
            logServer(player.getName() + " 已从房间移除");
        }
    }

    /**
     * 启动服务端控制台线程。
     * 服务端控制台输入的内容会广播给所有客户端。
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
                logServer("控制台线程异常：" + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 给所有玩家广播消息。
     *
     * @param msg 要广播的消息内容
     */
    private static void broadcast(String msg) {
        for (PlayerConnection player : PLAYERS) {
            player.send(msg);
        }
    }

    /**
     * 给除指定玩家以外的其他玩家广播消息。
     *
     * @param msg 要广播的消息内容
     * @param id  排除的玩家ID
     */
    private static void broadcast(String msg, int id) {
        for (PlayerConnection player : PLAYERS) {
            if (player.getPlayerId() != id) {
                player.send(msg);
            }
        }
    }

    /**
     * 给指定 ID 的玩家发消息。
     *
     * @param id  接收消息的玩家ID
     * @param msg 要发送的消息内容
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
     * 根据游戏阶段返回当前玩家的输入提示。
     *
     * @param type 当前阶段
     * @return 对应提示文本
     */
    public static String getMessage(GamePhase type) {
        switch (type) {
            case CALL_LANDLORD:
                return "1.叫地主 2.不叫";
            case ROB_LANDLORD:
                return "1.抢地主 2.不抢";
            case PLAYING:
                return "到你讲话，输入 PASS 或 空格 则过牌";
            default:
                return "";
        }
    }

    /**
     * 等待指定玩家输入。
     * <p>
     * 这里只负责：指定当前轮到谁、发提示、阻塞等待输入、返回输入结果。
     * </p>
     *
     * @param playerId 等待输入的玩家ID
     * @param type     当前等待的游戏阶段
     * @return 玩家输入结果；等待失败时返回 null
     */
    public static Result waitPlayerAction(int playerId, GamePhase type) {
        synchronized (ACTION_LOCK) {
            // 设置当前轮到的玩家。
            currentPlayerId = playerId;

            // 记录当前等待的阶段，客户端线程会把它写入 Result。
            currentWaitingMessageType = type;

            // 清空上一次残留结果，避免误用旧输入。
            pendingResult = null;

            // 通知所有玩家当前轮到谁，只给当前玩家发送输入提示。
            broadcast("系统：当前轮到玩家id:" + currentPlayerId +" name:"+PLAYERS.get(playerId - 1).getName()+ " 操作");
            broadcast(currentPlayerId, getMessage(type));

            // 主线程阻塞等待，直到客户端线程提交结果。
            while (pendingResult == null) {
                try {
                    ACTION_LOCK.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            // 拿到结果后清理当前操作状态。
            Result result = pendingResult;
            currentPlayerId = -1;
            currentWaitingMessageType = null;
            pendingResult = null;

            return result;
        }
    }

    /**
     * 服务端统一日志入口，方便后续替换为 slf4j 等日志框架。
     *
     * @param message 日志内容
     */
    private static void logServer(String message) {
        System.out.println("[Server] " + message);
    }

    /**
     * 打印每轮开始时的必要状态。
     *
     * @param playerId 当前操作玩家ID
     * @param phase    当前阶段
     */
    private static void logTurnStart(Integer playerId, GamePhase phase) {
        System.out.println("==== 当前轮到玩家 " + playerId + " ====");
        System.out.println("当前阶段 = " + phase);
        if (currentRoom != null) {
            System.out.println("地主玩家ID = " + currentRoom.getLandlordPlayerId());
        }
    }

    /**
     * 打印客户端输入。
     *
     * @param player 玩家连接
     * @param msg    输入内容
     */
    private static void logClientInput(PlayerConnection player, String msg) {
        System.out.println("[Input] 玩家 " + player.getPlayerId() + " 输入 = " + msg);
    }

    /**
     * 打印 GameFlow 处理结果，格式和 DebugMain 保持一致。
     *
     * @param playerId   动作玩家ID
     * @param gameResult 处理结果
     */
    private static void logGameResult(Integer playerId, GameResult gameResult) {
        System.out.println("==== 玩家" + playerId + " 动作处理结果 ====");
        System.out.println("是否成功 = " + gameResult.isSuccess());
        System.out.println("事件类型 = " + gameResult.getEventType());
        System.out.println("结果消息 = " + gameResult.getMessage());
        if (!gameResult.getPlayerMessages().isEmpty()) {
            System.out.println("玩家消息 = " + gameResult.getPlayerMessages());
        }
    }

    /**
     * 打印出牌阶段输入预览，便于核对客户端输入和服务端解析结果。
     *
     * @param playerId 玩家ID
     * @param input    原始输入
     * @param cards    解析后的牌ID
     */
    private static void logPlayPreview(int playerId, String input, List<Integer> cards) {
        System.out.println("==== 玩家" + playerId + " 出牌预览 ====");
        System.out.println("原始输入 = " + input);
        System.out.println("解析后牌ID = " + cards);
        System.out.println("解析后牌面 = " + CardUtil.cardsToString(cards));
    }

    /**
     * 打印房间关键状态，避免调试日志分散在流程各处。
     *
     * @param title 日志标题
     */
    private static void logRoomState(String title) {
        if (currentRoom == null) {
            logServer(title + "：currentRoom = null");
            return;
        }

        System.out.println("---- " + title + " ----");
        System.out.println("当前阶段 = " + currentRoom.getCurrentPhase());
        System.out.println("当前操作玩家ID = " + currentRoom.getCurrentPlayerId());
        System.out.println("地主玩家ID = " + currentRoom.getLandlordPlayerId());
        System.out.println("底牌 = " + CardUtil.cardsToString(currentRoom.getHoleCards()));

        if (landlordState != null) {
            System.out.println("首个叫地主玩家ID = " + landlordState.getFirstCallerId());
            System.out.println("当前地主候选人ID = " + landlordState.getLandlordCandidateId());
            System.out.println("不叫次数 = " + landlordState.getCallPassCount());
            System.out.println("叫地主阶段不叫玩家 = " + landlordState.getCallPassPlayerIds());
        }
        System.out.println();
    }

    /**
     * 打印地主阶段最终状态。
     *
     * @param title 日志标题
     */
    private static void logLandlordState(String title) {
        System.out.println("---- " + title + " ----");
        System.out.println("已确认地主ID = " + currentRoom.getLandlordPlayerId());
        System.out.println("首个叫地主玩家ID = " + landlordState.getFirstCallerId());
        System.out.println("当前地主候选人ID = " + landlordState.getLandlordCandidateId());
        System.out.println("不叫次数 = " + landlordState.getCallPassCount());
        System.out.println("底牌 = " + CardUtil.cardsToString(currentRoom.getHoleCards()));
        System.out.println();
    }

    /**
     * 打印玩家手牌。
     *
     * @param playerState 玩家状态
     * @param title       日志标题
     */
    private static void logPlayerCards(PlayerState playerState, String title) {
        System.out.println("[Cards] " + title + "：玩家" + playerState.getPlayerId()
                + " = " + CardUtil.cardsToString(playerState.getCards()));
    }
}
