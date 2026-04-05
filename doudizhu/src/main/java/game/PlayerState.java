package game;

import java.util.TreeSet;

public class PlayerState {
    private final int playerId;
    private final String playerName;
    private final TreeSet<Integer> cards;
    private boolean landlord;
    private boolean online;

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

    // 地主确定后，把底牌并入手牌时会用到。
    public void addCards(TreeSet<Integer> extraCards) {
        cards.addAll(extraCards);
    }
}
