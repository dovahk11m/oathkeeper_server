package com.oath.domain.place_tag_plan;

import com.oath.domain.place_tag_plan.place.Place;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PlaceResponseDto {
    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String description;
    private String imageUrl;
    private List<String> tags;

    public PlaceResponseDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.address = place.getAddress();
        this.lat = place.getLat();
        this.lng = place.getLng();
        this.description = place.getDescription();
        this.imageUrl = place.getImageUrl();
        this.tags = place.getPlaceTags().stream()
                .map(placeTag -> placeTag.getTag().getName())
                .collect(Collectors.toList());
    }
}
