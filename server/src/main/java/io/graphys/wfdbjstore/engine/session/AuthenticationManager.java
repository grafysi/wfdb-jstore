package io.graphys.wfdbjstore.engine.session;

import io.graphys.wfdbjstore.engine.session.auth.AuthToken;
import io.graphys.wfdbjstore.engine.session.auth.AuthenticationException;
import io.graphys.wfdbjstore.engine.session.auth.BasicAuthToken;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class AuthenticationManager {
    private final Path credentialPath;

    private final HashMap<String, String> credentials = new HashMap<>();

    public AuthenticationManager(Path credentialPath) {
        this.credentialPath = credentialPath;
        loadCredentials();
    }

    private void loadCredentials() {
        try (var in = new Scanner(credentialPath))
        {
            in.tokens()
                    .forEach(str -> credentials.put(
                            str.split(";")[0],
                            str.split(";")[1]));
        } catch (Exception e) {
            throw new RuntimeException("Load credentials failed.");
        }
    }

    public boolean authenticate(AuthToken authToken) throws AuthenticationException {
        try {
            return switch (authToken) {
                case BasicAuthToken token -> token.password().equals(credentials.get(token.username()));
                case null, default -> false;
            };
        } catch (Exception e) {
            throw new AuthenticationException("Error when authenticate user");
        }
    }
}
