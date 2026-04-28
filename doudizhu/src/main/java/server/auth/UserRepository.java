package server.auth;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByUsername(String username);

    void save(UserAccount userAccount);

    void updateLoginTime(String userId, LocalDateTime loginTime);
}
