package server.auth;

public class RepositoryAccessException extends RuntimeException {
    public RepositoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryAccessException(String message) {
        super(message);
    }
}
