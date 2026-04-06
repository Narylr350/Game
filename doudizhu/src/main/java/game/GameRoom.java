package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * 游戏房间管理类。
 * <p>
 * 负责管理一个斗地主房间的所有状态,包括玩家列表、底牌、游戏阶段、
 * 叫/抢地主阶段的各种计数器和管理员信息等。
 * </p>
 */
public class GameRoom {
    private final List<PlayerState> players;          // 玩家列表
    private final TreeSet<Integer> holeCards;         // 底牌
    private GamePhase phase;                          // 当前游戏阶段
    //叫/抢地主阶段所需字段
    private int passCount;                            // 不抢玩家计数器
    private List<Integer> passPlayerId;               // 不抢玩家id集合
    private Integer firstCallerId;                    // 第一个叫地主的玩家ID
    private Integer landLordCandidateId;              // 地主候选人ID
    private Integer currentTurnPlayerId;              // 当前轮到谁操作
    private Integer landLordPlayerId;                 // 最终地主ID

    /**
     * 创建一个新的游戏房间。
     *
     * @param players 玩家列表
     * @param holeCards 底牌集合
     */
    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
        passPlayerId = new ArrayList<>();
    }

    /**
     * 获取当前游戏阶段。
     *
     * @return 当前游戏阶段枚举值
     */
    public GamePhase getPhase() {
        return phase;
    }

    /**
     * 设置当前游戏阶段。
     *
     * @param phase 要设置的游戏阶段枚举值
     */
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    /**
     * 获取所有玩家的只读列表。
     * <p>
     * 返回的列表是不可修改的,防止外部直接修改房间内的玩家状态。
     * </p>
     *
     * @return 包含所有玩家的只读列表
     */
    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * 设置地主的玩家ID。
     * <p>
     * 设置地主ID后,会自动更新所有玩家的地主状态标识。
     * </p>
     *
     * @param landLordPlayerId 地主的玩家ID
     */
    public void setLandLordId(Integer landLordPlayerId) {
        this.landLordPlayerId = landLordPlayerId;
        for (PlayerState player : players) {
            player.setLandlord(landLordPlayerId != null && player.getPlayerId() == landLordPlayerId);
        }
    }

    /**
     * 根据玩家ID查找对应的玩家状态对象。
     *
     * @param playerId 要查找的玩家ID
     * @return 找到的玩家状态对象,如果未找到则返回null
     */
    public PlayerState findPlayerById(int playerId) {
        for (PlayerState player : players) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }

    public int getPassCount() {
        return passCount;
    }

    public void addPassCount() {
        passCount++;
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
