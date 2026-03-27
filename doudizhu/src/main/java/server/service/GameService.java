package server.service;

import client.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GameService {
    static HashMap<Integer, String> cards = new HashMap<>();
    static ArrayList<Integer> list = new ArrayList<>();

    static {
        ArrayList<String> numbers = new ArrayList<>();
        ArrayList<String> suits = new ArrayList<>();
        ArrayList<String> card = new ArrayList<>();

        Collections.addAll(numbers, "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2");
        Collections.addAll(suits, "⬛️", "♣️", "♥️", "♠️");

        for (String number : numbers) {
            for (String suit : suits) {
                card.add(number + suit);
            }
        }
        card.add("小王");
        card.add("大王");

        for (int i = 0; i < card.size(); i++) {
            cards.put(i + 1, card.get(i));
            list.add(i + 1);
        }
    }

    public List<Player> dealCards(String name1, String name2, String name3, Player player1, Player player2, Player player3) {
        List<Player> playerList = new ArrayList<>();
        Collections.shuffle(list);
        Player holeCard = new Player();

        TreeSet<Integer> p1 = new TreeSet<>();
        TreeSet<Integer> p2 = new TreeSet<>();
        TreeSet<Integer> p3 = new TreeSet<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < list.size(); i++) {
            if (i < 3) {
                holeCards.add(list.get(i));
                continue;
            }
            if (i % 3 == 0) {
                p1.add(list.get(i));
            }
            if (i % 3 == 1) {
                p2.add(list.get(i));
            }
            if (i % 3 == 2) {
                p3.add(list.get(i));
            }
        }

        player1.setName(name1);
        player2.setName(name2);
        player3.setName(name3);
        holeCard.setName("底牌");
        player1.setCard(p1);
        player2.setCard(p2);
        player3.setCard(p3);
        holeCard.setCard(holeCards);

        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        playerList.add(holeCard);

        return playerList;
    }

    public void findCards(Set<Integer> lists) {
        for (int list : lists) {
            String card = cards.get(list);
            System.out.print(card + " \t");
        }
        System.out.println();
    }
}
