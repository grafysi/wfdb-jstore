package io.graphys.wfdbjstore.protocol.exchange;

public enum StatusCode {
    SERVER_ERROR("500"),
    CLIENT_ERROR("400"),
    SUCCESS("200")
    ;

    private final String code;

    StatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static StatusCode getInstanceOf(String code) {
        for (var sCode: StatusCode.values()) {
            if (sCode.getCode().equals(code)) {
                return sCode;
            }
        }
        return null;
    }
}
