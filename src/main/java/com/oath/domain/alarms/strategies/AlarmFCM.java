package com.oath.domain.alarms.strategies;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmFCM implements AlarmSender {

    // TODO: Firebase Admin SDK 추가 후 FirebaseMessaging 주입
    // private final FirebaseMessaging firebaseMessaging;
    // private final MemberFCMTokenRepository fcmTokenRepository;

    @Override
    public void send(AlarmDTO request) {
        log.warn("[FCM 알림 전송 - 아직 구현되지 않음 to: {}]", request.getTo());

        // TODO: FCM 토큰 조회 및 전송
        /*
        String fcmToken = fcmTokenRepository.findByMemberEmail(request.getTo())
                .orElseThrow(() -> new Exception404("FCM 토큰이 없습니다."));

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(request.getSubject())
                        .setBody(request.getContent())
                        .build())
                .build();

        firebaseMessaging.send(message);
        log.info("[FCM 알림 전송 성공 to: {}]", request.getTo());
        */
    }

    @Override
    public boolean supports(String type) {
        return "FCM".equalsIgnoreCase(type) || "PUSH".equalsIgnoreCase(type);
    }
}

