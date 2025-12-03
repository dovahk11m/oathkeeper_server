package com.oath.domain.place_tag_plan.place.dto;

import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.plan.request.ParticipantResponse;
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
        private List<RecommendDetailPlace> centerAvgPlace;
        private List<RecommendDetailPlace> equalAvgPlace;

        public RecommendListPlace(List<RecommendDetailPlace> centerAvgPlace, List<RecommendDetailPlace> equalAvgPlace) {
            this.centerAvgPlace = centerAvgPlace;
            this.equalAvgPlace = equalAvgPlace;
        }
    }

    @Data
    @Builder
    public static class RecommendDetailPlace {

        private ParticipantResponse participant;
        private DetailPlace destination;
        private Long distance;
        private String duration;
    }

}
