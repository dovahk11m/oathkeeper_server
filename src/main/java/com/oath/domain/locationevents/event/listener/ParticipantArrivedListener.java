package com.oath.domain.locationevents.event.listener;

import com.oath.domain.locationevents.event.ParticipantArrivedEvent;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantArrivedEvent(ParticipantArrivedEvent event) {
        log.info("[참가자 자동 도착 이벤트 수신: 플랜ID='{}', 참가자ID='{}', 사용자='{}']",
                event.getPlanId(), event.getParticipantId(), event.getUsername());

        // PlanService를 통해 참가자의 도착 상태를 업데이트
        planService.updateParticipantArrivalStatus(event.getParticipantId(), ParticipantStatus.ARRIVED, event.getArrivalTime());

        // TODO: 도착 후 벌금 계산 로직 트리거 등 추가 후속 처리
    }
}
