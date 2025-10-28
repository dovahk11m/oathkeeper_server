package com.oath.domain.alarms.strategies.alarmlocals;

import com.oath.domain.members.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmLocalRepository extends JpaRepository<AlarmLocalEntity, Long> {

    List<AlarmLocalEntity> findByMemberOrderByCreatedAtDesc(Member member);

    Long countByMemberAndIsReadFalse(Member member);
}