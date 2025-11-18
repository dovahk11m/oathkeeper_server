package com.oath.domain.alarms.listeners;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.members.domain.Member;
import com.oath.domain.groups.groupEvent.CreateGroupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class CreateGroupListener {
    private final AlarmFactory alarmFactory;
    // on-create-group 설정이 없으면 default(LOCAL)
    @Value("${notification.policy.on-create-group:${notification.policy.default}}")
    private String notificationType;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // @Async // 초기화 시점의 과도한 DB Connection 방지를 위해 동기 처리로 변경
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // 서비스의 트랜잭션과 연동, 새로운 트랜잭션을 시작
    public void handleCreateGroupEvent(CreateGroupEvent event) {
        Member creator = event.getCreator();

        log.info(
                "[그룹 생성 이벤트 수신: 그룹명='{}', 생성자='{}', 알림 방식='{}']",
                event.getGroup()
                        .getName(),
                creator.getUsername(),
                notificationType
        );

        String subject = String.format("[Oath] '%s' 그룹 생성이 완료되었습니다.", event.getGroup().getName());
        String content = String.format(
                "'%s'님, 새로운 그룹 '%s'에 오신 것을 환영합니다!",
                creator.getUsername(),
                event.getGroup().getName()
        );
        AlarmDTO request = new AlarmDTO(creator.getEmail(), subject, content);

        // 설정된 알림 타입에 맞는 Sender를 찾아 알림
        AlarmSender sender = alarmFactory.findSender(notificationType);
        sender.send(request);
    }
}
