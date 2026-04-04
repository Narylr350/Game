package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class GameRoom {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;
    private GamePhase phase;
    private Map<Integer, Integer> playerScores; //玩家分值
    private int highestScore;                   //最高分
    private int actionCount;                    //轮次计数器
    private Integer currentTurnPlayerId;        // 当前轮到谁操作
    private Integer landlordPlayerId;           // 最终地主是谁，没确定前为 null
    private Integer lastHighestScorerId;        // 最后一个达到1分的玩家
    private Integer notCalledCount;             //不抢玩家计算器

    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public Integer getNotCalledCount() {
        return notCalledCount;
    }

    public void setNotCalledCount(Integer notCalledCount) {
        this.notCalledCount = notCalledCount;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public Map<Integer, Integer> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(Map<Integer, Integer> playerScores) {
        this.playerScores = playerScores;
    }

    public Integer getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Integer highestScore) {
        this.highestScore = highestScore;
    }

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public void setLandlordPlayerId(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
    }

    public void setLastHighestScorerId(Integer lastHighestScorerId) {
        this.lastHighestScorerId = lastHighestScorerId;
    }

    public Integer getLastHighestScorerId() {
        return lastHighestScorerId;
    }
    //查询所有玩家
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
    public void setLandlordId(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
        for (PlayerState player : players) {
            player.setLandlord(landlordPlayerId != null && player.getPlayerId() == landlordPlayerId);
        }
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
