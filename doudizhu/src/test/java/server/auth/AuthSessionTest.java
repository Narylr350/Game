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
    private static final String WELCOME_MENU = String.join("\n",
            "游戏的登录注册页面打开了",
            "╔════════════════════════════════╗",
            "    🎮 欢迎来到三人斗地主 🎮   ",
            "╚════════════════════════════════╝",
            "登录/注册输入时输入 exit 返回功能菜单",
            "请选择操作：1登录 2注册 3退出"
    );
    private static final String USERNAME_PROMPT = "请输入用户名（输入 exit 返回功能菜单）：";
    private static final String PASSWORD_PROMPT = "请输入密码（输入 exit 返回功能菜单）：";
    private static final String CONFIRM_PASSWORD_PROMPT = "请再次输入密码（输入 exit 返回功能菜单）：";

    @Test
    void should_complete_login_after_server_guided_questions() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        AuthStepResult firstPrompt = session.start();
        assertEquals(WELCOME_MENU, firstPrompt.message());
        assertFalse(firstPrompt.authenticated());

        AuthStepResult usernamePrompt = session.handleInput("1");
        assertEquals(USERNAME_PROMPT, usernamePrompt.message());

        AuthStepResult passwordPrompt = session.handleInput("alice");
        assertEquals(PASSWORD_PROMPT, passwordPrompt.message());

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
        assertEquals(PASSWORD_PROMPT, passwordPrompt.message());

        AuthStepResult confirmPasswordPrompt = session.handleInput("abc123");
        assertEquals(CONFIRM_PASSWORD_PROMPT, confirmPasswordPrompt.message());

        AuthStepResult success = session.handleInput("abc123");
        assertEquals("用户bob1注册成功！", success.message());
        assertTrue(success.authenticated());
        assertEquals("bob1", success.username());
    }

    @Test
    void should_reject_register_when_confirm_password_is_different() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        session.start();
        session.handleInput("2");
        session.handleInput("bob1");
        session.handleInput("abc123");

        AuthStepResult mismatch = session.handleInput("abc456");

        assertEquals("两次密码输入不一致请重新输入", mismatch.message());
        assertFalse(mismatch.authenticated());
        assertTrue(repository.findByUsername("bob1").isEmpty());
        assertEquals(PASSWORD_PROMPT, session.currentPrompt());
    }

    @Test
    void should_return_to_menu_when_exit_is_entered_during_login_or_register() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        AuthSession session = new AuthSession(new AuthenticationService(repository, new CredentialPolicy()));

        session.start();
        session.handleInput("1");
        session.handleInput("alice");
        AuthStepResult loginBack = session.handleInput("exit");
        assertEquals(WELCOME_MENU, loginBack.message());
        assertFalse(loginBack.authenticated());

        session.handleInput("2");
        session.handleInput("bob1");
        AuthStepResult registerBack = session.handleInput("exit");
        assertEquals(WELCOME_MENU, registerBack.message());
        assertFalse(registerBack.authenticated());
        assertEquals(WELCOME_MENU, session.currentPrompt());
    }

    @Test
    void should_request_exit_when_choose_exit_from_menu() {
        AuthSession session = new AuthSession(new AuthenticationService(new InMemoryUserRepository(), new CredentialPolicy()));

        session.start();
        AuthStepResult result = session.handleInput("3");

        assertEquals("用户选择了退出操作", result.message());
        assertFalse(result.authenticated());
        assertTrue(result.exitRequested());
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

        assertEquals(PASSWORD_PROMPT, result.message());
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
