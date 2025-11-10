package com.oath.domain.place_tag_plan.place.dto;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.plan.domain.Participant;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

    @Data
    @Builder
    public static class RecommendListPlace {
        private List<RecommendDetailPlace> recommendedPlaces;

        public RecommendListPlace(List<RecommendDetailPlace> detailPlaces) {
            this.recommendedPlaces = detailPlaces;
        }
    }

    @Data
    @Builder
    public static class RecommendDetailPlace {

        private Participant participant;
        private Place destination;
        private Long distance;
        private String duration;
    }

}
