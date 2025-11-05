package com.oath.domain.map.google.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public record GoogleMapResponse(
        List<GoogleMapRouteMatrixElement> matrixElements
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoogleMapRouteMatrixElement(
            Long originIndex,
            Long destinationIndex,
            Status status,
            Long distanceMeters,
            String duration,
            Condition condition
    ) {
        public enum Condition {
            ROUTE_EXISTS,
            ROUTE_NOT_FOUND,
            NO_ROUTE_FOUND,
            ROUTE_MATRIX_ELEMENT_CONDITION_UNSPECIFIED;

            // API가 어떤 문자열(대소문자 등)로 주든 안전하게 Enum으로 변환
            @JsonCreator
            public static Condition fromString(String value) {
                if (value == null) {
                    return ROUTE_MATRIX_ELEMENT_CONDITION_UNSPECIFIED;
                }
                try {
                    return Condition.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ROUTE_MATRIX_ELEMENT_CONDITION_UNSPECIFIED; // 모르는 값이 오면 기본값 처리
                }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Status(
                Integer code,
                String message
        ) {
            public Status() {
                this(null, null);
            }
        }
    }
}
