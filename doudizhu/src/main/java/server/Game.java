package server;

import java.util.*;

public class Game {
    static HashMap<Integer, String> cards = new HashMap<>();
    static ArrayList<Integer> list = new ArrayList<>();
    private List<Integer> holeCards = new ArrayList<>();

    static {
        ArrayList<String> numbers = new ArrayList<>();
        ArrayList<String> suits = new ArrayList<>();
        ArrayList<String> card = new ArrayList<>();

        Collections.addAll(numbers, "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2");
        Collections.addAll(suits, "♦", "♣", "♥", "♠");

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

    public void licensing(PlayerConnection p1, PlayerConnection p2, PlayerConnection p3) {
        Collections.shuffle(list);

        Set<Integer> set1 = new TreeSet<>();
        Set<Integer> set2 = new TreeSet<>();
        Set<Integer> set3 = new TreeSet<>();
        Set<Integer> hole = new TreeSet<>();

        for (int i = 0; i < list.size(); i++) {
            if (i < 3) {
                hole.add(list.get(i));
                continue;
            }

            if (i % 3 == 0) {
                set1.add(list.get(i));
            } else if (i % 3 == 1) {
                set2.add(list.get(i));
            } else {
                set3.add(list.get(i));
            }
        }

        p1.setCards(new ArrayList<>(set1));
        p2.setCards(new ArrayList<>(set2));
        p3.setCards(new ArrayList<>(set3));
        holeCards = new ArrayList<>(hole);
    }

    public List<Integer> getHoleCards() {
        return holeCards;
    }

    public static String cardsToString(Collection<Integer> lists) {
        StringBuilder sb = new StringBuilder();
        for (int id : lists) {
            sb.append(cards.get(id)).append(" ");
        }
        return sb.toString();
    }
}