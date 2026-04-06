package game;

import game.handler.CallLanLordHandler;
import game.handler.RobLandLordHandler;
import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class GameFlow {
    private CallLanLordHandler callLanLord;
    private RobLandLordHandler robLandLord;
    //单例模式
    private static GameFlow gameFlow = new GameFlow();

    private GameFlow() {
        this.callLanLord = new CallLanLordHandler();
        this.robLandLord = new RobLandLordHandler();
    }

    public static GameFlow getInstance() {
        return gameFlow;
    }

    //动作处理
    public GameActionResult handlePlayerAction(GameRoom room, Integer playerId, ActionType actionType) {
        GamePhase currentPhase = room.getPhase();
        GameActionResult result = null;
        if (currentPhase == GamePhase.DEALING) {
            return GameActionResult.redeal("开始重新发牌");
        }
        if (currentPhase == GamePhase.CALL_LANDLORD){
            result = callLanLord.callLandLordHandler(room, playerId, actionType);
        }else if (currentPhase == GamePhase.ROB_LANDLORD){
            result = robLandLord.robLandLordHandler(room, playerId, actionType);
        }else if (currentPhase == GamePhase.PLAYING){

        }else {
            return GameActionResult.invalidAction("当前阶段禁止操作");
        }
        return result;
    }


    // 服务端当前通过这个入口拿到“已经开局的一局”。
    public GameRoom startRoom(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setPhase(GamePhase.DEALING);
        room = startCallLandLord(room);
        return room;
    }

    public GameRoom startCallLandLord(GameRoom room) {
        room.setPhase(GamePhase.CALL_LANDLORD);
        room.setLandLordId(null);
        room.setCurrentTurnPlayerId(new Random().nextInt(1, 4));
        return room;
    }

    //重新发牌
    public GameRoom reDeal(GameRoom oldRoom) {
        DealResult dealResult = deal(collectPlayerNames(oldRoom));
        GameRoom newRoom  = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        newRoom = startCallLandLord(newRoom);
        return newRoom;
    }

    // 名字校验要兼容中文输入法下的全角空格。
    private void validatePlayerNames(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != 3) {
            throw new IllegalArgumentException("需要且仅需要3个玩家名称");
        }

        for (String playerName : playerNames) {
            if (playerName == null || playerName.isBlank()) {
                throw new IllegalArgumentException("玩家名称不能为空");
            }
        }
    }

    private static List<String> collectPlayerNames(GameRoom room) {
        List<String> playerNames = new ArrayList<>();
        for (PlayerState player : room.getPlayers()) {
            playerNames.add(player.getPlayerName());
        }
        return playerNames;
    }

    // 开局发牌。
    public DealResult deal(List<String> playerNames) {
        validatePlayerNames(playerNames);

        List<Integer> shuffledDeck = CardUtil.createShuffledDeck();
        List<TreeSet<Integer>> hands = new ArrayList<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < 3; i++) {
            hands.add(new TreeSet<>());
        }

        for (int i = 0; i < 3; i++) {
            holeCards.add(shuffledDeck.get(i));
        }

        for (int i = 3; i < shuffledDeck.size(); i++) {
            hands.get((i - 3) % 3)
                    .add(shuffledDeck.get(i));
        }

        List<PlayerState> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new PlayerState(i + 1, playerNames.get(i), hands.get(i)));
        }

        return new DealResult(players, holeCards);
    }
}
