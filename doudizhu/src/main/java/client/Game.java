package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class Game {
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

    public static void Licensing() {
        Collections.shuffle(list);

        Set<Integer> p1 = new TreeSet<>();
        Set<Integer> p2 = new TreeSet<>();
        Set<Integer> p3 = new TreeSet<>();
        Set<Integer> holeCards = new TreeSet<>();

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
        findCards(p1);
        findCards(p2);
        findCards(p3);
        findCards(holeCards);
    }

    public static void findCards(Set<Integer> lists) {
        for (int list : lists) {
            String card = cards.get(list);
            System.out.print(card + " \t");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Licensing();
    }
}
