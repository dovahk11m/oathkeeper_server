package com.oath.domain.locationevents.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LocationUpdatedEvent {

    private final Long planId;
    private final Long memberId;
    private final String username;
    private final String profileImageUrl;
    private final Double lat;
    private final Double lng;
    private final LocalDateTime lastLiveTs;

    public LocationUpdatedEvent(Long planId, Long memberId, String username, String profileImageUrl, Double lat, Double lng, LocalDateTime lastLiveTs) {
        this.planId = planId;
        this.memberId = memberId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.lat = lat;
        this.lng = lng;
        this.lastLiveTs = lastLiveTs;
    }
}
