package server.auth;

import util.CredentialPolicy;

import java.util.Optional;
import java.util.Random;

public class AuthenticationService {
    private final UserRepository userRepository;
    private final CredentialPolicy credentialPolicy;

    public AuthenticationService(UserRepository userRepository, CredentialPolicy credentialPolicy) {
        this.userRepository = userRepository;
        this.credentialPolicy = credentialPolicy;
    }

    public AuthenticationResult login(String username, String password) {
        try {
            Optional<UserAccount> userAccount = userRepository.findByUsername(username);
            if (userAccount.isEmpty()) {
                return new AuthenticationResult(false, "用户名" + username + "未注册，请先注册再登录", null);
            }

            UserAccount account = userAccount.get();
            if (!account.isStatus()) {
                return new AuthenticationResult(false, "用户" + username + "已禁用，请联系客服乌鲁鲁：18000000000", null);
            }

            if (!account.getPassword().equals(password)) {
                return new AuthenticationResult(false, "登录失败，密码输入错误~", null);
            }

            return new AuthenticationResult(true, "登录成功,游戏启动~", account.getUsername());
        } catch (RepositoryAccessException e) {
            return new AuthenticationResult(false, "登录失败，请稍后再试", null);
        }
    }

    public AuthenticationResult register(String username, String password) {
        try {
            String usernameMessage = validateNewUsername(username);
            if (usernameMessage != null) {
                return new AuthenticationResult(false, usernameMessage, null);
            }

            String passwordMessage = credentialPolicy.validatePassword(password);
            if (passwordMessage != null) {
                return new AuthenticationResult(false, passwordMessage, null);
            }

            userRepository.save(new UserAccount(createId(), username, password, true));
            return new AuthenticationResult(true, "用户" + username + "注册成功！", username);
        } catch (RepositoryAccessException e) {
            return new AuthenticationResult(false, "注册失败，请稍后再试", null);
        }
    }

    public String validateNewUsername(String username) {
        try {
            String usernameMessage = credentialPolicy.validateUsername(username);
            if (usernameMessage != null) {
                return usernameMessage;
            }
            if (userRepository.findByUsername(username).isPresent()) {
                return "用户名已经存在请重新输入";
            }
            return null;
        } catch (RepositoryAccessException e) {
            return "注册失败，请稍后再试";
        }
    }

    private String createId() {
        StringBuilder builder = new StringBuilder("user");
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
