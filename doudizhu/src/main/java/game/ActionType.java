package game;

public enum ActionType {

    CALL("叫"),
    PASS("不叫");

    private final String text;

    ActionType(String text) {
        this.text = text;
    }

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


        return null; // 表示非法输入
    }

    public String getText() {
        return text;
    }
}