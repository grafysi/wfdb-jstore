package io.graphys.wfdbjstore.protocol.exchange;

public enum MediaType {
    APPLICATION_JSON("application/json"),
    APPLICATION_SIGNAL_STREAM("application/signal-stream"),
    APPLICATION_PROTOBUF("application/protobuf");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MediaType getInstanceOf(String value) {
        for (var type: MediaType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
