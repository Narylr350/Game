package server.auth;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByUsername(String username);

    void save(UserAccount userAccount);
}
