package com.oath.domain.plan.event.listener;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.plan.event.LateEvent;
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
public class LateListener {
    private final AlarmFactory alarmFactory;

    @Value("${notification.policy.on-late:${notification.policy.default}}")
    private String notificationType;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLateEvent(LateEvent event) {
        String lateUserName = event.getLateParticipant().getMember().getUsername();
        String planTitle = event.getPlan().getTitle();

        log.info(
                "[지각 알림 이벤트 수신: 약속='{}', 지각자='{}', 지각 시간='{}분', 알림 방식='{}']",
                planTitle,
                lateUserName,
                event.getLateMinutes(),
                notificationType
        );

        AlarmSender sender = alarmFactory.findSender(notificationType);

        event.getOtherParticipants().forEach(participant -> {
            String subject = String.format("[Oath] '%s' 약속 지각 알림", planTitle);
            String content = String.format(
                    "'%s'님이 약속 시간보다 %d분 늦게 도착했습니다.",
                    lateUserName,
                    event.getLateMinutes()
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

