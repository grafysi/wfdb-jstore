package io.graphys.wfdbjstore.server;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public enum ConnectionAttr {
    REPORT_CONSUMER ("REPORT_CONSUMER"),
    SESSION_ID ("SESSION_ID"),
    REPORT_MEDIA_TYPE ("REPORT_MEDIA_TYPE"),
    CONNECTION_TYPE ("CONNECTION_TYPE"),
    EXECUTION_DISPATCHER ("EXECUTION_DISPATCHER"),
    BASIC_REPORT_PROCESSOR ("BASIC_REPORT_PROCESSOR")

    ;

    private final String key;

    ConnectionAttr(String key) {
        this.key = key;
    }

    public <T> Attribute<T> ofChannel(Channel c) {
        return c.attr(AttributeKey.valueOf(key));
    }

    public String key() {
        return key;
    }
}
