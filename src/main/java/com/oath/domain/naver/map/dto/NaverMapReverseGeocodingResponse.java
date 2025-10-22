package com.oath.domain.naver.map.dto;

import java.util.List;

public record NaverMapReverseGeocodingResponse(
        Status status,
        List<Results> results
) {
    public record Status(
            Long code,
            String name,
            String message
    ) {
    }

    public record Results(
            String name,
            Code code,
            Region region
    ) {
        public record Code(
                Long id,
                String type,
                Long mappingId
        ) {
        }

        public record Region(
                Area area0,
                Area area1,
                Area area2,
                Area area3,
                Area area4
        ) {
            public record Area(
                    String name,
                    Coords coords
            ) {

                public record Coords(
                        Center center
                ) {

                    public record Center(
                            String crs,
                            double x,
                            double y
                    ) {
                    }
                }
            }
        }
    }
}