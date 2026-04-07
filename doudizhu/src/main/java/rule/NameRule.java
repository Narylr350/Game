package rule;

import java.util.List;

/**
 * 玩家名称校验规则类。
 * <p>
 * 提供玩家名字合法性校验功能，确保游戏开始时所有玩家都有有效的名称。
 * </p>
 */
public class NameRule {

    /**
     * 校验玩家名称列表。
     * <p>
     * 要求必须有3个玩家，且每个玩家名称不能为null或空白字符串（包含全角空格）。
     * </p>
     *
     * @param playerNames 玩家名称列表
     * @return 校验通过返回true，否则返回false
     */
    public static boolean validatePlayerNames(List<String> playerNames) {
        if (playerNames == null || playerNames.size() != 3) {
            return false;
        }

        for (String playerName : playerNames) {
            if (playerName == null || playerName.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
