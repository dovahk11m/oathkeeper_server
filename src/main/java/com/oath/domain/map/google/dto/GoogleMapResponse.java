package com.oath.domain.map.google.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "Matrix API Response", description = "구글 Matrix API의 Response를 정의하는 DTO 입니다.", contentMediaType = "application/json")
public record GoogleMapResponse(
        List<GoogleMapRouteMatrixElement> matrixElements
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoogleMapRouteMatrixElement(
            @Schema(description = "계산에 이용된 각 사용자를 Index로 나타낸 값입니다. 필수 응답 여부는 true 입니다.", nullable = false, example = "1")
            Long originIndex,

            @Schema(description = "계산에 이용된 각 도착지를 Index로 나타낸 값입니다. 필수 응답 여부는 true 입니다.", nullable = false, example = "0")
            Long destinationIndex,

            @Schema(description = "오류 상태를 나타낸 객체입니다. 객체 자체의 필수 응답 여부는 true 입니다.", nullable = false)
            Status status,

            @Schema(description = "각 사용자와 도착지 간의 거리를 나타낸 미터(meter) 단위의 필드입니다. 필수 응답 여부는 true 입니다.", nullable = false, example = "508")
            Long distanceMeters,

            @Schema(description = "각 사용자와 도착지 간의 소요 시간을 나타낸 초(s) 단위의 필드입니다. 필수 응답 여부는 true 입니다.", nullable = false, example = "418s")
            String duration,

            @Schema(description = "상태와 관계없이 경로를 찾았는지 여부를 나타냅니다. 필수 응답 여부는 true 입니다.", nullable = false, example = "ROUTE_EXISTS")
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
                @Schema(description = "오류 상태 코드를 나타낸 필드입니다. 필수 응답 여부는 false 입니다.", nullable = true)
                Long code,

                @Schema(description = "오류 메시지를 나타낸 필드입니다. 필수 응답 여부는 false 입니다.", nullable = true)
                String message
        ) {
            public Status() {
                this(null, null);
            }
        }
    }
}
