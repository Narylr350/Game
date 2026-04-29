package server.auth;

public record AuthStepResult(String message, boolean authenticated, String username, boolean exitRequested) {
    public AuthStepResult(String message, boolean authenticated, String username) {
        this(message, authenticated, username, false);
    }

    public static AuthStepResult exit(String message) {
        return new AuthStepResult(message, false, null, true);
    }
}
