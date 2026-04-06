package rule;

import java.util.List;

public class NameRule {
    // 名字校验要兼容中文输入法下的全角空格
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
