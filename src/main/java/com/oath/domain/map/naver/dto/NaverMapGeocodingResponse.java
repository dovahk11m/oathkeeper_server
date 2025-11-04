package com.oath.domain.map.naver.dto;

import java.util.List;

public record NaverMapGeocodingResponse(
        String status,
        Meta meta,
        List<Addresses> addresses,
        String errorMessage
) {
    public record Meta(
            Long totalCount,
            Long page,
            Long count
    ) {
    }

    public record Addresses(
            String roadAddress,
            String jibunAddress,
            String englishAddress,
            List<AddressElements> addressElements,
            double x,
            double y,
            double distance
    ) {
        public record AddressElements(
                List<String> types,
                String longName,
                String shortName,
                String code
        ) {
        }
    }
}
