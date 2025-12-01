package com.oath.domain.place_tag_plan.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class PlaceResponseDto {



    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PlaceDto {
        private List<PlaceSearchDto> documents;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class PlaceSearchDto {
            @JsonProperty("place_name")
            private String placeName;
            @JsonProperty("address_name")
            private String addressName;
            private String phone;
            private Double x; // 경도
            private Double y; // 위도
        }
    }

}
