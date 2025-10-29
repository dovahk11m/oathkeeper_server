package com.oath.domain.visitors;

import lombok.Data;

import java.time.LocalDate;

public class VisitorRequest {

    @Data
    public static class VisitCountDto {
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
