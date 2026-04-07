package game.action;

/**
 * 叫地主/抢地主动作类型枚举。
 * <p>
 * 定义玩家在叫地主和抢地主阶段可以选择的动作类型。
 * </p>
 */
public enum ActionType {

    /** 叫地主/抢地主 */
    CALL("叫"),
    /** 不叫/不抢 */
    PASS("不叫");

    private final String text;

    ActionType(String text) {
        this.text = text;
    }

    /**
     * 从玩家输入的字符串解析为动作类型。
     * <p>
     * 支持精确匹配显示文本（如"叫"、"不叫"），忽略首尾空格。
     * </p>
     *
     * @param input 玩家输入的字符串
     * @return 匹配的动作类型，未匹配返回null
     */
    public static ActionType fromString(String input) {
        if (input == null) {
            return null;
        }

        input = input.trim();

        for (ActionType type : ActionType.values()) {
            if (type.text.equals(input)) {
                return type;
            }
        }

        return null;
    }

    /**
     * 获取动作的显示文本。
     *
     * @return 动作的中文显示文本
     */
    public String getText() {
        return text;
    }
}