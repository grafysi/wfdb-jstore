package io.graphys.wfdbjstore.engine.metadataquery;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class PathInfo implements Metadata {
    private String recordName;
    private String[] pathSegments;
}
