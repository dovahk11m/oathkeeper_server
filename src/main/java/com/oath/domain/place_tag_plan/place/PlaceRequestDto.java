package com.oath.domain.place_tag_plan.place;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class PlaceRequestDto {

    @Getter
    @Setter
    public static class CreatePlaceDto {
        private String name;
        private String address;
        private Double lat;
        private Double lng;
        private String description;
        private String imageUrl;
    }

    @Getter
    @Setter
    public static class UpdatePlaceDto {
        private String name;
        private String address;
        private Double lat;
        private Double lng;
        private String description;
        private String imageUrl;
    }

}
