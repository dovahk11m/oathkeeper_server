// com/oath/domain/locationevents/service/TrackService.java
package com.oath.domain.locationevents.service;

import com.oath.common.exception.Exception404;
import com.oath.domain.locationevents.domain.LocationEvent;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.dto.EventReq;
import com.oath.domain.locationevents.dto.TrackBatchReq;
import com.oath.domain.locationevents.dto.TrackPointReq;
import com.oath.domain.locationevents.event.GpsStationaryEvent;
import com.oath.domain.locationevents.event.LocationUpdatedEvent;
import com.oath.domain.locationevents.event.ParticipantArrivedEvent;
import com.oath.domain.locationevents.repository.LocationEventRepository;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.plan.MovementStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional("h2TransactionManager")
public class TrackService {

    private final LocationTrackRepository trackRepo;
    private final LocationEventRepository eventRepo;
    private final ParticipantRepository participantRepository;
    private final PlanJpaRepository planJpaRepository;
    private final ApplicationEventPublisher eventPublisher;
    // private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 제거

    // Redis 대신 인메모리 맵 사용
    private final Map<Long, Map<String, Object>> realtimeLocationCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> stationaryCheckCache = new ConcurrentHashMap<>();

    private static final double STATIONARY_THRESHOLD_METERS = 10.0; // 10미터 이내 이동은 정체로 간주
    private static final long STATIONARY_DURATION_MINUTES = 5; // 5분 이상 정체 시 이벤트 발생
    private static final double ARRIVAL_RADIUS_METERS = 100.0; // 도착 반경 100미터

