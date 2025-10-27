package com.oath.domain.plan.event.listener;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.plan.event.NotDepartedEvent;
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
public class NotDepartedListener {
    private final AlarmFactory alarmFactory;

    @Value("${notification.policy.on-not-departed:${notification.policy.default}}")
    private String notificationType;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotDepartedEvent(NotDepartedEvent event) {
        String notDepartedUserName = event.getNotDepartedParticipant().getMember().getUsername();
        String planTitle = event.getPlan().getTitle();

        log.info(
                "[미출발 독촉 알림 이벤트 수신: 약속='{}', 미출발자='{}', 남은 시간='{}분', 알림 방식='{}']",
                planTitle,
                notDepartedUserName,
                event.getRemainingMinutes(),
                notificationType
        );

        AlarmSender sender = alarmFactory.findSender(notificationType);

        String subject = String.format("[Oath] '%s' 약속 출발 독촉", planTitle);
        String content = String.format(
                "약속 시간까지 %d분 남았습니다. 지금 출발하지 않으면 지각할 수 있습니다!",
                event.getRemainingMinutes()
        );
        AlarmDTO alarmDTO = new AlarmDTO(
                event.getNotDepartedParticipant().getMember().getEmail(),
                subject,
                content
        );
        sender.send(alarmDTO);
    }
}


