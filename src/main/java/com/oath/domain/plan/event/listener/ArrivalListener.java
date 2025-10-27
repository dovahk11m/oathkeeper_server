package com.oath.domain.plan.event.listener;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.plan.event.ArrivalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class ArrivalListener {
    private final AlarmFactory alarmFactory;

    @Value("${notification.policy.on-arrival:${notification.policy.default}}")
    private String notificationType;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleArrivalEvent(ArrivalEvent event) {
        String arrivedUserName = event.getArrivedParticipant().getMember().getUsername();
        String planTitle = event.getPlan().getTitle();

        log.info(
                "[도착 알림 이벤트 수신: 약속='{}', 도착자='{}', 알림 방식='{}']",
                planTitle,
                arrivedUserName,
                notificationType
        );

        AlarmSender sender = alarmFactory.findSender(notificationType);

        event.getOtherParticipants().forEach(participant -> {
            String subject = String.format("[Oath] '%s' 약속 도착 알림", planTitle);
            String content = String.format(
                    "'%s'님이 약속 장소에 도착했습니다.",
                    arrivedUserName
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

