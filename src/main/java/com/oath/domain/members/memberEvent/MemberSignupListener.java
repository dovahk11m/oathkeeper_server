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
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberSignupListener {

    private final AlarmFactory alarmFactory;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${notification.policy.on-signup:EMAIL}") // 기본값을 EMAIL로 설정
    private String notificationType;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberSignup(MemberSignupEvent event) {
        log.info(
                "[회원가입 이벤트 수신: 사용자='{}', 이메일='{}', 알림 방식='{}']",
                event.getUsername(),
                event.getEmail(),
                notificationType
        );

        String subject = "[Oath] 회원가입 인증을 완료해주세요.";
        
        String verificationLink = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/member/verify")
                .queryParam("token", event.getVerificationToken())
                .build().toUriString();

        String content = String.format(
                "안녕하세요, %s님!\n\n가입을 완료하려면 아래 링크를 클릭하세요:\n%s\n\n만약 이 요청을 직접 하지 않으셨다면 이 이메일을 무시해주세요.",
                event.getUsername(),
                verificationLink
        );

        AlarmDTO request = new AlarmDTO(event.getEmail(), subject, content);

        AlarmSender sender = alarmFactory.findSender(notificationType);
        sender.send(request);
    }
}
