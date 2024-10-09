package com.grafysi.wfdbconsole.model;

import com.grafysi.wfdbconsole.utils.Utils;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.exchange.StatusCode;
import lombok.Builder;

import java.time.Duration;
import java.util.List;

@Builder
public record ReportModel(CommandType commandType, Description description, StatusCode statusCode,
                          Duration executionTime, String failedMessage, List<Content> contents) {

    public String getCommandType() {
        return commandType.name();
    }

    public String getDescription() {
        return Utils.toJsonString(description);
    }

    public String getStatusCode() {
        return statusCode.getCode();
    }

    public String getFailedMessage() {
        return failedMessage == null || failedMessage.isBlank() ? "<empty>" : failedMessage;
    }

    public Integer getContentCount() {
        return contents.size();
    }

    public String getExecutionTime() {
        var nanos = executionTime.toNanos();
        return nanos > 100_000
                ? String.format("%.2f ms", nanos / 1_000_000.0)
                : String.format("%.1f us", nanos / 1_000.0);
    }
}


















