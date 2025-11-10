package com.oath.domain.place_tag_plan.place.dto;

import com.oath.domain.place_tag_plan.place.Place;
import lombok.Builder;
import lombok.Data;

public class PlaceResponse {

    @Data
    @Builder
    public static class DetailPlace {
        private Long id;
        private String name;
        private String address;
        private Double lat;
        private Double lng;
        private String description;

        public static PlaceResponse.DetailPlace of(Place place) {
            return DetailPlace.builder()
                    .id(place.getId())
                    .name(place.getName())
                    .address(place.getAddress())
                    .lat(place.getLat())
                    .lng(place.getLng())
                    .description(place.getDescription())
                    .build();
        }
    }
}
