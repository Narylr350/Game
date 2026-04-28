package server.auth;

public class UserAccount {
    private final String id;
    private final String username;
    private final String password;
    private final boolean status;

    public UserAccount(String id, String username, String password, boolean status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
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
}
