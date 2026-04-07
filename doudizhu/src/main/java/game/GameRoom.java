package game;

import game.state.PlayState;
import game.state.PlayerState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class GameRoom {

    // ===== 房间基础 =====
    private final List<PlayerState> playerStates;
    private final TreeSet<Integer> holeCards;
    private final List<Integer> callPassPlayerIds;
    // ===== 出牌阶段状态 =====
    private final PlayState playState;
    // ===== 流程状态 =====
    private GamePhase currentPhase;
    private Integer currentPlayerId;
    private Integer landlordPlayerId;
    // ===== 地主阶段状态 =====
    private int callPassCount;
    private Integer firstCallerId;
    private Integer landlordCandidateId;

    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.playerStates = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
        this.callPassPlayerIds = new ArrayList<>();
        this.playState = new PlayState();
    }

    // ===== phase =====
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GamePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    // ===== 玩家 =====
    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(playerStates);
    }

    public PlayerState getPlayerById(int playerId) {
        for (PlayerState player : playerStates) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }

    // ===== 当前轮到谁 =====
    public Integer getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Integer currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public Integer getLandlordPlayerId() {
        return landlordPlayerId;
    }

    // ===== 地主 =====
    public void setLandlordPlayerId(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
        for (PlayerState player : playerStates) {
            player.setLandlord(landlordPlayerId != null && player.getPlayerId() == landlordPlayerId);
        }
    }

    // ===== 地主阶段 =====
    public int getCallPassCount() {
        return callPassCount;
    }

    public void incrementCallPassCount() {
        callPassCount++;
    }

    public List<Integer> getCallPassPlayerIds() {
        return callPassPlayerIds;
    }

    public void addCallPassPlayerId(Integer playerId) {
        this.callPassPlayerIds.add(playerId);
    }

    public Integer getFirstCallerId() {
        return firstCallerId;
    }

    public void setFirstCallerId(Integer firstCallerId) {
        this.firstCallerId = firstCallerId;
    }

    public Integer getLandlordCandidateId() {
        return landlordCandidateId;
    }

    public void setLandlordCandidateId(Integer landlordCandidateId) {
        this.landlordCandidateId = landlordCandidateId;
    }

    // ===== 底牌 =====
    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }

    // ===== 出牌阶段 =====
    public PlayState getPlayState() {
        return playState;
    }
}