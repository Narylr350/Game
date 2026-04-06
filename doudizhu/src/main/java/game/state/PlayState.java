package game.state;

import java.util.List;

public class PlayState {
    private Integer lastPlayPlayerId;
    private List<Integer> lastPlayedCards;
    private int passCount;

    public Integer getLastPlayPlayerId() {
        return lastPlayPlayerId;
    }

    public void setLastPlayPlayerId(Integer lastPlayPlayerId) {
        this.lastPlayPlayerId = lastPlayPlayerId;
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

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public void addPassCount() {
        this.passCount++;
    }

    public void resetRound() {
        this.lastPlayPlayerId = null;
        this.lastPlayedCards = null;
        this.passCount = 0;
    }
}