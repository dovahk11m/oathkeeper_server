package com.oath.domain.visitors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorResponse {
        private LocalDate date;
        private Long visitors;
}
