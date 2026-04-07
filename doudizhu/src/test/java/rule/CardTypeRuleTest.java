package rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CardTypeRule 单元测试类。
 * <p>
 * 注意: 根据项目文档,第1阶段牌型识别尚未实现,
 * 当前所有牌型都会被识别为INVALID。
 * </p>
 */
class CardTypeRuleTest {

    private CardTypeRule cardTypeRule;

    @BeforeEach
    void setUp() {
        cardTypeRule = new CardTypeRule();
    }

    @Test
    void testIdentify_NullCards() {
        CardType result = cardTypeRule.identify(null);
        assertEquals(CardType.INVALID, result, "null牌组应返回INVALID");
    }

    @Test
    void testIdentify_EmptyList() {
        CardType result = cardTypeRule.identify(Collections.emptyList());
        assertEquals(CardType.INVALID, result, "空牌组应返回INVALID");
    }

    @Test
    void testIdentify_SingleCard() {
        List<Integer> cards = Arrays.asList(1);
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段单张应返回INVALID");
    }

    @Test
    void testIdentify_Pair() {
        List<Integer> cards = Arrays.asList(1, 5); // 两张3
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段对子应返回INVALID");
    }

    @Test
    void testIdentify_Triple() {
        List<Integer> cards = Arrays.asList(1, 5, 9); // 三张3
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段三条应返回INVALID");
    }

    @Test
    void testIdentify_MultipleCards() {
        List<Integer> cards = Arrays.asList(1, 2, 3, 4, 5);
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段多张牌应返回INVALID");
    }

    @Test
    void testIdentify_Bomb() {
        List<Integer> cards = Arrays.asList(1, 5, 9, 13); // 四张3
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段炸弹应返回INVALID");
    }

    @Test
    void testIdentify_Rocket() {
        List<Integer> cards = Arrays.asList(53, 54); // 王炸
        CardType result = cardTypeRule.identify(cards);
        assertEquals(CardType.INVALID, result, "第1阶段火箭应返回INVALID");
    }
}
