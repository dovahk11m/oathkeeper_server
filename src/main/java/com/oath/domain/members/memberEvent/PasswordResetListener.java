package com.oath.domain.members.memberEvent;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private final AlarmFactory alarmFactory;

    @EventListener
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        AlarmSender alarmSender = alarmFactory.getSender("EMAIL");

        String subject = "[Oath] 임시 비밀번호 발급 안내";
        String text = "안녕하세요, " + event.getUsername() + "님.\n\n"
                + "요청하신 임시 비밀번호는 다음과 같습니다.\n\n"
                + "임시 비밀번호: " + event.getTempPassword() + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해주시기 바랍니다.\n\n"
                + "감사합니다.\n"
                + "Oath 팀 드림";

        AlarmDTO alarmDTO = new AlarmDTO(event.getEmail(), subject, text);
        alarmSender.send(alarmDTO);
    }
}
