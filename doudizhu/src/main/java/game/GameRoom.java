package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

// 表示当前这一局的最小运行状态：玩家、底牌、回合和地主归属。
public class GameRoom {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;
    private boolean gameStarted;
    private boolean gameFinished;
    private boolean landlordPhase;          // 当前是否处于抢地主阶段
    private Integer currentTurnPlayerId;    // 当前轮到谁操作
    private Integer landlordPlayerId;       // 最终地主是谁，没确定前为 null
    private Integer lastHighestScorerId;    // 最后一个达到当前最高分的玩家
    private int highestLandlordScore;       // 当前抢地主阶段的最高分
    private int passCountAfterLastRaise;    // 自从“最后一次有人加分”后，连续有多少次没人再加分


    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public Integer setLastHighestScorerId(String message) {
        int lastHighestScorerId = 0;
        if ("抢".equals(message)){
            lastHighestScorerId = currentTurnPlayerId;
        }
        return this.lastHighestScorerId = lastHighestScorerId;
    }

    public Integer getLastHighestScorerId() {
        return lastHighestScorerId;
    }

    public Integer setPassCountAfterLastRaise() {
        return passCountAfterLastRaise++;
    }

    public Integer getPassCountAfterLastRaise() {
        return passCountAfterLastRaise;
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
