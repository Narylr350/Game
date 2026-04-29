package util;

import game.enumtype.GamePhase;

public final class GamePromptUtil {
    private GamePromptUtil() {
    }

    public static String getMessage(GamePhase type) {
        return switch (type) {
            case CALL_LANDLORD -> "1.叫地主 2.不叫";
            case ROB_LANDLORD -> "1.抢地主 2.不抢";
            case PLAYING -> "到你出牌了，输入 PASS 或直接回车则过牌";
            default -> "";
        };
    }

    public static String turnBroadcast(String playerName) {
        return "系统：当前轮到玩家" + playerName + "操作";
    }

    public static String turnConsoleTitle(String playerName) {
        return "==== 当前轮到玩家 " + playerName + " ====";
    }

    public static String playedCardsBroadcast(String playerName, String cardsText) {
        if (cardsText == null || cardsText.isBlank()) {
            return playerName + "不出";
        }
        return playerName + "出了：\n" + cardsText;
    }

    public static String replayPrompt() {
        return "是否下一把：1.继续 2.退出";
    }
}
