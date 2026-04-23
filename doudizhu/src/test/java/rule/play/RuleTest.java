package rule.play;

import org.junit.jupiter.api.Test;
import util.CardUtil;

import java.util.List;
import java.util.Scanner;

public class RuleTest {
    public static void main(String[] args) {
        PlayCardGroup playCardGroup = PlayCardGroup.analyzeCards(List.of(2,3,6,7,10,11,14,15,18,19));
        System.out.println(playCardGroup.getType());
        System.out.println(playCardGroup.getMainRank());
        System.out.println(playCardGroup.getSize());
    }
}
