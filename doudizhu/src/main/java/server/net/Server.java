 package server.net;
 
 import game.*;
 import game.action.ActionType;
 import game.GameResult;
 import game.action.GameAction;
 import game.state.PlayerState;
 import util.CardUtil;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 斗地主游戏服务端入口类。
  * <p>
  * 负责管理整个服务端的生命周期,包括:
  * <ul>
  *   <li>接收客户端连接</li>
  *   <li>创建游戏房间并发牌</li>
  *   <li>驱动游戏流程(叫地主/抢地主/出牌)</li>
  *   <li>处理玩家输入并广播结果</li>
  * </ul>
  * </p>
  * <p>
  * 当前实现为控制台版本,通过命令行输入输出进行游戏。
  * </p>
  */
 public class Server{
 
     /**
      * 已连接的玩家集合
      */
     private static final List<PlayerConnection> PLAYERS = new ArrayList<>();
 
     /**
      * 游戏流程对象
      */
     private static final GameFlow GAME_FLOW = new GameFlow();
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
     private static volatile GamePhase currentWaitingMessageType = null;
 
     /**
      * 服务端主方法。
      * <p>
      * 启动服务端并等待3个客户端连接,连接完成后自动开始游戏流程。
      * 游戏在端口8888上监听。
      * </p>
      *
      * @param args 命令行参数(未使用)
      */
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
      * 运行当前房间的主流程。
      * <p>
      * 职责：
      * 1. 持续读取当前可操作玩家；
      * 2. 在抢地主阶段对“叫地主阶段已选择不叫”的玩家执行自动 PASS；
      * 3. 等待正常玩家输入并交给 GameFlow 处理；
      * 4. 根据处理结果决定是否继续当前流程、重开或结束。
      * </p>
      */
     private static void runGameFlow() {
         while (true) {
             if (currentRoom == null) {
                 System.out.println("当前房间为空，流程结束");
                 return;
             }
 
             Integer playerId = currentRoom.getCurrentPlayerId();
             if (playerId == null) {
                 System.out.println("当前没有可操作玩家，流程结束");
                 return;
             }
 
             /*
               抢地主阶段自动 PASS：
               如果当前阶段是抢地主，并且当前玩家在“叫地主阶段已选择不叫”的列表中，
               那么该玩家本轮不需要再等待输入，直接自动执行 PASS。
              */
             if (currentRoom.getCurrentPhase() == GamePhase.ROB_LANDLORD
                     && currentRoom.getCallPassPlayerIds().contains(playerId)) {
 
                 GameAction autoPassAction = new GameAction(playerId, ActionType.PASS, null);
 
                 // 如果 handleGameResult 返回 false，说明当前流程应结束
                 if (!handleGameResult(playerId, autoPassAction)) {
                     return;
                 }
 
                 // 自动 PASS 完成后直接进入下一轮，重新读取新的 currentPlayerId
                 continue;
             }
 
             GamePhase messageType = currentRoom.getCurrentPhase();
             if (messageType == null) {
                 System.out.println("当前阶段没有对应提示类型，流程结束。当前阶段：" + currentRoom.getCurrentPhase());
                 return;
             }
 
             /*
               正常玩家输入流程：
               1. 给当前玩家发提示；
               2. 等待其输入；
               3. 解析输入为动作；
               4. 交给统一的动作处理逻辑。
              */
             Result result = waitPlayerAction(playerId, messageType);
             if (result == null) {
                 System.out.println("等待玩家输入失败，流程结束");
                 return;
             }
 
             System.out.println("收到玩家 " + result.getPlayerId() + " 输入：" + result.getMessage());
 
             ActionType actionType = ActionType.parseAction(result.getMessage(), messageType);
             GameAction action = new GameAction(result.getPlayerId(), actionType, null);
 
             System.out.println("阶段: " + currentRoom.getCurrentPhase());
             System.out.println("当前操作人: " + currentRoom.getCurrentPlayerId());
 
             // 如果 handleGameResult 返回 false，说明当前流程应结束
             if (!handleGameResult(playerId, action)) {
                 return;
             }
         }
     }
 
     /**
      * 统一处理一个玩家动作，并根据处理结果控制主循环。
      * <p>
      * 返回值语义：
      * true  -> 当前流程继续，runGameFlow 继续 while
      * false -> 当前流程结束，runGameFlow 应直接 return
      * </p>
      *
      * @param playerId 当前动作的发起玩家ID
      * @param action   当前要处理的动作
      * @return 是否继续当前流程
      */
     private static boolean handleGameResult(Integer playerId, GameAction action) {
         GameResult gameResult = GAME_FLOW.handlePlayerAction(currentRoom, action);
 
         if (gameResult == null) {
             System.out.println("动作处理返回空，流程结束");
             return false;
         }
 
         // 广播动作处理结果
         broadcastResult(playerId, gameResult);
 
         // 打印当前状态，便于调试
         System.out.println(gameResult.getMessage());
         System.out.println("处理后阶段: " + currentRoom.getCurrentPhase());
         System.out.println("处理后当前操作人: " + currentRoom.getCurrentPlayerId());
         System.out.println("处理后地主: " + currentRoom.getLandlordPlayerId());
         System.out.println("第一个叫地主ID: " + currentRoom.getFirstCallerId());
         System.out.println("不叫地主次数: " + currentRoom.getCallPassCount());
         System.out.println("----------");
 
         /*
           地主已确认：
           当前地主阶段流程结束，广播地主信息和底牌，并把地主最终手牌发给各玩家。
          */
         if (gameResult.getEventType() == GameEventType.LANDLORD_DECIDED) {
             broadcast("地主已确定: 玩家 " + currentRoom.getLandlordPlayerId());
             broadcast("地主底牌 " + CardUtil.cardsToString(currentRoom.getHoleCards()));
 
             sendOpeningHands(currentRoom);
             System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
             return false;
         }
 
         /*
           需要重开：
           重新发牌并广播重开消息，然后继续主循环。
          */
         if (gameResult.getEventType() == GameEventType.REDEAL_REQUIRED) {
             currentRoom = GAME_FLOW.reDeal(currentRoom);
             broadcast(gameResult.getMessage());
             sendOpeningHands(currentRoom);
             System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
             return true;
         }
 
         /*
           其他普通情况：
           当前流程继续。
          */
         return true;
     }
 
     /**
      * 按结果类型向玩家广播消息。
      * <p>
      * 规则：
      * 1. ACTION_ACCEPTED：
      *    - 操作者收到自己的私有提示（不带名字）
      *    - 其他玩家收到“玩家名 + 动作”的广播
      * 2. ACTION_REJECTED：
      *    - 只发给操作者
      * </p>
      *
      * @param playerId    动作发起玩家ID
      * @param gameResult  动作处理结果
      */
     private static void broadcastResult(Integer playerId, GameResult gameResult) {
         if (gameResult.getEventType() == GameEventType.ACTION_ACCEPTED) {
             // 操作者收到私有提示
             broadcast(playerId, gameResult.getMessage());
 
             // 其他玩家收到带操作者名字的提示
             if (playerId >= 1 && playerId <= PLAYERS.size()) {
                 broadcast(PLAYERS.get(playerId - 1).getName() + " " + gameResult.getMessage(), playerId);
             }
         } else if (gameResult.getEventType() == GameEventType.ACTION_REJECTED) {
             // 非法操作只提示操作者本人
             broadcast(playerId, gameResult.getMessage());
         }
     }
 
     /**
      * 根据房间当前阶段，决定本轮该给玩家发什么提示。
      * 这里只做阶段到消息类型的映射，不写业务规则。
      *
      * @param room 游戏房间对象
      * @return 对应的消息类型,如果无法映射则返回null
      */
     private static MessageType resolveCurrentMessageType(GameRoom room) {
         if (room == null || room.getCurrentPhase() == null) {
             return null;
         }
 
 
         switch (room.getCurrentPhase()) {
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
      *
      * @param serverSocket 服务端Socket
      * @param playerCount 需要接收的玩家数量
      * @throws IOException 如果接收连接时发生IO错误
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
      * @param room 游戏房间对象,包含玩家手牌信息
      */
     private static void sendOpeningHands(GameRoom room) {
         for (PlayerConnection connection : PLAYERS) {
             PlayerState playerState = room.getPlayerById(connection.getPlayerId());
             if (playerState == null) {
                 continue;
             }
             connection.send("你的手牌： " + CardUtil.cardsToString(playerState.getCards()));
         }
     }
 
     /**
      * 监听某个客户端发来的消息。
      * 这里只负责收输入，不做游戏规则判断。
      *
      * @param player 要监听的玩家连接对象
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
         } finally {
             PLAYERS.removeIf(p -> p.getPlayerId() == player.getPlayerId());
             System.out.println(player.getName() + " 已从房间移除");
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
      *
      * @param msg 要广播的消息内容
      */
     private static void broadcast(String msg) {
         for (PlayerConnection player : PLAYERS) {
             player.send(msg);
         }
     }
 
     /**
      * 给除自己以外的玩家广播消息。
      *
      * @param msg 要广播的消息内容
      * @param id 发送者玩家ID(不会收到此消息)
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
      *
      * @param id 接收消息的玩家ID
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
      * 根据消息类型，返回给客户端的提示文本。
      *
      * @param type 消息类型枚举
      * @return 对应的提示文本
      */
     public static String getMessage(GamePhase type) {
         switch (type) {
             case CALL_LANDLORD:
                 return "1.叫地主 2.不叫";
             case ROB_LANDLORD:
                 return "1.抢地主 2.不抢";

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
      *
      * @param playerId 等待输入的玩家ID
      * @param type     当前等待的消息类型
      * @return 玩家输入的结果对象,如果等待失败则返回null
      */
     public static Result waitPlayerAction(int playerId, GamePhase type) {
         synchronized (ACTION_LOCK) {
             // 设置当前轮到的玩家
             currentPlayerId = playerId;
 
             // 记录当前等待的消息类型
             currentWaitingMessageType = type;
 
             // 清空上一次残留结果
             pendingResult = null;
 
 
 
             // 通知所有玩家当前轮到谁
             broadcast("系统：当前轮到玩家 " + currentPlayerId + " 操作");
 
             // 只提示当前玩家输入
             broadcast(currentPlayerId, getMessage(type));
 
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
 }
