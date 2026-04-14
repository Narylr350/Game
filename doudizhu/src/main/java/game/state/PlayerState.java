package game.state;

import java.util.Collection;
import java.util.TreeSet;

public class PlayerState {
    private final int playerId;              // 玩家唯一标识
    private final String playerName;         // 玩家名称
    private final TreeSet<Integer> cards;    // 玩家手牌(已排序)
    private boolean landlord;                // 是否为地主
    private boolean online;                  // 是否在线

    public PlayerState(int playerId, String playerName, TreeSet<Integer> cards) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.cards = new TreeSet<>(cards);
        this.online = true;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public TreeSet<Integer> getCards() {
        return new TreeSet<>(cards);
    }

    public boolean isLandlord() {
        return landlord;
    }

    public void setLandlord(boolean landlord) {
        this.landlord = landlord;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addCards(TreeSet<Integer> extraCards) {
        cards.addAll(extraCards);
    }

    /**
     * 从真实手牌中移除指定牌。
     *
     * @param removeCards 要移除的具体牌索引
     * @return 是否全部移除成功
     */
    public boolean removeCards(Collection<Integer> removeCards) {
        if (removeCards == null) {
            return false;
        }

        for (Integer removeCard : removeCards) {
            if (!cards.contains(removeCard)) {
                return false;
            }
        }

        for (Integer removeCard : removeCards) {
            cards.remove(removeCard);
        }
        return true;
    }
}