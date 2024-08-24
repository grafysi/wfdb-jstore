package io.graphys.wfdbjstore.engine.session.auth;

public class AuthenticationUtils {
    public static BasicAuthToken newBasicAuthToken(String token) {
        var splits = token.split(";");
        return BasicAuthToken
                .builder()
                .username(splits[0])
                .password(splits[1])
                .build();
    }

    public static AuthToken newAuthToken(String scheme, String token) throws UnsupportedSchemeException {
        var authScheme = AuthScheme.getInstanceFor(scheme);
        if (authScheme == null) {
            throw new UnsupportedSchemeException("Currently not support auth scheme:" + scheme);
        }

        return switch (authScheme) {
            case BASIC -> {
                var splits = token.split(";");
                yield BasicAuthToken.builder()
                        .username(splits[0])
                        .password(splits[1])
                        .build();
            }
        };
    }
}
