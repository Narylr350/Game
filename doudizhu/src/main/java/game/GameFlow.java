package game;

import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GameFlow {

    public DealResult deal(List<String> playerNames) {
        validatePlayerNames(playerNames);

        List<Integer> shuffledDeck = CardUtil.createShuffledDeck();
        List<PlayerState> players = new ArrayList<>();
        List<TreeSet<Integer>> hands = new ArrayList<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < 3; i++) {
            players.add(new PlayerState(i + 1, playerNames.get(i), new TreeSet<>()));
            hands.add(new TreeSet<>());
        }

        for (int i = 0; i < shuffledDeck.size(); i++) {
            int cardId = shuffledDeck.get(i);
            if (i < 51) {
                hands.get(i % 3).add(cardId);
            } else {
                holeCards.add(cardId);
            }
        }

        List<PlayerState> dealtPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerState player = players.get(i);
            dealtPlayers.add(new PlayerState(player.getPlayerId(), player.getPlayerName(), hands.get(i)));
        }

        return new DealResult(dealtPlayers, holeCards);
    }

    public GameRoom startRoom(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        return new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
    }

    private void validatePlayerNames(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != 3) {
            throw new IllegalArgumentException("Exactly 3 player names are required");
        }

        for (String playerName : playerNames) {
            if (playerName == null || playerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Player names must be non-blank");
            }
        }
    }
}
