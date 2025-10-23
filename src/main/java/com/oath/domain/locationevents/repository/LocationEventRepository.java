package com.oath.domain.locationevents.repository;

import com.oath.domain.locationevents.domain.LocationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationEventRepository extends JpaRepository<LocationEvent, Long> {
    List<LocationEvent> findAllByParticipantIdOrderByTsAsc(Long participantId);
}
