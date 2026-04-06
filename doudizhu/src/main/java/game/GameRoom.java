package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class GameRoom {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;
    private GamePhase phase;
    //叫/抢地主阶段所需字段
    private int passCount;                      // 不抢玩家计数器
    private List<Integer> passPlayerId;         //不抢玩家id集合
    private Integer firstCallerId;              //第一个叫地主
    private Integer landLordCandidateId;        // 地主候选人
    private Integer currentTurnPlayerId;        // 当前轮到谁操作
    private Integer landLordPlayerId;           // 最终地主

    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
        passPlayerId = new ArrayList<>();
    }

    // 获取当前游戏阶段
    public GamePhase getPhase() {
        return phase;
    }

    // 查询所有玩家
    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    // 设置玩家地主通过ID
    public void setLandLordId(Integer landLordPlayerId) {
        this.landLordPlayerId = landLordPlayerId;
        for (PlayerState player : players) {
            player.setLandlord(landLordPlayerId != null && player.getPlayerId() == landLordPlayerId);
        }
    }

    // 通过 playerId 找到对应的玩家。
    public PlayerState findPlayerById(int playerId) {
        for (PlayerState player : players) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }

    public int getPassCount() {
        return passCount++;
    }

    public void addPassCount() {
        passCount++;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
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

    public Integer getLandLordCandidateId() {
        return landLordCandidateId;
    }

    public void setLandLordCandidateId(Integer landLordCandidateId) {
        this.landLordCandidateId = landLordCandidateId;
    }

    public List<Integer> getPassPlayerId() {
        return passPlayerId;
    }

    public void addPassPlayerId(Integer passPlayerId) {
        this.passPlayerId.add(passPlayerId);
    }

    public Integer getLandLordPlayerId() {
        return landLordPlayerId;
    }

    public Integer getFirstCallerId() {
        return firstCallerId;
    }

    public void setFirstCallerId(Integer firstCallerId) {
        this.firstCallerId = firstCallerId;
    }
}
