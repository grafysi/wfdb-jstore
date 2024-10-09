package io.graphys.wfdbjstore.protocol.exchange;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ExchangeConvention {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String REPORT_END_TOKEN = "xREP";
    public static final String CONTENT_END_TOKEN = "xCNT";
}