    public int storeTracks(TrackBatchReq req) {
        if (req.points() == null || req.points().isEmpty()) return 0;

        Participant participant = participantRepository.findById(req.participantId())
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다: " + req.participantId()));

        Plan plan = participant.getPlan();

        // 약속 상태가 실시간 위치 추적에 적합한지 확인
        if (plan.getStatus() == Status.COMPLETED || plan.getStatus() == Status.CANCELED) {
            return 0;
        }

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

            // 인메모리 캐시에 최신 위치 정보 저장
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("planId", plan.getId());
            locationData.put("memberId", participant.getMember().getId());
            locationData.put("username", participant.getMember().getUsername());
            locationData.put("profileImageUrl", participant.getMember().getProfileImageUrl());
            locationData.put("lat", p.lat());
            locationData.put("lng", p.lng());
            locationData.put("lastLiveTs", ts);
            realtimeLocationCache.put(req.participantId(), locationData);


            // 참가자의 이동 상태를 MOVING으로 업데이트 (도착 상태가 아니면)
            if (participant.getMovementStatus() != MovementStatus.ARRIVED) {
                participant.setMovementStatus(MovementStatus.MOVING);
                participantRepository.save(participant);
            }


            // GPS 미변화 감지 로직
            detectGpsStationary(plan, participant, p, ts);

            // 자동 도착 처리 로직
            detectArrival(plan, participant, p, ts);

            // 이벤트 발행
            eventPublisher.publishEvent(new LocationUpdatedEvent(
                    plan.getId(),
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

    private void detectGpsStationary(Plan plan, Participant participant, TrackPointReq currentPoint, LocalDateTime currentTs) {
        // 약속 상태가 IN_PROGRESS일 때만 정체 감지 로직 수행
        if (plan.getStatus() != Status.PROGRESS) {
            return;
        }

        // 이미 도착한 참가자는 정체 감지 불필요
        if (participant.getMovementStatus() == MovementStatus.ARRIVED) {
            return;
        }

        Map<String, Object> lastStationaryData = stationaryCheckCache.getOrDefault(participant.getId(), new HashMap<>());

        Point currentLoc = new Point(currentPoint.lng(), currentPoint.lat());

        if (lastStationaryData.isEmpty() || !lastStationaryData.containsKey("lastLat")) {
            // 첫 위치 정보이거나, 이전에 정체 상태가 아니었음. 현재 위치를 기준으로 정체 시작 시간 기록
            Map<String, Object> newStationaryData = new HashMap<>();
            newStationaryData.put("lastLat", currentPoint.lat());
            newStationaryData.put("lastLng", currentPoint.lng());
            newStationaryData.put("stationaryStartTime", currentTs.toString());
            stationaryCheckCache.put(participant.getId(), newStationaryData);
            return;
        }

        double lastLat = (Double) lastStationaryData.get("lastLat");
        double lastLng = (Double) lastStationaryData.get("lastLng");
        LocalDateTime stationaryStartTime = LocalDateTime.parse((String) lastStationaryData.get("stationaryStartTime"));
        Point lastLoc = new Point(lastLng, lastLat);

        // 현재 위치와 이전 위치 간의 거리 계산
        double distance = calculateDistance(lastLoc, currentLoc);

        if (distance < STATIONARY_THRESHOLD_METERS) {
            // 정체 임계값 이내 이동: 정체 상태 지속
            long durationMinutes = ChronoUnit.MINUTES.between(stationaryStartTime, currentTs);
            if (durationMinutes >= STATIONARY_DURATION_MINUTES) {
                // 일정 시간 이상 정체 감지, 이벤트 발행
                eventPublisher.publishEvent(new GpsStationaryEvent(
                        plan.getId(),
                        participant.getId(),
                        participant.getMember().getId(),
                        participant.getMember().getUsername(),
                        lastLoc,
                        stationaryStartTime,
                        durationMinutes
                ));
                // 참가자의 이동 상태를 STATIONARY로 업데이트
                if (participant.getMovementStatus() != MovementStatus.STATIONARY) {
                    participant.setMovementStatus(MovementStatus.STATIONARY);
                    participantRepository.save(participant);
                }
                // 이벤트 발행 후 정체 상태 초기화 (중복 알림 방지)
                stationaryCheckCache.remove(participant.getId());
            }
        } else {
            // 움직임 감지: 정체 상태 초기화
            Map<String, Object> newStationaryData = new HashMap<>();
            newStationaryData.put("lastLat", currentPoint.lat());
            newStationaryData.put("lastLng", currentPoint.lng());
            newStationaryData.put("stationaryStartTime", currentTs.toString());
            stationaryCheckCache.put(participant.getId(), newStationaryData);
            // 움직임이 감지되면 이동 상태를 MOVING으로 업데이트 (정체 상태가 아니면)
            if (participant.getMovementStatus() == MovementStatus.STATIONARY) {
                participant.setMovementStatus(MovementStatus.MOVING);
                participantRepository.save(participant);
            }
        }
    }

    private void detectArrival(Plan plan, Participant participant, TrackPointReq currentPoint, LocalDateTime currentTs) {
        // 약속 상태가 IN_PROGRESS일 때만 도착 감지 로직 수행
        if (plan.getStatus() != Status.PROGRESS) {
            return;
        }

        // 이미 도착한 참가자는 다시 감지하지 않음
        if (participant.getMovementStatus() == MovementStatus.ARRIVED) {
            return;
        }

        // 약속 장소 정보가 없으면 도착 감지 불가
        if (plan.getPlaceLocation() == null) {
            return;
        }

        Point planPlaceLoc = plan.getPlaceLocation();
        Point currentLoc = new Point(currentPoint.lng(), currentPoint.lat());

        double distanceToPlace = calculateDistance(planPlaceLoc, currentLoc);

        if (distanceToPlace <= ARRIVAL_RADIUS_METERS) {
            // 도착 반경 내 진입 감지, 이벤트 발행
            eventPublisher.publishEvent(new ParticipantArrivedEvent(
                    plan.getId(),
                    participant.getId(),
                    participant.getMember().getId(),
                    participant.getMember().getUsername(),
                    currentLoc,
                    currentTs
            ));
            // 참가자의 이동 상태를 ARRIVED로 업데이트
            participant.setMovementStatus(MovementStatus.ARRIVED);
            participantRepository.save(participant);
            // 도착 감지 후 정체 상태 초기화 (더 이상 정체 감지 불필요)
            stationaryCheckCache.remove(participant.getId());
        }
    }

    // 두 지점 간의 거리 계산 (미터 단위, 간단한 근사치)
    private double calculateDistance(Point p1, Point p2) {
        final int R = 6371000; // 지구 반지름 (미터)
        double latDistance = Math.toRadians(p2.getY() - p1.getY());
        double lonDistance = Math.toRadians(p2.getX() - p1.getX());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(p1.getY())) * Math.cos(Math.toRadians(p2.getY()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 거리 (미터)
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
