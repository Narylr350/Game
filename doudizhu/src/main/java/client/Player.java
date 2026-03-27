package client;

import java.util.TreeSet;

public class Player {
    private String name;
    private TreeSet<Integer> card;

    public Player() {
    }

    public Player(String name, TreeSet<Integer> card) {
        this.name = name;
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSet<Integer> getCard() {
        return card;
    }

    public void setCard(TreeSet<Integer> card) {
        this.card = card;
    }
}
