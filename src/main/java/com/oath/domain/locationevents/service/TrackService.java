// com/oath/domain/locationevents/service/TrackService.java
package com.oath.domain.locationevents.service;

import com.oath.domain.locationevents.domain.LocationEvent;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.dto.EventReq;
import com.oath.domain.locationevents.dto.TrackBatchReq;
import com.oath.domain.locationevents.dto.TrackPointReq;
import com.oath.domain.locationevents.repository.LocationEventRepository;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final LocationTrackRepository trackRepo;
    private final LocationEventRepository eventRepo;

    @Transactional
    public int storeTracks(TrackBatchReq req) {
        if (req.points() == null || req.points().isEmpty()) return 0;
        int stored = 0;
        for (TrackPointReq p : req.points()) {
            LocalDateTime ts = p.ts() != null ? p.ts() : LocalDateTime.now();
            LocationTrack e = LocationTrack.builder()
                    .participantId(req.participantId())
                    .ts(ts)
                    .lat(p.lat())
                    .lng(p.lng())
                    .speedMps(p.speedMps())
                    .accuracyM(p.accuracyM())
                    .source(p.source() != null ? p.source() : "BG")
                    .isMockLocation(Boolean.TRUE.equals(p.isMock()))
                    .build();
            trackRepo.save(e);
            stored++;
        }
        return stored;
    }

    @Transactional
    public void storeEvent(EventReq req) {
        LocalDateTime ts = req.ts() != null ? req.ts() : LocalDateTime.now();
        LocationEvent e = LocationEvent.builder()
                .participantId(req.participantId())
                .eventType(req.eventType())
                .ts(ts)
                .lat(req.lat())
                .lng(req.lng())
                .build();
        eventRepo.save(e);
    }
}
