package com.oath.domain.alarms.localAlarms;

import com.oath.domain.members.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalAlarmRepository extends JpaRepository<LocalAlarm, Long> {

    List<LocalAlarm> findByMemberOrderByCreatedAtDesc(Member member);

    Long countByMemberAndIsReadFalse(Member member);
}