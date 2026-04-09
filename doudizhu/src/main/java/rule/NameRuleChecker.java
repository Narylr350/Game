package rule;

import java.util.List;

/**
 * 玩家名称校验规则类。
 * <p>
 * 提供玩家名字合法性校验功能，确保游戏开始时所有玩家都有有效的名称。
 * </p>
 */
public class NameRuleChecker {

    /**
     * 校验玩家名称列表的有效性。
     *
     * @param playerNames 玩家名称的列表，每个元素代表一个玩家的名字
     * @throws IllegalArgumentException 如果玩家列表为空，或者列表中包含空或空白的玩家名
     */
    public static void validatePlayerNames(List<String> playerNames) {
        if (playerNames == null ) {
            throw new  IllegalArgumentException("玩家列表不能为空");
        }

        for (String playerName : playerNames) {
            if (playerName == null || playerName.isBlank()) {
                throw new IllegalArgumentException("玩家名不能为空");
            }
        }
    }
}
