// com/oath/domain/locationevents/service/TrackService.java
package com.oath.domain.locationevents.service;

import com.oath.common.exception.Exception404;
import com.oath.domain.locationevents.domain.LocationEvent;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.dto.EventReq;
import com.oath.domain.locationevents.dto.TrackBatchReq;
import com.oath.domain.locationevents.dto.TrackPointReq;
import com.oath.domain.locationevents.event.LocationUpdatedEvent;
import com.oath.domain.locationevents.repository.LocationEventRepository;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional("h2TransactionManager")
public class TrackService {

    private final LocationTrackRepository trackRepo;
    private final LocationEventRepository eventRepo;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;

    public int storeTracks(TrackBatchReq req) {
        if (req.points() == null || req.points().isEmpty()) return 0;

        Participant participant = participantRepository.findById(req.participantId())
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다: " + req.participantId()));

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

            // 이벤트 발행
            eventPublisher.publishEvent(new LocationUpdatedEvent(
                    participant.getPlan().getId(),
                    participant.getMember().getId(),
                    participant.getMember().getUsername(),
                    participant.getMember().getProfileImageUrl(),
                    p.lat(),
                    p.lng(),
                    ts
            ));
        }
        return stored;
    }

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
