package com.oath.recommend_domain.place.event_listener;

import lombok.Data;

@Data
public class PlaceConfirmedEvent {

    private final Long placeId;

    public PlaceConfirmedEvent(Long planId) {
        this.placeId = planId;
    }
}
