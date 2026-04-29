package server.auth;

import org.junit.jupiter.api.Test;
import util.CredentialPolicy;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticationServiceTest {

    @Test
    void should_fail_register_when_repository_is_unavailable() {
        AuthenticationService service =
                new AuthenticationService(new FailingUserRepository(), new CredentialPolicy());

        AuthenticationResult result = service.register("alice1", "abc123");

        assertFalse(result.success());
        assertTrue(result.message().contains("注册失败"));
    }

    @Test
    void should_fail_login_when_repository_is_unavailable() {
        AuthenticationService service =
                new AuthenticationService(new FailingUserRepository(), new CredentialPolicy());

        AuthenticationResult result = service.login("alice", "abc123");

        assertFalse(result.success());
        assertTrue(result.message().contains("登录失败"));
    }

    @Test
    void should_persist_login_time_when_register_succeeds() {
        RecordingUserRepository repository = new RecordingUserRepository();
        AuthenticationService service = new AuthenticationService(repository, new CredentialPolicy());

        AuthenticationResult result = service.register("alice1", "abc123");

        assertTrue(result.success());
        assertNotNull(repository.savedUser);
        assertNotNull(repository.savedUser.getLastLoginAt());
    }

    @Test
    void should_refresh_login_time_when_login_succeeds() {
        RecordingUserRepository repository = new RecordingUserRepository();
        repository.user = new UserAccount("user00001", "alice", "abc123", true, null);
        AuthenticationService service = new AuthenticationService(repository, new CredentialPolicy());

        AuthenticationResult result = service.login("alice", "abc123");

        assertTrue(result.success());
        assertEquals("user00001", repository.updatedUserId);
        assertNotNull(repository.updatedLoginAt);
    }

    @Test
    void should_auto_login_when_last_login_is_within_seven_days() {
        RecordingUserRepository repository = new RecordingUserRepository();
        repository.user = new UserAccount(
                "user00001",
                "alice",
                "abc123",
                true,
                LocalDateTime.now().minusDays(3)
        );
        AuthenticationService service = new AuthenticationService(repository, new CredentialPolicy());

        LoginDecision decision = service.prepareLogin("alice");

        assertTrue(decision.success());
        assertFalse(decision.requirePassword());
        assertEquals("alice", decision.username());
        assertEquals("user00001", repository.updatedUserId);
        assertNotNull(repository.updatedLoginAt);
    }

    @Test
    void should_require_password_when_last_login_is_older_than_seven_days() {
        RecordingUserRepository repository = new RecordingUserRepository();
        repository.user = new UserAccount(
                "user00001",
                "alice",
                "abc123",
                true,
                LocalDateTime.now().minusDays(8)
        );
        AuthenticationService service = new AuthenticationService(repository, new CredentialPolicy());

        LoginDecision decision = service.prepareLogin("alice");

        assertFalse(decision.success());
        assertTrue(decision.requirePassword());
        assertEquals("请输入密码（输入 exit 返回功能菜单）：", decision.message());
    }

    private static final class FailingUserRepository implements UserRepository {
        @Override
        public Optional<UserAccount> findByUsername(String username) {
            throw new RepositoryAccessException("db unavailable");
        }

        @Override
        public void save(UserAccount userAccount) {
            throw new RepositoryAccessException("db unavailable");
        }

        @Override
        public void updateLoginTime(String userId, LocalDateTime loginTime) {
            throw new RepositoryAccessException("db unavailable");
        }
    }

    private static final class RecordingUserRepository implements UserRepository {
        private UserAccount user;
        private UserAccount savedUser;
        private String updatedUserId;
        private LocalDateTime updatedLoginAt;

        @Override
        public Optional<UserAccount> findByUsername(String username) {
            return Optional.ofNullable(user);
        }

        @Override
        public void save(UserAccount userAccount) {
            this.savedUser = userAccount;
            this.user = userAccount;
        }

        @Override
        public void updateLoginTime(String userId, LocalDateTime loginTime) {
            this.updatedUserId = userId;
            this.updatedLoginAt = loginTime;
        }
    }
}
