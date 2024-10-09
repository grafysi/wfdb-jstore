package io.graphys.wfdbjstore.protocol.exchange;

public enum ConnectionType {

    NOT_INITIALIZED(new MediaType[] {MediaType.APPLICATION_JSON}, new MediaType[] {MediaType.APPLICATION_JSON}),
    METADATA(new MediaType[] {MediaType.APPLICATION_JSON}, new MediaType[] {MediaType.APPLICATION_JSON}),
    SIGNAL(new MediaType[] {MediaType.APPLICATION_JSON}, new MediaType[] {MediaType.APPLICATION_SIGNAL_STREAM});

    private final MediaType[] descriptionMediaTypes;
    private final MediaType[] reportMediaTypes;

    ConnectionType(MediaType[] descriptionMediaTypes, MediaType[] reportMediaTypes) {
        this.descriptionMediaTypes = descriptionMediaTypes;
        this.reportMediaTypes = reportMediaTypes;
    }

    public boolean commandSupports(MediaType mediaType) {
        for (var supportedType: descriptionMediaTypes) {
            if (supportedType.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    public MediaType[] getReportMediaTypes() {
        return reportMediaTypes;
    }

    public MediaType[] getDescriptionMediaTypes() {
        return descriptionMediaTypes;
    }

    public boolean reportSupports(MediaType mediaType) {
        for (var supportedType: reportMediaTypes) {
            if (supportedType.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }
}




























