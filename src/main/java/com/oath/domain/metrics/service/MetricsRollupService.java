// com/oath/domain/metrics/service/MetricsRollupService.java
package com.oath.domain.metrics.service;

import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.util.GeoUtils;
import com.oath.domain.plan.domain.Participant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsRollupService {

    private final LocationTrackRepository trackRepo;
    private final ParticipantMetricsRepository metricsRepo;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void rebuildForPlan(Long planId){
        List<Participant> participants = em.createQuery(
                "select p from Participant p join fetch p.member where p.plan.id = :planId",
                Participant.class
        ).setParameter("planId", planId).getResultList();

        log.info("[rollup] planId={} participants={}", planId, participants.size());

        for (Participant p : participants){
            computeAndSave(planId, p.getMember().getId(), p.getId());
        }
    }

    @Transactional
    public void rebuildForParticipantByParticipantId(Long planId, Long participantId) {
        Participant p = em.find(Participant.class, participantId);
        if (p == null) {
            log.warn("[rollup] skip: participantId={} not found", participantId);
            return;
        }
        if (!p.getPlan().getId().equals(planId)) {
            log.warn("[rollup] skip: participantId={} not in planId={}", participantId, planId);
            return;
        }
        computeAndSave(planId, p.getMember().getId(), participantId);
    }

    @Transactional
    public void rebuildForParticipant(Long planId, Long memberId, Long participantId){
        computeAndSave(planId, memberId, participantId);
    }

    private void computeAndSave(Long planId, Long memberId, Long participantId){
        List<LocationTrack> tracks = trackRepo.findAllByParticipantIdOrderByTsAsc(participantId);
        log.info("[rollup] planId={} participantId={} tracks={}", planId, participantId, tracks.size());

        if (tracks.isEmpty()) return;

        // 거리(km)
        double distKm = 0.0;
        for (int i = 1; i < tracks.size(); i++){
            var a = tracks.get(i - 1);
            var b = tracks.get(i);
            if (a.getLat()==null || a.getLng()==null || b.getLat()==null || b.getLng()==null) continue;
            distKm += GeoUtils.haversineKm(a.getLat(), a.getLng(), b.getLat(), b.getLng());
        }
        distKm = GeoUtils.round2(distKm);

        // 이동시간(분)
        long minutes = 0;
        if (tracks.get(0).getTs()!=null && tracks.get(tracks.size()-1).getTs()!=null) {
            minutes = Duration.between(tracks.get(0).getTs(), tracks.get(tracks.size()-1).getTs()).toMinutes();
            if (minutes < 0) minutes = 0;
        }

        var m = metricsRepo.findByPlanIdAndMemberId(planId, memberId)
                .orElse(ParticipantMetrics.builder()
                        .planId(planId)
                        .memberId(memberId)
                        .build());

        m.setDistanceKm(distKm);
        m.setTravelMinutes((int) minutes);
        metricsRepo.save(m);

        log.info("[rollup] saved planId={} memberId={} distKm={} minutes={}",
                planId, memberId, distKm, minutes);
    }
}
