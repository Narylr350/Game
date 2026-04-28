package server.auth;

public class AuthSession {
    private enum Step {
        CHOOSE_ACTION,
        LOGIN_USERNAME,
        LOGIN_PASSWORD,
        REGISTER_USERNAME,
        REGISTER_PASSWORD,
        AUTHENTICATED
    }

    private final AuthenticationService authenticationService;
    private Step currentStep = Step.CHOOSE_ACTION;
    private boolean authenticated;
    private String pendingUsername;

    public AuthSession(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public AuthStepResult start() {
        currentStep = Step.CHOOSE_ACTION;
        return new AuthStepResult("请选择操作：1登录 2注册", false, null);
    }

    public AuthStepResult handleInput(String input) {
        return switch (currentStep) {
            case CHOOSE_ACTION -> handleChooseAction(input);
            case LOGIN_USERNAME -> handleLoginUsername(input);
            case LOGIN_PASSWORD -> handleLoginPassword(input);
            case REGISTER_USERNAME -> handleRegisterUsername(input);
            case REGISTER_PASSWORD -> handleRegisterPassword(input);
            case AUTHENTICATED -> new AuthStepResult("登录成功,游戏启动~", true, pendingUsername);
        };
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String currentPrompt() {
        return switch (currentStep) {
            case CHOOSE_ACTION -> "请选择操作：1登录 2注册";
            case LOGIN_USERNAME, REGISTER_USERNAME -> "请输入用户名：";
            case LOGIN_PASSWORD, REGISTER_PASSWORD -> "请输入密码：";
            case AUTHENTICATED -> "";
        };
    }

    private AuthStepResult handleChooseAction(String input) {
        if ("1".equals(input)) {
            currentStep = Step.LOGIN_USERNAME;
            return new AuthStepResult("请输入用户名：", false, null);
        }
        if ("2".equals(input)) {
            currentStep = Step.REGISTER_USERNAME;
            return new AuthStepResult("请输入用户名：", false, null);
        }
        return new AuthStepResult("输入有误请重新输入", false, null);
    }

    private AuthStepResult handleLoginUsername(String input) {
        LoginDecision decision = authenticationService.prepareLogin(input);
        if (decision.success()) {
            authenticated = true;
            currentStep = Step.AUTHENTICATED;
            pendingUsername = decision.username();
            return new AuthStepResult(decision.message(), true, decision.username());
        }
        if (decision.requirePassword()) {
            pendingUsername = input;
            currentStep = Step.LOGIN_PASSWORD;
            return new AuthStepResult(decision.message(), false, null);
        }
        currentStep = Step.CHOOSE_ACTION;
        pendingUsername = null;
        return new AuthStepResult(decision.message(), false, null);
    }

    private AuthStepResult handleLoginPassword(String input) {
        AuthenticationResult result = authenticationService.login(pendingUsername, input);
        if (result.success()) {
            authenticated = true;
            currentStep = Step.AUTHENTICATED;
            pendingUsername = result.username();
            return new AuthStepResult(result.message(), true, result.username());
        }
        currentStep = Step.CHOOSE_ACTION;
        pendingUsername = null;
        return new AuthStepResult(result.message(), false, null);
    }

    private AuthStepResult handleRegisterUsername(String input) {
        String validationMessage = authenticationService.validateNewUsername(input);
        if (validationMessage != null) {
            return new AuthStepResult(validationMessage, false, null);
        }
        pendingUsername = input;
        currentStep = Step.REGISTER_PASSWORD;
        return new AuthStepResult("请输入密码：", false, null);
    }

    private AuthStepResult handleRegisterPassword(String input) {
        AuthenticationResult result = authenticationService.register(pendingUsername, input);
        if (result.success()) {
            authenticated = true;
            currentStep = Step.AUTHENTICATED;
            pendingUsername = result.username();
            return new AuthStepResult(result.message(), true, result.username());
        }

        if ("用户名已经存在请重新输入".equals(result.message())
                || "用户名长度必须是3-16位之间".equals(result.message())
                || "只能由字母，数字组成，不能是纯数字".equals(result.message())) {
            currentStep = Step.REGISTER_USERNAME;
            pendingUsername = null;
        }
        return new AuthStepResult(result.message(), false, null);
    }
}
