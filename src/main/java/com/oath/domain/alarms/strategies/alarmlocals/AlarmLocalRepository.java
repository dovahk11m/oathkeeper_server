package com.oath.domain.alarms.strategies.alarmlocals;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmLocalRepository extends JpaRepository<AlarmLocalEntity, Long> {
}