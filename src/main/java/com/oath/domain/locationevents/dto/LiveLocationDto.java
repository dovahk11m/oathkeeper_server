package com.oath.domain.locationevents.dto;

import com.oath.domain.locationevents.event.LocationUpdatedEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LiveLocationDto {
    private final Long memberId;
    private final String username;
    private final String profileImageUrl;
    private final Double lat;
    private final Double lng;
    private final LocalDateTime lastLiveTs;

    public LiveLocationDto(LocationUpdatedEvent event) {
        this.memberId = event.getMemberId();
        this.username = event.getUsername();
        this.profileImageUrl = event.getProfileImageUrl();
        this.lat = event.getLat();
        this.lng = event.getLng();
        this.lastLiveTs = event.getLastLiveTs();
    }
}
