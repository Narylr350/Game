package server.auth;

public class AuthSession {
    private enum Step {
        CHOOSE_ACTION,
        LOGIN_USERNAME,
        LOGIN_PASSWORD,
        REGISTER_USERNAME,
        REGISTER_PASSWORD,
        REGISTER_CONFIRM_PASSWORD,
        AUTHENTICATED
    }

    private static final String WELCOME_MENU = String.join("\n",
            "游戏的登录注册页面打开了",
            "╔════════════════════════════════╗",
            "    🎮 欢迎来到三人斗地主 🎮   ",
            "╚════════════════════════════════╝",
            "登录/注册输入时输入 exit 返回功能菜单",
            "请选择操作：1登录 2注册 3退出"
    );
    private static final String EXIT = "exit";

    private final AuthenticationService authenticationService;
    private Step currentStep = Step.CHOOSE_ACTION;
    private boolean authenticated;
    private String pendingUsername;
    private String pendingPassword;

    public AuthSession(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public AuthStepResult start() {
        currentStep = Step.CHOOSE_ACTION;
        pendingUsername = null;
        pendingPassword = null;
        return new AuthStepResult(WELCOME_MENU, false, null);
    }

    public AuthStepResult handleInput(String input) {
        if (isExit(input)) {
            return handleExit();
        }
        return switch (currentStep) {
            case CHOOSE_ACTION -> handleChooseAction(input);
            case LOGIN_USERNAME -> handleLoginUsername(input);
            case LOGIN_PASSWORD -> handleLoginPassword(input);
            case REGISTER_USERNAME -> handleRegisterUsername(input);
            case REGISTER_PASSWORD -> handleRegisterPassword(input);
            case REGISTER_CONFIRM_PASSWORD -> handleRegisterConfirmPassword(input);
            case AUTHENTICATED -> new AuthStepResult("登录成功,游戏启动~", true, pendingUsername);
        };
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String currentPrompt() {
        return switch (currentStep) {
            case CHOOSE_ACTION -> WELCOME_MENU;
            case LOGIN_USERNAME, REGISTER_USERNAME -> "请输入用户名（输入 exit 返回功能菜单）：";
            case LOGIN_PASSWORD, REGISTER_PASSWORD -> "请输入密码（输入 exit 返回功能菜单）：";
            case REGISTER_CONFIRM_PASSWORD -> "请再次输入密码（输入 exit 返回功能菜单）：";
            case AUTHENTICATED -> "";
        };
    }

    private AuthStepResult handleChooseAction(String input) {
        if ("1".equals(input)) {
            currentStep = Step.LOGIN_USERNAME;
            return new AuthStepResult("请输入用户名（输入 exit 返回功能菜单）：", false, null);
        }
        if ("2".equals(input)) {
            currentStep = Step.REGISTER_USERNAME;
            return new AuthStepResult("请输入用户名（输入 exit 返回功能菜单）：", false, null);
        }
        if ("3".equals(input)) {
            return AuthStepResult.exit("用户选择了退出操作");
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
            pendingPassword = null;
            return new AuthStepResult(result.message(), true, result.username());
        }
        currentStep = Step.CHOOSE_ACTION;
        pendingUsername = null;
        pendingPassword = null;
        return new AuthStepResult(result.message(), false, null);
    }

    private AuthStepResult handleRegisterUsername(String input) {
        String validationMessage = authenticationService.validateNewUsername(input);
        if (validationMessage != null) {
            return new AuthStepResult(validationMessage, false, null);
        }
        pendingUsername = input;
        currentStep = Step.REGISTER_PASSWORD;
        return new AuthStepResult("请输入密码（输入 exit 返回功能菜单）：", false, null);
    }

    private AuthStepResult handleRegisterPassword(String input) {
        String passwordMessage = authenticationService.validatePassword(input);
        if (passwordMessage != null) {
            return new AuthStepResult(passwordMessage, false, null);
        }
        pendingPassword = input;
        currentStep = Step.REGISTER_CONFIRM_PASSWORD;
        return new AuthStepResult("请再次输入密码（输入 exit 返回功能菜单）：", false, null);
    }

    private AuthStepResult handleRegisterConfirmPassword(String input) {
        if (!pendingPassword.equals(input)) {
            pendingPassword = null;
            currentStep = Step.REGISTER_PASSWORD;
            return new AuthStepResult("两次密码输入不一致请重新输入", false, null);
        }

        AuthenticationResult result = authenticationService.register(pendingUsername, pendingPassword);
        if (result.success()) {
            authenticated = true;
            currentStep = Step.AUTHENTICATED;
            pendingUsername = result.username();
            pendingPassword = null;
            return new AuthStepResult(result.message(), true, result.username());
        }

        if ("用户名已经存在请重新输入".equals(result.message())
                || "用户名长度必须是3-16位之间".equals(result.message())
                || "只能由字母，数字组成，不能是纯数字".equals(result.message())) {
            currentStep = Step.REGISTER_USERNAME;
            pendingUsername = null;
            pendingPassword = null;
        }
        return new AuthStepResult(result.message(), false, null);
    }

    private boolean isExit(String input) {
        return EXIT.equalsIgnoreCase(input);
    }

    private AuthStepResult handleExit() {
        if (currentStep == Step.CHOOSE_ACTION) {
            return AuthStepResult.exit("用户选择了退出操作");
        }
        currentStep = Step.CHOOSE_ACTION;
        pendingUsername = null;
        pendingPassword = null;
        return new AuthStepResult(WELCOME_MENU, false, null);
    }
}
