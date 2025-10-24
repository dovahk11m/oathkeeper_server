package com.oath.domain.alarms;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AlarmFactory {
    private final List<AlarmSender> senderList;

    //들어온 type에 맞는 Sender를 찾아 반환
    public AlarmSender findSender(String type) {
        for (AlarmSender sender : senderList) {
            if (sender.supports(type)) {
                return sender;
            }
        }
        throw new IllegalArgumentException("미지원 알림 방식" + type);
    }
}
