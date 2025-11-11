package com.oath.domain.members.memberEvent;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.members.domain.Member;
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
public class SocialSignupListener {

    private final AlarmFactory alarmFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSocialMemberSignup(SocialSignupEvent event) {
        Member member = event.getMember();
        log.info("[소셜 회원가입 이벤트 수신: 사용자='{}', 소셜 타입='{}']", member.getUsername(), member.getSocialType());

        String subject = "Oath 가입을 환영합니다!";
        String content = String.format("'%s'님, Oath의 회원이 되신 것을 진심으로 환영합니다. 지금 바로 약속을 만들어보세요!", member.getUsername());
        AlarmDTO request = new AlarmDTO(member.getEmail(), subject, content);

        // 인앱 알림(LOCAL) 전송
        AlarmSender sender = alarmFactory.findSender("LOCAL");
        sender.send(request);
    }
}
