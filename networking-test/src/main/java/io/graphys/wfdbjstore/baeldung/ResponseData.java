package io.graphys.wfdbjstore.baeldung;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseData {
    private int intValue;

    @Override
    public String toString() {
        return "IntValue: " + intValue;
    }
}
