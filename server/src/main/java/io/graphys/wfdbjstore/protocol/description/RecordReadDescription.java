package io.graphys.wfdbjstore.protocol.description;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Jacksonized
public record RecordReadDescription(Integer limit, String name, String[] textInfoPattern) implements Description {
}









