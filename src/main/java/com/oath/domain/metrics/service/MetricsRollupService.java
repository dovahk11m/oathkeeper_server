// com/oath/domain/metrics/service/MetricsRollupService.java
package com.oath.domain.metrics.service;

import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.util.GeoUtils;
import com.oath.domain.plan.Participant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsRollupService {

    private final LocationTrackRepository trackRepo;         // location_tracks_tb
    private final ParticipantMetricsRepository metricsRepo;   // plan_member_metrics_tb

    @PersistenceContext
    private EntityManager em;

    /** 약속(planId) 단위로 모든 참가자 메트릭 계산 */
    @Transactional
    public void rebuildForPlan(Long planId){
        List<Participant> participants = em.createQuery(
                "select pm from Participant pm join fetch pm.member where pm.plan.id = :planId",
                Participant.class
        ).setParameter("planId", planId).getResultList();

        for (Participant p : participants){
            computeAndSave(planId, p.getMember().getId(), p.getId());
        }
    }

    /** 특정 참가자만 재계산 */
    @Transactional
    public void rebuildForParticipant(Long planId, Long memberId, Long participantId){
        computeAndSave(planId, memberId, participantId);
    }

    private void computeAndSave(Long planId, Long memberId, Long participantId){
        List<LocationTrack> tracks = trackRepo.findAllByParticipantIdOrderByTsAsc(participantId);
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

        // upsert
        var m = metricsRepo.findByPlanIdAndMemberId(planId, memberId)
                .orElse(ParticipantMetrics.builder()
                        .planId(planId)
                        .memberId(memberId)
                        .build());

        m.setDistanceKm(distKm);
        m.setTravelMinutes((int) minutes);
        metricsRepo.save(m);
    }
}
