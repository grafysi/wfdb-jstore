package io.graphys.wfdbjstore.driver.domain;

import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString
public final class Command {
    private final CommandType commandType;
    private final Description description;

    @Builder
    @Jacksonized
    public Command(CommandType commandType, Description description) {
        this.commandType = commandType;
        this.description = description;
    }
}
