package server.service;

import server.model.DealResult;
import server.model.GameSession;
import server.model.PlayerHand;
import server.model.PlayerState;
import server.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GameService {

    public DealResult deal(List<String> playerNames) {
        validatePlayerNames(playerNames);

        List<Integer> shuffledDeck = CardUtil.createShuffledDeck();
        List<TreeSet<Integer>> playerCards = new ArrayList<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < playerNames.size(); i++) {
            playerCards.add(new TreeSet<>());
        }

        for (int i = 0; i < shuffledDeck.size(); i++) {
            Integer cardId = shuffledDeck.get(i);
            if (i < 3) {
                holeCards.add(cardId);
                continue;
            }

            int playerIndex = (i - 3) % playerNames.size();
            playerCards.get(playerIndex).add(cardId);
        }

        List<PlayerHand> playerHands = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            playerHands.add(new PlayerHand(i + 1, playerNames.get(i), playerCards.get(i)));
        }

        return new DealResult(playerHands, holeCards);
    }

    public GameSession startGame(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        List<PlayerState> players = new ArrayList<>();

        for (PlayerHand hand : dealResult.getPlayerHands()) {
            players.add(new PlayerState(
                    hand.getPlayerId(),
                    hand.getPlayerName(),
                    hand.getCards(),
                    false,
                    true
            ));
        }

        GameSession session = new GameSession(players, dealResult.getHoleCards());
        session.setGameStarted(true);
        session.setGameFinished(false);
        session.setCurrentTurnPlayerId(players.get(0).getPlayerId());
        return session;
    }

    private void validatePlayerNames(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != 3) {
            throw new IllegalArgumentException("斗地主必须有 3 个玩家");
        }

        for (String playerName : playerNames) {
            if (playerName == null || playerName.trim().isEmpty()) {
                throw new IllegalArgumentException("玩家名字不能为空");
            }
        }
    }
}
