package server.auth;

import java.time.LocalDateTime;

public class UserAccount {
    private final String id;
    private final String username;
    private final String password;
    private final boolean status;
    private final LocalDateTime lastLoginAt;

    public UserAccount(String id, String username, String password, boolean status) {
        this(id, username, password, status, null);
    }

    public UserAccount(String id, String username, String password, boolean status, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isStatus() {
        return status;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
