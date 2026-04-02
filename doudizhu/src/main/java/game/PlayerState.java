package game;

import java.util.TreeSet;

// 只保留当前阶段真正需要的玩家状态，后续玩法扩展时再逐步增加字段。
public class PlayerState {
    private final int playerId;
    private final String playerName;
    private final TreeSet<Integer> cards;
    private boolean landlord;
    private boolean online;
    private int score;

    public PlayerState(int playerId, String playerName, TreeSet<Integer> cards) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.cards = new TreeSet<>(cards);
        this.online = true;
        this.score = 0;
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

    // 地主确定后，把底牌并入手牌时会用到。
    public void addCards(TreeSet<Integer> extraCards) {
        cards.addAll(extraCards);
    }

    // 当前先保留最小计分入口，后续再决定抢地主阶段怎么用。
    public void addScore() {
        score++;
    }

    public int getScore() {
        return score;
    }
}
