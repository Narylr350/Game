package game;

import game.state.PlayState;
import game.state.PlayerState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 游戏房间类。
 * <p>
 * 封装一局游戏的所有状态，包括玩家、手牌、底牌、当前阶段和流程状态。
 * 该类是游戏的核心数据容器，所有阶段流转都通过修改此类状态完成。
 * </p>
 */
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
    private int robPassCount;
    private Integer firstCallerId;
    private Integer landlordCandidateId;

    /**
     * 创建游戏房间。
     *
     * @param players 玩家状态列表
     * @param holeCards 底牌集合
     */
    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.playerStates = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
        this.callPassPlayerIds = new CopyOnWriteArrayList<>();
        this.playState = new PlayState();
    }

    /**
     * 获取当前游戏阶段。
     *
     * @return 当前游戏阶段枚举
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * 设置当前游戏阶段。
     *
     * @param currentPhase 要设置的游戏阶段
     */
    public void setCurrentPhase(GamePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    /**
     * 获取所有玩家状态的只读列表。
     *
     * @return 不可修改的玩家状态列表
     */
    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(playerStates);
    }

    /**
     * 根据玩家ID查找玩家状态。
     *
     * @param playerId 玩家ID
     * @return 找到的玩家状态，未找到返回null
     */
    public PlayerState getPlayerById(int playerId) {
        for (PlayerState player : playerStates) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }

    /**
     * 获取当前轮到操作的玩家ID。
     *
     * @return 当前玩家ID
     */
    public Integer getCurrentPlayerId() {
        return currentPlayerId;
    }

    /**
     * 设置当前轮到操作的玩家ID。
     *
     * @param currentPlayerId 玩家ID
     */
    public void setCurrentPlayerId(Integer currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    /**
     * 获取地主玩家ID。
     *
     * @return 地主玩家ID
     */
    public Integer getLandlordPlayerId() {
        return landlordPlayerId;
    }

    /**
     * 设置地主玩家ID，并同步更新所有玩家的地主状态。
     *
     * @param landlordPlayerId 地主玩家ID
     */
    public void setLandlordPlayerId(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
        for (PlayerState player : playerStates) {
            player.setLandlord(landlordPlayerId != null && player.getPlayerId() == landlordPlayerId);
        }
    }

    /**
     * 获取叫地主阶段不叫的次数。
     *
     * @return 不叫次数
     */
    public int getCallPassCount() {
        return callPassCount;
    }

    /**
     * 获取抢地主阶段不抢的次数。
     *
     * @return 不抢次数
     */
    public int getRobPassCount(){
        return robPassCount;
    }

    /**
     * 重置叫地主阶段的不叫次数。
     */
    public void resetCallPassCount() {
        this.callPassCount = 0;
    }

    /**
     * 清空叫地主阶段不叫的玩家ID列表。
     */
    public void resetCallPasserId() {
        callPassPlayerIds.clear();
    }

    /**
     * 重置第一个叫地主的玩家ID。
     */
    public void resetFirstCallerId() {
        firstCallerId = null;
    }

    /**
     * 重置抢地主阶段的不抢次数。
     */
    public void resetRobPassCount() {
        robPassCount = 0;
    }
    /**
     * 重置地主候选人ID。
     */
    public void resetLandlordCandidateId() {
        this.landlordCandidateId = null;
    }

    /**
     * 叫地主不叫次数加1。
     */
    public void incrementCallPassCount() {
        callPassCount++;
    }

    /**
     * 抢地主不抢次数加1。
     */
    public void incrementRobPassCount(){
        robPassCount++;
    }
    /**
     * 获取叫地主阶段不叫的玩家ID列表。
     *
     * @return 不叫的玩家ID列表
     */
    public List<Integer> getCallPassPlayerIds() {
        return callPassPlayerIds;
    }

    /**
     * 添加叫地主阶段不叫的玩家ID。
     *
     * @param playerId 玩家ID
     */
    public void addCallPassPlayerId(Integer playerId) {
        this.callPassPlayerIds.add(playerId);
    }

    /**
     * 获取第一个叫地主的玩家ID。
     *
     * @return 第一个叫地主的玩家ID
     */
    public Integer getFirstCallerId() {
        return firstCallerId;
    }

    /**
     * 设置第一个叫地主的玩家ID。
     *
     * @param firstCallerId 玩家ID
     */
    public void setFirstCallerId(Integer firstCallerId) {
        this.firstCallerId = firstCallerId;
    }

    /**
     * 获取当前地主候选人ID。
     *
     * @return 地主候选人ID
     */
    public Integer getLandlordCandidateId() {
        return landlordCandidateId;
    }

    /**
     * 设置当前地主候选人ID。
     *
     * @param landlordCandidateId 玩家ID
     */
    public void setLandlordCandidateId(Integer landlordCandidateId) {
        this.landlordCandidateId = landlordCandidateId;
    }

    /**
     * 获取底牌的副本。
     *
     * @return 底牌集合的副本(已排序)
     */
    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }

    /**
     * 获取出牌阶段状态对象。
     *
     * @return 出牌阶段状态
     */
    public PlayState getPlayState() {
        return playState;
    }

    /**
     * 获取下一个玩家ID（循环）。
     *
     * @param currentPlayerId 当前玩家ID
     * @return 下一个玩家ID
     */
    public Integer getNextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId == null || playerStates.isEmpty()) {
            return null;
        }
        int currentIndex = -1;
        for (int i = 0; i < playerStates.size(); i++) {
            if (playerStates.get(i).getPlayerId() == currentPlayerId) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % playerStates.size();
        return playerStates.get(nextIndex).getPlayerId();
    }
}