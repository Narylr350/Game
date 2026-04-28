package server.auth;

public record AuthStepResult(String message, boolean authenticated, String username) {
}
