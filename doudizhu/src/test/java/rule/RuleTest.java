package rule;

import org.junit.jupiter.api.Test;

import java.util.Scanner;

public class RuleTest {
    public static void main(String[] args) {
        String input = new Scanner(System.in).nextLine();
        PlayCardGroup playCardGroup = PlayCardGroup.of(input);
        System.out.println(playCardGroup.getType());
        System.out.println(playCardGroup.getMainRank());
        System.out.println(playCardGroup.getSize());
    }
}
