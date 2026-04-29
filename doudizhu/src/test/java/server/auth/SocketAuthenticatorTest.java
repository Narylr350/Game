package server.auth;

import org.junit.jupiter.api.Test;
import util.CredentialPolicy;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocketAuthenticatorTest {
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

    @Test
    void should_drive_login_by_prompting_step_by_step() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        SocketAuthenticator authenticator =
                new SocketAuthenticator(new AuthenticationService(repository, new CredentialPolicy()));

        BufferedReader reader = new BufferedReader(new StringReader("1\nalice\nabc123\n"));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);

        String username = authenticator.authenticate(reader, writer);

        assertEquals("alice", username);
        assertEquals(
                String.join(System.lineSeparator(),
                        WELCOME_MENU,
                        USERNAME_PROMPT,
                        PASSWORD_PROMPT,
                        "登录成功,游戏启动~")
                        + System.lineSeparator(),
                output.toString()
        );
    }

    @Test
    void should_prompt_again_after_login_failure() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true));
        SocketAuthenticator authenticator =
                new SocketAuthenticator(new AuthenticationService(repository, new CredentialPolicy()));

        BufferedReader reader = new BufferedReader(new StringReader("1\nalice\nwrong\n1\nalice\nabc123\n"));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);

        String username = authenticator.authenticate(reader, writer);

        assertEquals("alice", username);
        assertEquals(
                String.join(System.lineSeparator(),
                        WELCOME_MENU,
                        USERNAME_PROMPT,
                        PASSWORD_PROMPT,
                        "登录失败，密码输入错误~",
                        WELCOME_MENU,
                        USERNAME_PROMPT,
                        PASSWORD_PROMPT,
                        "登录成功,游戏启动~")
                        + System.lineSeparator(),
                output.toString()
        );
    }

    @Test
    void should_complete_login_without_password_when_last_login_is_recent() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.save(new UserAccount("user00001", "alice", "abc123", true, LocalDateTime.now().minusDays(1)));
        SocketAuthenticator authenticator =
                new SocketAuthenticator(new AuthenticationService(repository, new CredentialPolicy()));

        BufferedReader reader = new BufferedReader(new StringReader("1\nalice\n"));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);

        String username = authenticator.authenticate(reader, writer);

        assertEquals("alice", username);
        assertEquals(
                String.join(System.lineSeparator(),
                        WELCOME_MENU,
                        USERNAME_PROMPT,
                        "登录成功,游戏启动~")
                        + System.lineSeparator(),
                output.toString()
        );
    }

    @Test
    void should_return_null_when_user_exits_from_menu() {
        SocketAuthenticator authenticator =
                new SocketAuthenticator(new AuthenticationService(new InMemoryUserRepository(), new CredentialPolicy()));

        BufferedReader reader = new BufferedReader(new StringReader("3\n"));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);

        String username = authenticator.authenticate(reader, writer);

        assertEquals(null, username);
        assertEquals(
                String.join(System.lineSeparator(),
                        WELCOME_MENU,
                        "用户选择了退出操作")
                        + System.lineSeparator(),
                output.toString()
        );
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
