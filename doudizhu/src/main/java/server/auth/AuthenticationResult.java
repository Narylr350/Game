package server.auth;

public record AuthenticationResult(boolean success, String message, String username, boolean requirePassword) {
    public AuthenticationResult(boolean success, String message, String username) {
        this(success, message, username, false);
    }
}
