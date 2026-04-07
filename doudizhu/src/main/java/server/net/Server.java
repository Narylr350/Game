 package server.net; // ICERainbow666
  // ICERainbow666
 import game.*; // ICERainbow666
 import game.action.ActionType; // ICERainbow666
 import game.GameResult; // ICERainbow666
 import game.action.GameAction; // ICERainbow666
 import game.state.PlayerState; // ICERainbow666
 import util.CardUtil; // ICERainbow666
  // ICERainbow666
 import java.io.BufferedReader; // ICERainbow666
 import java.io.IOException; // ICERainbow666
 import java.io.InputStreamReader; // ICERainbow666
 import java.io.PrintWriter; // ICERainbow666
 import java.net.ServerSocket; // ICERainbow666
 import java.net.Socket; // ICERainbow666
 import java.util.ArrayList; // ICERainbow666
 import java.util.List; // ICERainbow666
  // ICERainbow666
 /** // ICERainbow666
  * 斗地主游戏服务端入口类。 // ICERainbow666
  * <p> // ICERainbow666
  * 负责管理整个服务端的生命周期,包括: // ICERainbow666
  * <ul> // ICERainbow666
  *   <li>接收客户端连接</li> // ICERainbow666
  *   <li>创建游戏房间并发牌</li> // ICERainbow666
  *   <li>驱动游戏流程(叫地主/抢地主/出牌)</li> // ICERainbow666
  *   <li>处理玩家输入并广播结果</li> // ICERainbow666
  * </ul> // ICERainbow666
  * </p> // ICERainbow666
  * <p> // ICERainbow666
  * 当前实现为控制台版本,通过命令行输入输出进行游戏。 // ICERainbow666
  * </p> // ICERainbow666
  */ // ICERainbow666
 public class Server{ // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 已连接的玩家集合 // ICERainbow666
      */ // ICERainbow666
     private static final List<PlayerConnection> PLAYERS = new ArrayList<>(); // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 游戏流程对象 // ICERainbow666
      */ // ICERainbow666
     private static final GameFlow GAME_FLOW = new GameFlow(); // ICERainbow666
     /** // ICERainbow666
      * 主流程等待输入、客户端线程提交输入，用同一把锁同步 // ICERainbow666
      */ // ICERainbow666
     private static final Object ACTION_LOCK = new Object(); // ICERainbow666
     /** // ICERainbow666
      * 当前房间 // ICERainbow666
      */ // ICERainbow666
     private static GameRoom currentRoom; // ICERainbow666
     /** // ICERainbow666
      * 当前轮到操作的玩家ID，-1表示当前没有等待任何玩家输入 // ICERainbow666
      */ // ICERainbow666
     private static volatile int currentPlayerId = -1; // ICERainbow666
     /** // ICERainbow666
      * 当前等待到的玩家输入结果 // ICERainbow666
      */ // ICERainbow666
     private static volatile Result pendingResult = null; // ICERainbow666
     /** // ICERainbow666
      * 当前正在等待的消息类型：叫地主 / 抢地主 / 出牌 // ICERainbow666
      */ // ICERainbow666
     private static volatile MessageType currentWaitingMessageType = null; // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 服务端主方法。 // ICERainbow666
      * <p> // ICERainbow666
      * 启动服务端并等待3个客户端连接,连接完成后自动开始游戏流程。 // ICERainbow666
      * 游戏在端口8888上监听。 // ICERainbow666
      * </p> // ICERainbow666
      * // ICERainbow666
      * @param args 命令行参数(未使用) // ICERainbow666
      */ // ICERainbow666
     public static void main(String[] args) { // ICERainbow666
         final int port = 8888; // ICERainbow666
         final int playerCount = 3; // ICERainbow666
  // ICERainbow666
         try (ServerSocket serverSocket = new ServerSocket(port)) { // ICERainbow666
             System.out.println("服务器启动，等待 " + playerCount + " 个客户端连接..."); // ICERainbow666
  // ICERainbow666
             // 接收玩家连接 // ICERainbow666
             acceptPlayers(serverSocket, playerCount); // ICERainbow666
  // ICERainbow666
             System.out.println(playerCount + " 个客户端已全部连接，开始游戏..."); // ICERainbow666
  // ICERainbow666
             // 创建房间 // ICERainbow666
             currentRoom = GAME_FLOW.startRoom(collectPlayerNames()); // ICERainbow666
  // ICERainbow666
             // 给每个玩家发送手牌 // ICERainbow666
             sendOpeningHands(currentRoom); // ICERainbow666
  // ICERainbow666
             broadcast("系统：发牌完成，游戏开始！"); // ICERainbow666
             System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards())); // ICERainbow666
  // ICERainbow666
             // 启动服务端控制台线程，可手动广播消息 // ICERainbow666
             startConsoleThread(); // ICERainbow666
  // ICERainbow666
             // 启动每个客户端的监听线程 // ICERainbow666
             for (PlayerConnection player : PLAYERS) { // ICERainbow666
                 new Thread(() -> handleClient(player)).start(); // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             // 这里只保留流程入口，不写具体规则 // ICERainbow666
             // 下面这个方法内部只负责： // ICERainbow666
             // 1. 按房间状态找到当前操作玩家 // ICERainbow666
             // 2. 发提示 // ICERainbow666
             // 3. 等输入 // ICERainbow666
             // 4. 调用外部已经写好的处理逻辑 // ICERainbow666
             runGameFlow(); // ICERainbow666
  // ICERainbow666
         } catch (IOException e) { // ICERainbow666
             e.printStackTrace(); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 运行当前房间的主流程。 // ICERainbow666
      * <p> // ICERainbow666
      * 职责： // ICERainbow666
      * 1. 持续读取当前可操作玩家； // ICERainbow666
      * 2. 在抢地主阶段对“叫地主阶段已选择不叫”的玩家执行自动 PASS； // ICERainbow666
      * 3. 等待正常玩家输入并交给 GameFlow 处理； // ICERainbow666
      * 4. 根据处理结果决定是否继续当前流程、重开或结束。 // ICERainbow666
      * </p> // ICERainbow666
      */ // ICERainbow666
     private static void runGameFlow() { // ICERainbow666
         while (true) { // ICERainbow666
             if (currentRoom == null) { // ICERainbow666
                 System.out.println("当前房间为空，流程结束"); // ICERainbow666
                 return; // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             Integer playerId = currentRoom.getCurrentPlayerId(); // ICERainbow666
             if (playerId == null) { // ICERainbow666
                 System.out.println("当前没有可操作玩家，流程结束"); // ICERainbow666
                 return; // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             /* // ICERainbow666
               抢地主阶段自动 PASS： // ICERainbow666
               如果当前阶段是抢地主，并且当前玩家在“叫地主阶段已选择不叫”的列表中， // ICERainbow666
               那么该玩家本轮不需要再等待输入，直接自动执行 PASS。 // ICERainbow666
              */ // ICERainbow666
             if (currentRoom.getCurrentPhase() == GamePhase.ROB_LANDLORD // ICERainbow666
                     && currentRoom.getCallPassPlayerIds().contains(playerId)) { // ICERainbow666
  // ICERainbow666
                 GameAction autoPassAction = new GameAction(playerId, ActionType.PASS, null); // ICERainbow666
  // ICERainbow666
                 // 如果 handleGameResult 返回 false，说明当前流程应结束 // ICERainbow666
                 if (!handleGameResult(playerId, autoPassAction)) { // ICERainbow666
                     return; // ICERainbow666
                 } // ICERainbow666
  // ICERainbow666
                 // 自动 PASS 完成后直接进入下一轮，重新读取新的 currentPlayerId // ICERainbow666
                 continue; // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             MessageType messageType = resolveCurrentMessageType(currentRoom); // ICERainbow666
             if (messageType == null) { // ICERainbow666
                 System.out.println("当前阶段没有对应提示类型，流程结束。当前阶段：" + currentRoom.getCurrentPhase()); // ICERainbow666
                 return; // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             /* // ICERainbow666
               正常玩家输入流程： // ICERainbow666
               1. 给当前玩家发提示； // ICERainbow666
               2. 等待其输入； // ICERainbow666
               3. 解析输入为动作； // ICERainbow666
               4. 交给统一的动作处理逻辑。 // ICERainbow666
              */ // ICERainbow666
             Result result = waitPlayerAction(playerId, messageType); // ICERainbow666
             if (result == null) { // ICERainbow666
                 System.out.println("等待玩家输入失败，流程结束"); // ICERainbow666
                 return; // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             System.out.println("收到玩家 " + result.getPlayerId() + " 输入：" + result.getMessage()); // ICERainbow666
  // ICERainbow666
             ActionType actionType = parseAction(result.getMessage(), messageType); // ICERainbow666
             GameAction action = new GameAction(result.getPlayerId(), actionType, null); // ICERainbow666
  // ICERainbow666
             System.out.println("阶段: " + currentRoom.getCurrentPhase()); // ICERainbow666
             System.out.println("当前操作人: " + currentRoom.getCurrentPlayerId()); // ICERainbow666
  // ICERainbow666
             // 如果 handleGameResult 返回 false，说明当前流程应结束 // ICERainbow666
             if (!handleGameResult(playerId, action)) { // ICERainbow666
                 return; // ICERainbow666
             } // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 统一处理一个玩家动作，并根据处理结果控制主循环。 // ICERainbow666
      * <p> // ICERainbow666
      * 返回值语义： // ICERainbow666
      * true  -> 当前流程继续，runGameFlow 继续 while // ICERainbow666
      * false -> 当前流程结束，runGameFlow 应直接 return // ICERainbow666
      * </p> // ICERainbow666
      * // ICERainbow666
      * @param playerId 当前动作的发起玩家ID // ICERainbow666
      * @param action   当前要处理的动作 // ICERainbow666
      * @return 是否继续当前流程 // ICERainbow666
      */ // ICERainbow666
     private static boolean handleGameResult(Integer playerId, GameAction action) { // ICERainbow666
         GameResult gameResult = GAME_FLOW.handlePlayerAction(currentRoom, action); // ICERainbow666
  // ICERainbow666
         if (gameResult == null) { // ICERainbow666
             System.out.println("动作处理返回空，流程结束"); // ICERainbow666
             return false; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
         // 广播动作处理结果 // ICERainbow666
         broadcastResult(playerId, gameResult); // ICERainbow666
  // ICERainbow666
         // 打印当前状态，便于调试 // ICERainbow666
         System.out.println(gameResult.getMessage()); // ICERainbow666
         System.out.println("处理后阶段: " + currentRoom.getCurrentPhase()); // ICERainbow666
         System.out.println("处理后当前操作人: " + currentRoom.getCurrentPlayerId()); // ICERainbow666
         System.out.println("处理后地主: " + currentRoom.getLandlordPlayerId()); // ICERainbow666
         System.out.println("第一个叫地主ID: " + currentRoom.getFirstCallerId()); // ICERainbow666
         System.out.println("不叫地主次数: " + currentRoom.getCallPassCount()); // ICERainbow666
         System.out.println("----------"); // ICERainbow666
  // ICERainbow666
         /* // ICERainbow666
           地主已确认： // ICERainbow666
           当前地主阶段流程结束，广播地主信息和底牌，并把地主最终手牌发给各玩家。 // ICERainbow666
          */ // ICERainbow666
         if (gameResult.getEventType() == GameEventType.LANDLORD_DECIDED) { // ICERainbow666
             broadcast("地主已确定: 玩家 " + currentRoom.getLandlordPlayerId()); // ICERainbow666
             broadcast("地主底牌 " + CardUtil.cardsToString(currentRoom.getHoleCards())); // ICERainbow666
  // ICERainbow666
             sendOpeningHands(currentRoom); // ICERainbow666
             System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards())); // ICERainbow666
             return false; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
         /* // ICERainbow666
           需要重开： // ICERainbow666
           重新发牌并广播重开消息，然后继续主循环。 // ICERainbow666
          */ // ICERainbow666
         if (gameResult.getEventType() == GameEventType.REDEAL_REQUIRED) { // ICERainbow666
             currentRoom = GAME_FLOW.reDeal(currentRoom); // ICERainbow666
             broadcast(gameResult.getMessage()); // ICERainbow666
             sendOpeningHands(currentRoom); // ICERainbow666
             System.out.println("系统：底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards())); // ICERainbow666
             return true; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
         /* // ICERainbow666
           其他普通情况： // ICERainbow666
           当前流程继续。 // ICERainbow666
          */ // ICERainbow666
         return true; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 按结果类型向玩家广播消息。 // ICERainbow666
      * <p> // ICERainbow666
      * 规则： // ICERainbow666
      * 1. ACTION_ACCEPTED： // ICERainbow666
      *    - 操作者收到自己的私有提示（不带名字） // ICERainbow666
      *    - 其他玩家收到“玩家名 + 动作”的广播 // ICERainbow666
      * 2. ACTION_REJECTED： // ICERainbow666
      *    - 只发给操作者 // ICERainbow666
      * </p> // ICERainbow666
      * // ICERainbow666
      * @param playerId    动作发起玩家ID // ICERainbow666
      * @param gameResult  动作处理结果 // ICERainbow666
      */ // ICERainbow666
     private static void broadcastResult(Integer playerId, GameResult gameResult) { // ICERainbow666
         if (gameResult.getEventType() == GameEventType.ACTION_ACCEPTED) { // ICERainbow666
             // 操作者收到私有提示 // ICERainbow666
             broadcast(playerId, gameResult.getMessage()); // ICERainbow666
  // ICERainbow666
             // 其他玩家收到带操作者名字的提示 // ICERainbow666
             if (playerId >= 1 && playerId <= PLAYERS.size()) { // ICERainbow666
                 broadcast(PLAYERS.get(playerId - 1).getName() + " " + gameResult.getMessage(), playerId); // ICERainbow666
             } // ICERainbow666
         } else if (gameResult.getEventType() == GameEventType.ACTION_REJECTED) { // ICERainbow666
             // 非法操作只提示操作者本人 // ICERainbow666
             broadcast(playerId, gameResult.getMessage()); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 根据房间当前阶段，决定本轮该给玩家发什么提示。 // ICERainbow666
      * 这里只做阶段到消息类型的映射，不写业务规则。 // ICERainbow666
      * // ICERainbow666
      * @param room 游戏房间对象 // ICERainbow666
      * @return 对应的消息类型,如果无法映射则返回null // ICERainbow666
      */ // ICERainbow666
     private static MessageType resolveCurrentMessageType(GameRoom room) { // ICERainbow666
         if (room == null || room.getCurrentPhase() == null) { // ICERainbow666
             return null; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
  // ICERainbow666
         switch (room.getCurrentPhase()) { // ICERainbow666
             case CALL_LANDLORD: // ICERainbow666
                 return MessageType.CALL_LANDLORD; // ICERainbow666
             case ROB_LANDLORD: // ICERainbow666
                 return MessageType.ROB_LANDLORD; // ICERainbow666
             case PLAYING: // ICERainbow666
                 return MessageType.PLAY_CARD; // ICERainbow666
             default: // ICERainbow666
                 return null; // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 接收客户端连接，并创建 PlayerConnection 放进集合。 // ICERainbow666
      * // ICERainbow666
      * @param serverSocket 服务端Socket // ICERainbow666
      * @param playerCount 需要接收的玩家数量 // ICERainbow666
      * @throws IOException 如果接收连接时发生IO错误 // ICERainbow666
      */ // ICERainbow666
     private static void acceptPlayers(ServerSocket serverSocket, int playerCount) throws IOException { // ICERainbow666
         while (PLAYERS.size() < playerCount) { // ICERainbow666
             Socket socket = serverSocket.accept(); // ICERainbow666
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // ICERainbow666
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true); // ICERainbow666
  // ICERainbow666
             // 客户端连接后第一行默认发玩家名字 // ICERainbow666
             String name = reader.readLine(); // ICERainbow666
             int playerId = PLAYERS.size() + 1; // ICERainbow666
  // ICERainbow666
             PlayerConnection player = new PlayerConnection(playerId, name, socket, reader, writer); // ICERainbow666
             PLAYERS.add(player); // ICERainbow666
  // ICERainbow666
             System.out.println("第 " + playerId + " 个客户端已连接：" // ICERainbow666
                     + socket.getInetAddress() + ":" + socket.getPort() // ICERainbow666
                     + "，名字：" + name); // ICERainbow666
  // ICERainbow666
             player.send("欢迎你，" + name + "，你的编号是：" + playerId); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 收集玩家名称，用于开局创建房间。 // ICERainbow666
      * // ICERainbow666
      * @return 包含所有已连接玩家名称的列表 // ICERainbow666
      */ // ICERainbow666
     private static List<String> collectPlayerNames() { // ICERainbow666
         List<String> playerNames = new ArrayList<>(); // ICERainbow666
         for (PlayerConnection player : PLAYERS) { // ICERainbow666
             playerNames.add(player.getName()); // ICERainbow666
         } // ICERainbow666
         return playerNames; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 按连接编号找到对应玩家，把各自手牌发回客户端。 // ICERainbow666
      * // ICERainbow666
      * @param room 游戏房间对象,包含玩家手牌信息 // ICERainbow666
      */ // ICERainbow666
     private static void sendOpeningHands(GameRoom room) { // ICERainbow666
         for (PlayerConnection connection : PLAYERS) { // ICERainbow666
             PlayerState playerState = room.getPlayerById(connection.getPlayerId()); // ICERainbow666
             if (playerState == null) { // ICERainbow666
                 continue; // ICERainbow666
             } // ICERainbow666
             connection.send("你的手牌： " + CardUtil.cardsToString(playerState.getCards())); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 监听某个客户端发来的消息。 // ICERainbow666
      * 这里只负责收输入，不做游戏规则判断。 // ICERainbow666
      * // ICERainbow666
      * @param player 要监听的玩家连接对象 // ICERainbow666
      */ // ICERainbow666
     private static void handleClient(PlayerConnection player) { // ICERainbow666
         try { // ICERainbow666
             System.out.println("开始处理客户端 " + player.getPlayerId()); // ICERainbow666
  // ICERainbow666
             String msg; // ICERainbow666
             while ((msg = player.getReader() // ICERainbow666
                     .readLine()) != null) { // ICERainbow666
                 msg = msg.trim(); // ICERainbow666
                 System.out.println("收到玩家 " + player.getPlayerId() + " 输入：" + msg); // ICERainbow666
  // ICERainbow666
                 synchronized (ACTION_LOCK) { // ICERainbow666
                     // 不是当前轮到的玩家，直接拒绝 // ICERainbow666
                     if (player.getPlayerId() != currentPlayerId) { // ICERainbow666
                         player.send("现在还没轮到你操作"); // ICERainbow666
                         continue; // ICERainbow666
                     } // ICERainbow666
  // ICERainbow666
                     // 记录当前玩家输入 // ICERainbow666
                     pendingResult = new Result(player.getPlayerId(), msg, currentWaitingMessageType); // ICERainbow666
  // ICERainbow666
                     // 唤醒等待中的主流程 // ICERainbow666
                     ACTION_LOCK.notifyAll(); // ICERainbow666
                 } // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             System.out.println(player.getName() + " 客户端正常关闭连接"); // ICERainbow666
  // ICERainbow666
         } catch (Exception e) { // ICERainbow666
             System.out.println(player.getName() + " 连接异常"); // ICERainbow666
             e.printStackTrace(); // ICERainbow666
         } finally { // ICERainbow666
             PLAYERS.removeIf(p -> p.getPlayerId() == player.getPlayerId()); // ICERainbow666
             System.out.println(player.getName() + " 已从房间移除"); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 启动服务端控制台线程。 // ICERainbow666
      * 你在服务端输入的内容会广播给所有客户端。 // ICERainbow666
      */ // ICERainbow666
     private static void startConsoleThread() { // ICERainbow666
         new Thread(() -> { // ICERainbow666
             try { // ICERainbow666
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in)); // ICERainbow666
                 String input; // ICERainbow666
                 while ((input = console.readLine()) != null) { // ICERainbow666
                     broadcast("服务器：" + input); // ICERainbow666
                 } // ICERainbow666
             } catch (Exception e) { // ICERainbow666
                 e.printStackTrace(); // ICERainbow666
             } // ICERainbow666
         }).start(); // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 给所有玩家广播消息。 // ICERainbow666
      * // ICERainbow666
      * @param msg 要广播的消息内容 // ICERainbow666
      */ // ICERainbow666
     private static void broadcast(String msg) { // ICERainbow666
         for (PlayerConnection player : PLAYERS) { // ICERainbow666
             player.send(msg); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 给除自己以外的玩家广播消息。 // ICERainbow666
      * // ICERainbow666
      * @param msg 要广播的消息内容 // ICERainbow666
      * @param id 发送者玩家ID(不会收到此消息) // ICERainbow666
      */ // ICERainbow666
     private static void broadcast(String msg, int id) { // ICERainbow666
         for (PlayerConnection player : PLAYERS) { // ICERainbow666
             if (player.getPlayerId() != id) { // ICERainbow666
                 player.send(msg); // ICERainbow666
             } // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 给指定ID的玩家发消息。 // ICERainbow666
      * // ICERainbow666
      * @param id 接收消息的玩家ID // ICERainbow666
      * @param msg 要发送的消息内容 // ICERainbow666
      */ // ICERainbow666
     private static void broadcast(int id, String msg) { // ICERainbow666
         for (PlayerConnection player : PLAYERS) { // ICERainbow666
             if (player.getPlayerId() == id) { // ICERainbow666
                 player.send(msg); // ICERainbow666
                 break; // ICERainbow666
             } // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 根据消息类型，返回给客户端的提示文本。 // ICERainbow666
      * // ICERainbow666
      * @param type 消息类型枚举 // ICERainbow666
      * @return 对应的提示文本 // ICERainbow666
      */ // ICERainbow666
     public static String getMessage(MessageType type) { // ICERainbow666
         switch (type) { // ICERainbow666
             case CALL_LANDLORD: // ICERainbow666
                 return "1.叫地主 2.不叫"; // ICERainbow666
             case ROB_LANDLORD: // ICERainbow666
                 return "1.抢地主 2.不抢"; // ICERainbow666
             case PLAY_CARD: // ICERainbow666
                 return "请输入要出的牌"; // ICERainbow666
             case PASS: // ICERainbow666
                 return "不出"; // ICERainbow666
             default: // ICERainbow666
                 return ""; // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 等待指定玩家输入。 // ICERainbow666
      * 这里只负责： // ICERainbow666
      * 1. 指定当前轮到谁 // ICERainbow666
      * 2. 发提示消息 // ICERainbow666
      * 3. 阻塞等待该玩家输入 // ICERainbow666
      * 4. 返回输入结果 // ICERainbow666
      * // ICERainbow666
      * @param playerId 等待输入的玩家ID // ICERainbow666
      * @param type     当前等待的消息类型 // ICERainbow666
      * @return 玩家输入的结果对象,如果等待失败则返回null // ICERainbow666
      */ // ICERainbow666
     public static Result waitPlayerAction(int playerId, MessageType type) { // ICERainbow666
         synchronized (ACTION_LOCK) { // ICERainbow666
             // 设置当前轮到的玩家 // ICERainbow666
             currentPlayerId = playerId; // ICERainbow666
  // ICERainbow666
             // 记录当前等待的消息类型 // ICERainbow666
             currentWaitingMessageType = type; // ICERainbow666
  // ICERainbow666
             // 清空上一次残留结果 // ICERainbow666
             pendingResult = null; // ICERainbow666
  // ICERainbow666
  // ICERainbow666
  // ICERainbow666
             // 通知所有玩家当前轮到谁 // ICERainbow666
             broadcast("系统：当前轮到玩家 " + currentPlayerId + " 操作"); // ICERainbow666
  // ICERainbow666
             // 只提示当前玩家输入 // ICERainbow666
             broadcast(currentPlayerId, getMessage(type)); // ICERainbow666
  // ICERainbow666
             // 主线程阻塞等待，直到客户端线程提交结果 // ICERainbow666
             while (pendingResult == null) { // ICERainbow666
                 try { // ICERainbow666
                     ACTION_LOCK.wait(); // ICERainbow666
                 } catch (InterruptedException e) { // ICERainbow666
                     Thread.currentThread() // ICERainbow666
                             .interrupt(); // ICERainbow666
                     return null; // ICERainbow666
                 } // ICERainbow666
             } // ICERainbow666
  // ICERainbow666
             // 拿到结果后，清理当前操作状态 // ICERainbow666
             Result result = pendingResult; // ICERainbow666
             currentPlayerId = -1; // ICERainbow666
             currentWaitingMessageType = null; // ICERainbow666
             pendingResult = null; // ICERainbow666
  // ICERainbow666
             return result; // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 将玩家输入的字符串解析为对应的操作类型。 // ICERainbow666
      * // ICERainbow666
      * @param input 玩家输入的字符串 // ICERainbow666
      * @param type 当前阶段的消息类型 // ICERainbow666
      * @return 解析后的操作类型,如果输入无效则返回null // ICERainbow666
      */ // ICERainbow666
     private static ActionType parseAction(String input, MessageType type) { // ICERainbow666
         if (input == null || type == null) { // ICERainbow666
             return null; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
         input = input.trim(); // ICERainbow666
  // ICERainbow666
         switch (type) { // ICERainbow666
             case CALL_LANDLORD: // ICERainbow666
                 if ("1".equals(input) || "叫".equals(input) || "叫地主".equals(input)) { // ICERainbow666
                     return ActionType.CALL; // ICERainbow666
                 } // ICERainbow666
                 if ("2".equals(input) || "不叫".equals(input)) { // ICERainbow666
                     return ActionType.PASS; // ICERainbow666
                 } // ICERainbow666
                 break; // ICERainbow666
  // ICERainbow666
             case ROB_LANDLORD: // ICERainbow666
                 if ("1".equals(input) || "抢".equals(input) || "抢地主".equals(input)) { // ICERainbow666
                     return ActionType.CALL; // ICERainbow666
                 } // ICERainbow666
                 if ("2".equals(input) || "不抢".equals(input)) { // ICERainbow666
                     return ActionType.PASS; // ICERainbow666
                 } // ICERainbow666
                 break; // ICERainbow666
         } // ICERainbow666
  // ICERainbow666
         return null; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 清空控制台。 // ICERainbow666
      * <p> // ICERainbow666
      * 使用系统命令cls来清空控制台输出(仅Windows系统)。 // ICERainbow666
      * </p> // ICERainbow666
      */ // ICERainbow666
     public static void clearConsole() { // ICERainbow666
         try { // ICERainbow666
             new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor(); // ICERainbow666
         } catch (Exception e) { // ICERainbow666
             e.printStackTrace(); // ICERainbow666
         } // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
 } // ICERainbow666
