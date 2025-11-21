package com.oath.domain.locationevents.event.listener;

import com.oath.domain.locationevents.dto.ParticipantArrivedNotificationDto;
import com.oath.domain.locationevents.event.ParticipantArrivedEvent;
import com.oath.domain.plan.MovementStatus;
import com.oath.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParticipantArrivedListener {

    private final PlanService planService;
    private final SimpMessagingTemplate messagingTemplate; // SimpMessagingTemplate 주입

    @Async
    @Transactional(value = "h2TransactionManager", propagation = Propagation.REQUIRES_NEW) // 트랜잭션 관리자 명시적 지정 (value= 추가)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantArrivedEvent(ParticipantArrivedEvent event) {
        log.info(
                "[참가자 자동 도착 이벤트 수신: 플랜ID='{}', 참가자ID='{}', 사용자='{}']",
                event.getPlanId(),
                event.getParticipantId(),
                event.getUsername()
        );

        // PlanService를 통해 참가자의 도착 상태를 업데이트
        planService.updateParticipantArrivalStatus(
                event.getParticipantId(),
                MovementStatus.ARRIVED,
                event.getArrivalTime()
        );

        // 클라이언트 WebSocket 알림 전송
        String websocketDestination = "/topic/plans/" + event.getPlanId() + "/events";
        String messageContent = String.format(
                "'%s'님이 약속 장소에 도착했습니다!",
                event.getUsername()
        );
        ParticipantArrivedNotificationDto notificationDto = new ParticipantArrivedNotificationDto(
                event.getPlanId(),
                event.getParticipantId(),
                event.getMemberId(),
                event.getUsername(),
                event.getLat(), // lat
                event.getLng(), // lng
                event.getArrivalTime(),
                messageContent
        );
        messagingTemplate.convertAndSend(
                websocketDestination,
                notificationDto
        );
        log.info(
                "[WebSocket 참가자 도착 알림 전송 성공 to: {}]",
                websocketDestination
        );

        // TODO: 도착 후 벌금 계산 로직 트리거 등 추가 후속 처리
    }
}
