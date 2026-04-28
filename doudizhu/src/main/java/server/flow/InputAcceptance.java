package server.flow;

public record InputAcceptance(boolean accepted, String message) {
    public static InputAcceptance accept() {
        return new InputAcceptance(true, "");
    }

    public static InputAcceptance reject(String message) {
        return new InputAcceptance(false, message);
    }
}
