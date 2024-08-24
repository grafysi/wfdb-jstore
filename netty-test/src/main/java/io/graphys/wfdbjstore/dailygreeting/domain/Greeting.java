package io.graphys.wfdbjstore.dailygreeting.domain;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Greeting {
    private LocalDate sentDate;
    private String customerName;
    private String forService;

    @Override
    public String toString() {
        return String.format("[%s]: Hi %s, hope you enjoy %s", sentDate, customerName, forService);
    }
}
