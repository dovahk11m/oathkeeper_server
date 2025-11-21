package com.oath.domain.members.memberEvent;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private final AlarmFactory alarmFactory;

    @Value("${notification.policy.on-password-reset:${notification.policy.default}}")
    private String notificationType;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        log.info("[비밀번호 재설정 이벤트 수신: 사용자='{}']", event.getUsername());

        // 1. 알림 내용 생성
        String subject = "[Oath] 임시 비밀번호 발급 안내";
        String content = "안녕하세요, " + event.getUsername() + "님.\n\n"
                + "요청하신 임시 비밀번호는 다음과 같습니다.\n\n"
                + "임시 비밀번호: " + event.getTempPassword() + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해주시기 바랍니다.\n\n"
                + "감사합니다.\n"
                + "Oath 팀 드림";

        // 2. 알림 DTO 생성
        AlarmDTO request = new AlarmDTO(event.getEmail(), subject, content);

        // 3. 팩토리를 통해 적절한 Sender를 찾아 알림 발송
        AlarmSender sender = alarmFactory.findSender(notificationType);
        sender.send(request);
    }
}
