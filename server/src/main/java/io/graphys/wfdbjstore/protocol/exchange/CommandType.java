package io.graphys.wfdbjstore.protocol.exchange;

import io.graphys.wfdbjstore.protocol.content.*;
import io.graphys.wfdbjstore.protocol.description.*;

public enum CommandType {
    INIT_METADATA_CONNECTION("IMCN", ReportSize.MONO, ConnectionType.NOT_INITIALIZED, MetadataConnectionDescription.class, MetadataConnectionContent.class),
    READ_METADATA_RECORD("RMRC", ReportSize.POLY, ConnectionType.METADATA, RecordReadDescription.class, RecordReadContent.class),
    READ_METADATA_PATH_INFO("RMPI", ReportSize.POLY, ConnectionType.METADATA, null, null),
    INIT_SIGNAL_CONNECTION("ISCN", ReportSize.MONO, ConnectionType.NOT_INITIALIZED, SignalConnectionDescription.class, SignalConnectionContent.class),
    READ_SIGNAL_FLOW("RSFL", ReportSize.POLY, ConnectionType.SIGNAL, ReadSignalFlowDescription.class, ReadSignalFlowContent.class),
    READ_SIGNAL_SEEK("RSSK", ReportSize.MONO, ConnectionType.SIGNAL, ReadSignalSeekDescription.class, ReadSignalSeekContent.class),
    WRITE_SIGNAL_POSITION("WSSP", ReportSize.MONO, ConnectionType.SIGNAL, null, null),
    WRITE_SIGNAL_ELEMENTS("WSSE", ReportSize.MONO, ConnectionType.SIGNAL, null, null);

    public static final int COMMAND_CODE_SIZE = 4;

    private final String code;

    private final ReportSize reportSize;

    private final ConnectionType connectionType;

    private final Class<? extends Description> descriptionClass;

    private final Class<? extends Content> contentClass;

    CommandType(String code, ReportSize reportSize, ConnectionType connectionType,
                Class<? extends Description> descriptionClass, Class<? extends Content> contentClass) {
        this.code = code;
        this.reportSize = reportSize;
        this.connectionType = connectionType;
        this.descriptionClass = descriptionClass;
        this.contentClass = contentClass;
    }

    public String getCode() {
        return code;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public ReportSize getReportSize() {
        return reportSize;
    }

    public Class<?> getDescriptionClass() {
        return descriptionClass;
    }

    public Class<?> getContentClass() {
        return contentClass;
    }

    public static CommandType getInstanceOf(String code) {
        for (var type: CommandType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}












































