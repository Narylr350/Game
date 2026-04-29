package game.model;

import game.enumtype.GamePhase;
import game.state.LandlordState;
import game.state.PlayingState;
import game.state.PlayerState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

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
    // ===== 阶段状态 =====
    private final LandlordState landlordState;
    private final PlayingState playingState;
    // ===== 流程状态 =====
    private GamePhase currentPhase;
    private Integer currentPlayerId;
    private Integer landlordPlayerId;

    /**
     * 创建游戏房间。
     *
     * @param players   玩家状态列表
     * @param holeCards 底牌集合
     */
    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        // 牌初始化
        this.playerStates = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
        // 阶段初始化
        this.landlordState = new LandlordState();
        this.playingState = new PlayingState();
        // 流程初始化
        this.currentPhase = GamePhase.WAITING;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GamePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

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

    public Integer getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Integer currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

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

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }

    public LandlordState getLandlordState() {
        return landlordState;
    }

    public PlayingState getPlayingState() {
        return playingState;
    }

    public Integer getNextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId == null || playerStates.isEmpty()) {
            return null;
        }
        int currentIndex = -1;
        for (int i = 0; i < playerStates.size(); i++) {
            if (playerStates.get(i)
                    .getPlayerId() == currentPlayerId) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return null;
        }
        int nextIndex = (currentIndex + 1) % playerStates.size();
        return playerStates.get(nextIndex)
                .getPlayerId();
    }
}
