package io.graphys.wfdbjstore.engine.session.auth;

import lombok.Builder;

@Builder
public record BasicAuthToken(String username, String password) implements AuthToken {

}
