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
                        "请选择操作：1登录 2注册",
                        "请输入用户名：",
                        "请输入密码：",
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
                        "请选择操作：1登录 2注册",
                        "请输入用户名：",
                        "请输入密码：",
                        "登录失败，密码输入错误~",
                        "请选择操作：1登录 2注册",
                        "请输入用户名：",
                        "请输入密码：",
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
                        "请选择操作：1登录 2注册",
                        "请输入用户名：",
                        "登录成功,游戏启动~")
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
