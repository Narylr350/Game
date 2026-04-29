package server.auth;

import org.junit.jupiter.api.Test;
import util.CredentialPolicy;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSessionTest {

    @Test
    void should_complete_login_after_server_guided_questions() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        AuthStepResult firstPrompt = session.start();
        assertEquals("请选择操作：1登录 2注册", firstPrompt.message());
        assertFalse(firstPrompt.authenticated());

        AuthStepResult usernamePrompt = session.handleInput("1");
        assertEquals("请输入用户名：", usernamePrompt.message());

        AuthStepResult passwordPrompt = session.handleInput("alice");
        assertEquals("请输入密码：", passwordPrompt.message());

        AuthStepResult success = session.handleInput("abc123");
        assertEquals("登录成功,游戏启动~", success.message());
        assertTrue(success.authenticated());
        assertEquals("alice", success.username());
        assertTrue(session.isAuthenticated());
    }

    @Test
    void should_reject_duplicate_username_and_keep_registering() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        session.start();
        session.handleInput("2");

        AuthStepResult duplicate = session.handleInput("alice");
        assertEquals("用户名已经存在请重新输入", duplicate.message());
        assertFalse(duplicate.authenticated());

        AuthStepResult passwordPrompt = session.handleInput("bob1");
        assertEquals("请输入密码：", passwordPrompt.message());

        AuthStepResult success = session.handleInput("abc123");
        assertEquals("用户bob1注册成功！", success.message());
        assertTrue(success.authenticated());
        assertEquals("bob1", success.username());
    }

    @Test
    void should_auto_login_after_username_when_last_login_is_recent() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true, LocalDateTime.now().minusDays(2)));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        session.start();
        session.handleInput("1");
        AuthStepResult result = session.handleInput("alice");

        assertEquals("登录成功,游戏启动~", result.message());
        assertTrue(result.authenticated());
        assertEquals("alice", result.username());
    }

    @Test
    void should_still_ask_for_password_when_remembered_login_has_expired() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true, LocalDateTime.now().minusDays(8)));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        session.start();
        session.handleInput("1");
        AuthStepResult result = session.handleInput("alice");

        assertEquals("请输入密码：", result.message());
        assertFalse(result.authenticated());
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<String, UserAccount> users = new HashMap<>();

        @Override
        public Optional<UserAccount> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }

        @Override
        public void save(UserAccount userAccount) {
            users.put(userAccount.getUsername(), userAccount);
        }

        @Override
        public void updateLoginTime(String userId, LocalDateTime loginTime) {
            for (Map.Entry<String, UserAccount> entry : users.entrySet()) {
                UserAccount userAccount = entry.getValue();
                if (userAccount.getId().equals(userId)) {
                    entry.setValue(new UserAccount(
                            userAccount.getId(),
                            userAccount.getUsername(),
                            userAccount.getPassword(),
                            userAccount.isStatus(),
                            loginTime
                    ));
                    return;
                }
            }
        }
    }
}
