package server.net;

import game.GameFlow;
import game.GamePhase;
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

// 当前服务端入口只负责收连接、开一局并把手牌发出去。
public class Server {
    private static final List<PlayerConnection> PLAYERS = new ArrayList<>();
    private static final GameFlow GAME_FLOW = new GameFlow();
    // 现阶段只维护单局房间，先不引入额外的房间管理层。
    private static GameRoom currentRoom;

    public static void main(String[] args) {
        final int port = 8888;
        final int playerCount = 3;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，等待 " + playerCount + " 个客户端连接...");

            currentRoom.setPhase(GamePhase.WAITING);

            acceptPlayers(serverSocket, playerCount);

            System.out.println(playerCount + " 个客户端已全部连接，开始游戏...");
            currentRoom = GAME_FLOW.startRoom(collectPlayerNames());
            sendOpeningHands(currentRoom);

            broadcast("系统：发牌完成，游戏开始！");
            broadcast("系统：底牌已生成，等待后续抢地主逻辑接入。");
            System.out.println("系统：底牌已生成:" + CardUtil.cardsToString(currentRoom.getHoleCards()));
            //地主出现后发送底牌给所有人
            //broadcast("底牌:"+ CardUtil.cardsToString(currentRoom.getHoleCards()));
            //创建开始服务器线程
            startConsoleThread();

            for (PlayerConnection player : PLAYERS) {
                new Thread(() -> handleClient(player)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //接收发来的名字，将其分别创建对象PlayerConnection 放入PLAYERS集合
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

    // 按连接编号找到对应玩家，把各自手牌发回客户端。
    private static void sendOpeningHands(GameRoom room) {
        for (PlayerConnection connection : PLAYERS) {
            PlayerState playerState = room.findPlayerById(connection.getPlayerId());
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

    //发送消息给所有人
    private static void broadcast(String msg) {
        for (PlayerConnection player : PLAYERS) {
            player.send(msg);
        }
    }

    // 服务器将消息广播给除发送者(self)以外的所有玩家
    private static void broadcastOthers(PlayerConnection self, String msg) {
        for (PlayerConnection player : PLAYERS) {
            if (player != self) {
                player.send(msg);
            }
        }
    }
    //服务器发送给某一个人 通过id查找
    private static void sendMsgToPlayer(int id,String msg) {
        for (PlayerConnection player : PLAYERS) {
            if (player.getPlayerId() == id) {
                player.send(msg);
            }
        }
    }
    //定义各个消息
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

    //玩家发送的消息只传给服务器
//    private static Rseult returnServer(PlayerConnection self){
//
////        Rseult Rseult = new Rseult(self.getPlayerId(),);
//
//
//        return Rseult;
//    }




}
class Rseult{
    boolean finished;
    boolean readl;

    Integer currentID;//当前ID
    Integer nextPlayerID;//下一个玩家ID
    Integer landlordPlayerID;//地主ID

    String message;


}
