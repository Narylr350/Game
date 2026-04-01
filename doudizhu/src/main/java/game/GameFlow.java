package game;

import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GameFlow {

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
            hands.get((i - 3) % 3).add(shuffledDeck.get(i));
        }

        List<PlayerState> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new PlayerState(i + 1, playerNames.get(i), hands.get(i)));
        }

        return new DealResult(players, holeCards);
    }

    public GameRoom startRoom(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setGameStarted(true);
        room.setGameFinished(false);
        return room;
    }

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
}
