package game;

public class GameResult {
    private final boolean success;
    private final String message;
    private final GameEventType eventType;
    private final Integer sendToPlayerId;

    public GameResult(boolean success, String message, GameEventType eventType, Integer sendToPlayerId) {
        this.success = success;
        this.message = message;
        this.eventType = eventType;
        this.sendToPlayerId = sendToPlayerId;
    }

    public static GameResult rejected(String message) {
        return new GameResult(
                false,
                message,
                GameEventType.ACTION_REJECTED,
                null);
    }

    public static GameResult rejected(String message, Integer sendToPlayerId) {
        return new GameResult(
                false,
                message,
                GameEventType.ACTION_REJECTED,
                sendToPlayerId);
    }

    public static GameResult accepted(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.ACTION_ACCEPTED,
                null);
    }

    public static GameResult accepted(String message, Integer sendToPlayerId) {
        return new GameResult(
                true,
                message,
                GameEventType.ACTION_ACCEPTED,
                sendToPlayerId);
    }

    public static GameResult redealRequired(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.REDEAL_REQUIRED,
                null);
    }

    public static GameResult landlordDecided(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.LANDLORD_DECIDED,
                null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public GameEventType getEventType() {
        return eventType;
    }

    public Integer getSendToPlayerId() {
        return sendToPlayerId;
    }
}