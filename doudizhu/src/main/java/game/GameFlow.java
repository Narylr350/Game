package game;

import game.action.ActionType;
import game.action.GameAction;
import game.handler.CallLandlordHandler;
import game.handler.RobLandlordHandler;
import game.state.PlayerState;
import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static rule.NameRule.validatePlayerNames;

public class GameFlow {

    private final CallLandlordHandler callLandlordHandler;
    private final RobLandlordHandler robLandlordHandler;

    public GameFlow() {
        this.callLandlordHandler = new CallLandlordHandler();
        this.robLandlordHandler = new RobLandlordHandler();
    }

    public GameActionResult handlePlayerAction(GameRoom room, GameAction action) {
        GamePhase currentPhase = room.getCurrentPhase();
        if (currentPhase == GamePhase.DEALING) {
            return GameActionResult.redeal("开始重新发牌");
        }

        if (currentPhase == GamePhase.CALL_LANDLORD) {
            return callLandlordHandler.handle(room, action);
        }

        if (currentPhase == GamePhase.ROB_LANDLORD) {
            return robLandlordHandler.handle(room, action);
        }

        if (currentPhase == GamePhase.PLAYING) {
            return GameActionResult.invalidAction("出牌阶段暂未实现");
        }

        return GameActionResult.invalidAction("当前阶段禁止操作");
    }

    public GameRoom startRoom(List<String> playerNames) {
        if (!validatePlayerNames(playerNames)) {
            throw new IllegalArgumentException("名字不合法");
        }

        DealResult dealResult = deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setCurrentPhase(GamePhase.DEALING);
        return startCallLandLord(room);
    }

    public GameRoom startCallLandLord(GameRoom room) {
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setLandlordPlayerId(null);
        room.setCurrentPlayerId(new Random().nextInt(1, 4));
        return room;
    }

    public GameRoom reDeal(GameRoom oldRoom) {
        DealResult dealResult = deal(collectPlayerNames(oldRoom));
        GameRoom newRoom = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        return startCallLandLord(newRoom);
    }

    public DealResult deal(List<String> playerNames) {
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
            hands.get((i - 3) % 3).add(shuffledDeck.get(i));
        }

        List<PlayerState> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new PlayerState(i + 1, playerNames.get(i), hands.get(i)));
        }

        return new DealResult(players, holeCards);
    }

    private static List<String> collectPlayerNames(GameRoom room) {
        List<String> playerNames = new ArrayList<>();
        for (PlayerState player : room.getPlayers()) {
            playerNames.add(player.getPlayerName());
        }
        return playerNames;
    }
}