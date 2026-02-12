package com.oath.domain.members.memberEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;
import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        public void handleMemberSignup(MemberSignupEvent event) {
                log.info("[회원가입 이벤트 수신: 사용자='{}', 이메일='{}', 알림 방식='{}']", event.getUsername(),
                                event.getEmail(), notificationType);

                try {
                        String subject = "[Oath] 회원가입 인증을 완료해주세요.";

                        String verificationLink = UriComponentsBuilder.fromUriString(baseUrl)
                                        .path("/api/member/verify")
                                        .queryParam("token", event.getVerificationToken()).build()
                                        .toUriString();

                        log.info("[인증 링크 생성: {}]", verificationLink);

                        String content = String.format(
                                        "안녕하세요, %s님!\n\n가입을 완료하려면 아래 링크를 클릭하세요:\n%s\n\n만약 이 요청을 직접 하지 않으셨다면 이 이메일을 무시해주세요.",
                                        event.getUsername(), verificationLink);

                        AlarmDTO request = new AlarmDTO(event.getEmail(), subject, content);

                        log.info("[이메일 발송 시도: to='{}', subject='{}']", event.getEmail(), subject);

                        AlarmSender sender = alarmFactory.findSender(notificationType);
                        sender.send(request);

                        log.info("[이메일 발송 완료: to='{}']", event.getEmail());

                } catch (Exception e) {
                        log.error("[이메일 발송 실패: 사용자='{}', 이메일='{}', 에러='{}']", event.getUsername(),
                                        event.getEmail(), e.getMessage(), e);
                        // 이메일 발송 실패해도 회원가입은 완료되어야 하므로 예외를 던지지 않음
                }
        }
}
