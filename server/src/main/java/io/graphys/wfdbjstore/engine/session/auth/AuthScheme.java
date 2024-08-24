package io.graphys.wfdbjstore.engine.session.auth;

public enum AuthScheme {
    BASIC("basic");

    private String value;

    AuthScheme(String scheme) {
        this.value = scheme;
    }

    public static AuthScheme getInstanceFor(String value) {
        for (var scheme: AuthScheme.values()) {
            if (scheme.getValue().equals(value)) {
                return scheme;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
