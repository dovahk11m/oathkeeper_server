package com.oath.domain.locationevents.event.listener;

import com.oath.domain.locationevents.dto.LiveLocationDto;
import com.oath.domain.locationevents.event.LocationUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationUpdateListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleLocationUpdate(LocationUpdatedEvent event) {
        String destination = "/topic/plans/" + event.getPlanId() + "/live";
        LiveLocationDto payload = new LiveLocationDto(event);

        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.info("[LIVE-LOCATION] Plan ID: {} 로 위치 정보 전송 성공. Member ID: {}", event.getPlanId(), event.getMemberId());
        } catch (Exception e) {
            log.error("[LIVE-LOCATION] Plan ID: {} 로 위치 정보 전송 실패. Member ID: {}. Error: {}",
                    event.getPlanId(), event.getMemberId(), e.getMessage());
        }
    }
}
