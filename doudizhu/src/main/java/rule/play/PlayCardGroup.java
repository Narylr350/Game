package rule.play;

import java.util.Collection;

/**
 * 牌型分析结果。
 * <p>
 * 只保存牌型、主牌点数和牌数。具体分析逻辑放在 {@link PlayCardAnalyzer}。
 * </p>
 */
public class PlayCardGroup {
    private final CardType type;
    private final int mainRank;
    private final int size;

    public PlayCardGroup(CardType type, int mainRank, int size) {
        this.type = type;
        this.mainRank = mainRank;
        this.size = size;
    }

    public CardType getType() {
        return type;
    }

    public int getMainRank() {
        return mainRank;
    }

    public int getSize() {
        return size;
    }

    /**
     * 兼容旧调用入口，实际分析逻辑由 {@link PlayCardAnalyzer} 处理。
     */
    public static PlayCardGroup analyzeCards(Collection<Integer> cards) {
        return PlayCardAnalyzer.analyze(cards);
    }
}
