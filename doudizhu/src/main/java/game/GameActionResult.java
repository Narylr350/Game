package game;

public class GameActionResult {

    private final boolean success;
    private final boolean needRedeal;
    private final String message;

    private final Integer nextPlayerId;
    private final Integer sendToPlayerId;
    private final Integer landlordPlayerId;
    private final GamePhase nextPhase;

    public GameActionResult(boolean success, boolean needRedeal, String message, Integer nextPlayerId, Integer sendToPlayerId, Integer landlordPlayerId, GamePhase nextPhase) {
        this.success = success;
        this.needRedeal = needRedeal;
        this.message = message;
        this.nextPlayerId = nextPlayerId;
        this.sendToPlayerId = sendToPlayerId;
        this.landlordPlayerId = landlordPlayerId;
        this.nextPhase = nextPhase;
    }

    public static GameActionResult invalidAction(String message) {
        return new GameActionResult(
                false,
                false,
                message,
                null,
                null,
                null,
                null);
    }

    public static GameActionResult invalidAction(String message, Integer sendToPlayerId) {
        return new GameActionResult(
                false,
                false,
                message,
                null,
                sendToPlayerId,
                null,
                null);
    }

    public static GameActionResult actionAccepted(String message) {
        return new GameActionResult(
                true,
                false,
                message,
                null,
                null,
                null,
                null);
    }

    public static GameActionResult actionAccepted(String message, Integer nextPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                nextPlayerId,
                null,
                null,
                null);
    }

    public static GameActionResult actionAccepted(String message, Integer nextPlayerId, Integer sendToPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                nextPlayerId,
                sendToPlayerId,
                null,
                null);
    }

    public static GameActionResult redeal(String message) {
        return new GameActionResult(
                true,
                true,
                message,
                null,
                null,
                null,
                GamePhase.DEALING);
    }

    public static GameActionResult landlordDecided(String message, Integer landlordPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                null,
                null,
                landlordPlayerId,
                GamePhase.PLAYING);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isNeedRedeal() {
        return needRedeal;
    }

    public String getMessage() {
        return message;
    }

    public Integer getNextPlayerId() {
        return nextPlayerId;
    }

    public Integer getSendToPlayerId() {
        return sendToPlayerId;
    }

    public Integer getLandlordPlayerId() {
        return landlordPlayerId;
    }

    public GamePhase getNextPhase() {
        return nextPhase;
    }
}