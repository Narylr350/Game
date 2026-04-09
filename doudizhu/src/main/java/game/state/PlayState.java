package game.state;

import java.util.List;

/**
 * 出牌阶段状态类。
 * <p>
 * 封装出牌阶段的流程状态，包括上一手出牌信息、出牌玩家和过牌次数。
 * 用于判断当前玩家是否可以出牌以及是否需要重置轮次。
 * </p>
 */
public class PlayState {
    /**
     * 上一手出牌的玩家ID
     */
    private Integer lastPlayPlayerId;
    /**
     * 上一手打出的牌
     */
    private List<Integer> lastPlayedCards;
    /**
     * 连续过牌次数
     */
    private int passCount;

    /**
     * 获取上一手出牌的玩家ID。
     *
     * @return 玩家ID
     */
    public Integer getLastPlayPlayerId() {
        return lastPlayPlayerId;
    }

    /**
     * 设置上一手出牌的玩家ID。
     *
     * @param lastPlayPlayerId 玩家ID
     */
    public void setLastPlayPlayerId(Integer lastPlayPlayerId) {
        this.lastPlayPlayerId = lastPlayPlayerId;
    }

    /**
     * 获取上一手打出的牌。
     *
     * @return 牌列表
     */
    public List<Integer> getLastPlayedCards() {
        return lastPlayedCards;
    }

    /**
     * 设置上一手打出的牌。
     *
     * @param lastPlayedCards 牌列表
     */
    public void setLastPlayedCards(List<Integer> lastPlayedCards) {
        this.lastPlayedCards = lastPlayedCards;
    }

    /**
     * 获取连续过牌次数。
     *
     * @return 过牌次数
     */
    public int getPassCount() {
        return passCount;
    }

    /**
     * 设置连续过牌次数。
     *
     * @param passCount 过牌次数
     */
    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    /**
     * 过牌次数加1。
     */
    public void addPassCount() {
        this.passCount++;
    }

    /**
     * 重置轮次。
     * <p>
     * 当一轮出牌结束（如所有人都过牌，或有人出牌无人要）时调用，
     * 清空上一手出牌信息和过牌次数。
     * </p>
     */
    public void resetRound() {
        this.lastPlayPlayerId = null;
        this.lastPlayedCards = null;
        this.passCount = 0;
    }
}