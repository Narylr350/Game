package util;

public class CredentialPolicy {

    public String validateUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 16) {
            return "用户名长度必须是3-16位之间";
        }
        int[] counts = countCharacters(username);
        if (counts[0] <= 0 || counts[2] != 0) {
            return "只能由字母，数字组成，不能是纯数字";
        }
        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.length() < 3 || password.length() > 8) {
            return "密码长度必须是3-8位之间";
        }
        int[] counts = countCharacters(password);
        if (counts[0] <= 0 || counts[1] <= 0 || counts[2] != 0) {
            return "只能由字母+数字组成，不能有其他";
        }
        return null;
    }

    private int[] countCharacters(String value) {
        int letters = 0;
        int numbers = 0;
        int others = 0;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isLetter(current)) {
                letters++;
            } else if (Character.isDigit(current)) {
                numbers++;
            } else {
                others++;
            }
        }
        return new int[]{letters, numbers, others};
    }
}
