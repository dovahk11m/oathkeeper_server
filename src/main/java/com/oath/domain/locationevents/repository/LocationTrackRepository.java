package com.oath.domain.locationevents.repository;

import com.oath.domain.locationevents.domain.LocationTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationTrackRepository extends JpaRepository<LocationTrack, Long> {
    List<LocationTrack> findAllByParticipantIdOrderByTsAsc(Long participantId);
}
