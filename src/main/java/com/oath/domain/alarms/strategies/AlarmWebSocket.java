package com.oath.domain.alarms.strategies;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmWebSocket implements AlarmSender {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(AlarmDTO request) {
        try {
            // /topic/alarms/{userEmail} 구독자에게 메시지 전송
            String destination = "/topic/alarms/" + request.getTo();

            messagingTemplate.convertAndSend(destination, request);

            log.info(
                    "[WebSocket 알림 전송 성공 to: {}, subject: {}]",
                    request.getTo(),
                    request.getSubject()
            );
        } catch (Exception e) {
            log.error(
                    "[WebSocket 알림 전송 실패 to: {}, error: {}]",
                    request.getTo(),
                    e.getMessage()
            );
        }
    }

    @Override
    public boolean supports(String type) {
        return "WEBSOCKET".equalsIgnoreCase(type) || "WS".equalsIgnoreCase(type);
    }
}

