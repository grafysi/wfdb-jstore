package io.graphys.wfdbjstore.dailygreeting.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
    private int id;
    private String name;
    private String referredService;
    private LocalDate joinDate;

    @Override
    public String toString() {
        return String.format("Id: %s; Name: %s; ReferredService: %s; JoinDate: %s",
                id, name, referredService, joinDate);
    }
}


