package game.state;

import java.util.List;

public class PlayingState {
    private List<Integer> lastPlayedCards;    // 上一手出的牌
    private int passCount;                    // 连续过牌次数
    private Integer highestCardPlayerId;      // 最大牌的拥有者ID
    private List<Integer> recentPlayedCards;  // 当前牌

    public List<Integer> getRecentPlayedCards() {
        return recentPlayedCards;
    }

    public void setRecentPlayedCards(List<Integer> recentPlayedCards) {
        this.recentPlayedCards = recentPlayedCards;
    }

    public Integer getHighestCardPlayerId() {
        return highestCardPlayerId;
    }

    public void setHighestCardPlayerId(Integer highestCardPlayerId) {
        this.highestCardPlayerId = highestCardPlayerId;
    }

    public List<Integer> getLastPlayedCards() {
        return lastPlayedCards;
    }

    public void setLastPlayedCards(List<Integer> lastPlayedCards) {
        this.lastPlayedCards = lastPlayedCards;
    }

    public int getPassCount() {
        return passCount;
    }

    public void incrementPassCount() {
        this.passCount++;
    }
    public void resetPassCount() {
        this.passCount = 0;
    }
    /**
     * 重置出牌阶段的状态。
     * <p>
     * 该方法将上一手出的牌以及连续过牌次数重置为初始状态。
     * 通常在一轮出牌结束后调用此方法，以便开始新的一轮出牌。
     * </p>
     */
    public void resetRound() {
        this.lastPlayedCards = null;
        this.passCount = 0;
    }
}
