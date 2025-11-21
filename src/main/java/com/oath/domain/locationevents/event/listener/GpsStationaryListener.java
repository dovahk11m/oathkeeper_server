package com.oath.domain.locationevents.event.listener;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.locationevents.event.GpsStationaryEvent;
import com.oath.domain.members.repository.MemberRepository;
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
public class GpsStationaryListener {

    private final AlarmFactory alarmFactory;
    private final MemberRepository memberRepository; // 이메일 주소를 가져오기 위해 MemberRepository 주입

    @Value("${notification.policy.on-gps-stationary:${notification.policy.default}}")
    private String notificationType;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGpsStationaryEvent(GpsStationaryEvent event) {
        log.info("[GPS 정체 이벤트 수신: 플랜ID='{}', 참가자ID='{}', 사용자='{}']",
                event.getPlanId(), event.getParticipantId(), event.getUsername());

        // 사용자 이메일 주소 조회
        String memberEmail = memberRepository.findById(event.getMemberId())
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다: " + event.getMemberId()))
                .getEmail();

        // 1. 알림 내용 생성
        String subject = String.format("[Oath] '%s'님, 아직 출발 준비 중이신가요?", event.getUsername());
        String content = String.format(
                "약속 장소로 이동 중인 것으로 보였으나, %d분 동안 위치 변화가 감지되지 않았습니다.\n" +
                        "현재 위치: 위도 %.4f, 경도 %.4f\n" +
                        "약속에 늦지 않도록 확인해주세요!",
                event.getStationaryDurationMinutes(),
                event.getLastKnownLocation().getY(), // 위도
                event.getLastKnownLocation().getX()  // 경도
        );

        // 2. 알림 DTO 생성
        AlarmDTO request = new AlarmDTO(memberEmail, subject, content);

        // 3. 팩토리를 통해 적절한 Sender를 찾아 알림 발송
        AlarmSender sender = alarmFactory.findSender(notificationType);
        sender.send(request);
    }
}
