package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

// 表示当前这一局的最小运行状态：玩家、底牌、回合和地主归属。
public class GameRoom {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;
    private Integer currentTurnPlayerId;
    private Integer landlordPlayerId;
    private Integer lastHighestScorerId;
    private boolean gameStarted;
    private boolean gameFinished;
    private boolean landlordPhase;

    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }

    public Integer getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(Integer currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public Integer getLandlordPlayerId() {
        return landlordPlayerId;
    }

    // 允许传入 null，表示当前还没有确定地主，或需要重置地主状态。
    public void setLandlord(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
        for (PlayerState player : players) {
            player.setLandlord(landlordPlayerId != null && player.getPlayerId() == landlordPlayerId);
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(boolean gameFinished) {
        this.gameFinished = gameFinished;
    }

    // 网络层当前通过 playerId 在房间里找到连接对应的玩家。
    public PlayerState findPlayerById(int playerId) {
        for (PlayerState player : players) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }
}
