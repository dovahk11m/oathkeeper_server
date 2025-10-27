package com.oath.domain.plan.event.listener;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.plan.event.DepartureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class DepartureListener {
    private final AlarmFactory alarmFactory;

    @Value("${notification.policy.on-departure:${notification.policy.default}}")
    private String notificationType;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // 데이터베이스에 커밋 성공했을때만 리스너가 실행된다
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDepartureEvent(DepartureEvent event) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            log.error("출발 알림 10초 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return;
        }

        String departedUserName = event.getDepartedParticipant().getMember().getUsername();
        String planTitle = event.getPlan().getTitle();

        log.info(
                "[출발 알림 이벤트 수신: 약속='{}', 출발자='{}', 알림 방식='{}']",
                planTitle,
                departedUserName,
                notificationType
        );

        AlarmSender sender = alarmFactory.findSender(notificationType);

        event.getOtherParticipants().forEach(participant -> {
            String subject = String.format("[Oath] '%s' 약속 출발 알림", planTitle);
            String content = String.format(
                    "'%s'님이 출발했습니다. 약속 시간: %s",
                    departedUserName,
                    event.getPlan().getPlanDatetime()
            );
            AlarmDTO alarmDTO = new AlarmDTO(
                    participant.getMember().getEmail(),
                    subject,
                    content
            );
            sender.send(alarmDTO);
        });
    }
}

