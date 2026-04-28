package server.auth;

public record AuthenticationResult(boolean success, String message, String username) {
}
