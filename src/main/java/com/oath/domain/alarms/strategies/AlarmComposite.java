package com.oath.domain.alarms.strategies;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmComposite implements AlarmSender {

    private final AlarmWebSocket alarmWebSocket;
    private final AlarmLocal alarmLocal;

    @Override
    public void send(AlarmDTO request) {
        log.info("[복합 알림 전송 시작 to: {}]", request.getTo());

        // 1. WebSocket으로 실시간 푸시
        try {
            alarmWebSocket.send(request);
        } catch (Exception e) {
            log.error("[WebSocket 전송 실패, 계속 진행: {}]", e.getMessage());
        }

        // 2. DB에 저장 (나중에 읽을 수 있도록)
        try {
            alarmLocal.send(request);
        } catch (Exception e) {
            log.error("[LOCAL 저장 실패: {}]", e.getMessage());
        }

        log.info("[복합 알림 전송 완료 to: {}]", request.getTo());
    }

    @Override
    public boolean supports(String type) {
        return "COMPOSITE".equalsIgnoreCase(type) || "MULTI".equalsIgnoreCase(type);
    }
}

