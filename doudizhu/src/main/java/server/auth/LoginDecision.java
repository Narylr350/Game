package server.auth;

public record LoginDecision(boolean success, boolean requirePassword, String message, String username) {
}
