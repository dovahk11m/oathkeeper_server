package com.oath.domain.alarms.strategies;

import com.oath.common.exception.Exception404;
import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.alarms.localAlarms.LocalAlarm;
import com.oath.domain.alarms.localAlarms.LocalAlarmRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmLocal implements AlarmSender {

    // [수정] 인앱 알림 저장을 위한 Repository 주입
    private final LocalAlarmRepository localAlarmRepository;
    private final MemberRepository memberRepository;

    @Override
    public void send(AlarmDTO request) {
        log.info(
                "[인앱 알림 저장 to: {}, subject: {}]",
                request.getTo(),
                request.getSubject()
        );

        // [수정] 실제 알림 엔티티를 생성하고 DB에 저장하는 로직 구현
        Member member = memberRepository.findByEmail(request.getTo())
                .orElseThrow(() -> new Exception404("알림을 받을 사용자를 찾을 수 없습니다: " + request.getTo()));

        LocalAlarm newAlarm = LocalAlarm.builder()
                .member(member)
                .subject(request.getSubject())
                .content(request.getContent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        localAlarmRepository.save(newAlarm);
    }

    @Override
    public boolean supports(String type) {
        return "LOCAL".equalsIgnoreCase(type);
    }
}