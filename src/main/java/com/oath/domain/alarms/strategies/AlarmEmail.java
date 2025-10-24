package com.oath.domain.alarms.strategies;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmEmail implements AlarmSender {

    private final JavaMailSender javaMailSender;
    // 발신자 이메일 주소
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void send(AlarmDTO request) {

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmail);
            simpleMailMessage.setTo(request.getTo());
            simpleMailMessage.setSubject(request.getSubject());
            simpleMailMessage.setText(request.getContent());
            javaMailSender.send(simpleMailMessage);
            log.info(
                    "[이메일 발송 성공 to: {}]",
                    request.getTo()
            );
        } catch (Exception e) {
            log.error(
                    "[이메일 발송 실패 to: {}, error: {}]",
                    request.getTo(),
                    e.getMessage()
            );
            throw new RuntimeException(
                    "이메일 발송에 실패했습니다.",
                    e
            );
        }
    }

    @Override
    public boolean supports(String type) {
        return "EMAIL".equalsIgnoreCase(type);
    }
}
