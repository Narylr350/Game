package game;

import game.handler.CallLanLordHandler;
import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class GameFlow {
    private GameRoom room;
    private GamePhase currentPhase;
    private CallLanLordHandler callLanLord;

    public GameFlow() {
        this.callLanLord = new CallLanLordHandler();
    }

    public ActionResult handlePlayerAction(GameRoom room,Integer playerId ,ActionType actionType) {
        currentPhase = room.getPhase();
        ActionResult result = null;
        if (currentPhase == GamePhase.CALL_LANDLORD || currentPhase == GamePhase.ROB_LANDLORD) {
            result = callLanLord.callLandLordHandler(room, playerId ,actionType);
        }
        return result;
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

    // 服务端当前通过这个入口拿到“已经开局的一局”。
    public GameRoom startRoom(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setPhase(GamePhase.DEALING);
        room = startCallLandLord(room);
        return room;
    }

    public GameRoom startCallLandLord(GameRoom room) {
        room.setPhase(GamePhase.CALL_LANDLORD);
        room.setLandlordId(null);
        room.setLandlordPlayerId(null);
        room.setHighestScore(0);
        room.addActionCount();
        room.setCurrentTurnPlayerId(new Random().nextInt(1, 4));
        return room;
    }

    //重新发牌
    public void reDeal(GameRoom room) {
        if (room.getPhase() != GamePhase.CALL_LANDLORD){
            DealResult dealResult = deal(collectPlayerNames(room));
            this.room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        }
    }

    public GameRoom getCurrentRoom() {
        return room;
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
}
