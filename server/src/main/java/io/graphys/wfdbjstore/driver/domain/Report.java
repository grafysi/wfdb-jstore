package io.graphys.wfdbjstore.driver.domain;

import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class Report {
    @Getter
    private final CommandType commandType;

    @Setter @Getter
    private StatusCode statusCode;

    @Setter @Getter
    private String failedMessage;

    @Getter
    private final List<Content> contentList = new LinkedList<>();

    public Report(CommandType commandType) {
        this.commandType = commandType;
    }

    public void addContent(Content content) {
        contentList.add(content);
    }
}


































