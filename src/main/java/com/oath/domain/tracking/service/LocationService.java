package com.oath.domain.tracking.service;

import com.oath.domain.tracking.dto.TrackingDto;
import com.oath.domain.tracking.model.LocationTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final SimpMessagingTemplate messaging; // /topic 브로커로 발송
    private final Map<Long, List<LocationTrack>> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    /** 위치 저장 + 즉시 브로드캐스트(/topic/room/{planId}) */
    public void upload(TrackingDto dto, Optional<Long> memberIdFromToken) {
        dto.ensureDefaults();

        long memberId = memberIdFromToken.orElseGet(() ->
                (dto.getMemberId() > 0 ? dto.getMemberId() : 0L));

        LocationTrack t = LocationTrack.builder()
                .id(seq.getAndIncrement())
                .planId(dto.getPlanId())
                .memberId(memberId)
                .lat(dto.getLat())
                .lng(dto.getLng())
                .accuracy(dto.getAccuracy())
                .speed(dto.getSpeed())
                .heading(dto.getHeading())
                .ts(Optional.ofNullable(dto.getTs()).orElse(Instant.now()))
                .build();

        store.computeIfAbsent(t.getPlanId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(t);

        Map<String, Object> payload = Map.of(
                "planId", t.getPlanId(),
                "memberId", t.getMemberId(),
                "lat", t.getLat(),
                "lng", t.getLng(),
                "accuracy", t.getAccuracy(),
                "speed", t.getSpeed(),
                "heading", t.getHeading(),
                "ts", t.getTs().toString()
        );

        // ⬇️ 여기만 바뀜: 실패해도 API는 200이 되도록 보호
        try {
            messaging.convertAndSend("/topic/room/" + t.getPlanId(), payload);
        } catch (Exception e) {
            log.warn("[tracking] broadcast failed: {}", e.toString());
        }
    }

    /** bbox 안에서 멤버별 최신 1건 */
    public List<Map<String, Object>> listRecent(long planId, double minLng, double minLat, double maxLng, double maxLat){
        List<LocationTrack> list = store.getOrDefault(planId, List.of());

        LinkedHashMap<Long, LocationTrack> latest = new LinkedHashMap<>();
        ListIterator<LocationTrack> it = list.listIterator(list.size());
        while (it.hasPrevious()) {
            LocationTrack t = it.previous();
            if (!latest.containsKey(t.getMemberId()) &&
                    contains(minLng, minLat, maxLng, maxLat, t.getLng(), t.getLat())) {
                latest.put(t.getMemberId(), t);
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (LocationTrack t : latest.values()) {
            out.add(Map.of(
                    "planId", t.getPlanId(),
                    "memberId", t.getMemberId(),
                    "lat", t.getLat(),
                    "lng", t.getLng(),
                    "accuracy", t.getAccuracy(),
                    "speed", t.getSpeed(),
                    "heading", t.getHeading(),
                    "ts", t.getTs().toString()
            ));
        }
        return out;
    }

    private boolean contains(double minLng, double minLat, double maxLng, double maxLat, double xLng, double xLat){
        return xLat >= minLat && xLat <= maxLat && xLng >= minLng && xLng <= maxLng;
    }
}
