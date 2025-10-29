package com.oath.domain.visitors;

import lombok.Data;

import java.time.LocalDate;

public class VisitorResponse {

    @Data
    public static class PeriodCount {
        private LocalDate date;
        private Long count;
    }

}
