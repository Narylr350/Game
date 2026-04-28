package client.auth;

public class ClientAuthState {
    private boolean authenticated;

    public void consumeServerMessage(String message) {
        if ("登录成功,游戏启动~".equals(message) || message.endsWith("注册成功！")) {
            authenticated = true;
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
