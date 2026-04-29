package server.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class SocketAuthenticator {
    private final AuthenticationService authenticationService;

    public SocketAuthenticator(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public String authenticate(BufferedReader reader, PrintWriter writer) {
        AuthSession session = new AuthSession(authenticationService);
        writer.println(session.start().message());

        while (!session.isAuthenticated()) {
            String input;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                return null;
            }

            if (input == null) {
                return null;
            }

            AuthStepResult result = session.handleInput(input.trim());
            writer.println(result.message());
            if (result.exitRequested()) {
                return null;
            }
            if (result.authenticated()) {
                return result.username();
            }
            String prompt = session.currentPrompt();
            if (!prompt.isEmpty() && !prompt.equals(result.message())) {
                writer.println(prompt);
            }
        }

        return null;
    }
}
