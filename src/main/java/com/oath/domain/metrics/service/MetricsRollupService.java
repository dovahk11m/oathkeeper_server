// com/oath/domain/metrics/service/MetricsRollupService.java
package com.oath.domain.metrics.service;

import com.oath.common.exception.Exception404;
import com.oath.domain.groups.Group;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.util.GeoUtils;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional("h2TransactionManager")
public class MetricsRollupService {

    private final LocationTrackRepository trackRepo;
    private final ParticipantMetricsRepository metricsRepo;
    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository; // Participant Repository 추가

    @PersistenceContext(unitName = "h2EntityManagerFactory")
    private EntityManager em;

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

        // Participant 엔티티에서 지각 시간(timeBurdenMinutes) 가져오기
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("Participant not found with id: " + participantId));
        
        Integer lateMinutes = participant.getTimeBurdenMinutes();
        if (lateMinutes != null && lateMinutes < 0) {
            lateMinutes = 0; // 지각 시간은 0 이상이어야 함
        }

        m.setDistanceKm(distKm);
        m.setTravelMinutes((int) minutes);
        m.setLateMinutes(lateMinutes); // 지각 시간 설정

        metricsRepo.save(m);

        log.info("[rollup] saved planId={} memberId={} distKm={} minutes={} lateMinutes={}",
                planId, memberId, distKm, minutes, lateMinutes);
    }

    /**
     * 멤버별 통계를 합산하여 Plan 엔티티의 통계 필드를 업데이트합니다.
     * @param planId 통계를 집계할 Plan의 ID
     */
    public void aggregateMetricsForPlan(Long planId) {
        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("Plan not found with id: " + planId));

        List<ParticipantMetrics> metricsList = metricsRepo.findAllByPlanId(planId);

        int totalLateMinutes = metricsList.stream()
                .map(ParticipantMetrics::getLateMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        double totalTravelDistance = metricsList.stream()
                .map(ParticipantMetrics::getDistanceKm)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        int totalTravelTime = metricsList.stream()
                .map(ParticipantMetrics::getTravelMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // '정시 도착'은 Participant의 arrivalStatus를 기반으로 계산해야 합니다.
        long onTimeArrivals = plan.getParticipants().stream()
                .filter(p -> p.getArrivalStatus() == com.oath.domain.plan.ArrivalStatus.ON_TIME)
                .count();

        plan.updateStatistics(
                totalLateMinutes,
                (int) onTimeArrivals,
                GeoUtils.round2(totalTravelDistance),
                totalTravelTime
        );

        planJpaRepository.save(plan);

        log.info("[aggregate] Plan 통계 집계 완료: planId={}, totalLateMinutes={}, totalTravelDistance={}, totalTravelTime={}, onTimeArrivals={}",
                planId, totalLateMinutes, totalTravelDistance, totalTravelTime, onTimeArrivals);
    }

    /**
     * 완료된 Plan의 통계를 해당 Group에 누적합니다.
     * @param planId 완료된 Plan의 ID
     */
    public void accumulatePlanStatsToGroup(Long planId) {
        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("Plan not found with id: " + planId));

        Group group = plan.getGroup();
        if (group == null) {
            log.info("[accumulate] Plan(id={}) is not associated with any group. Skipping accumulation.", planId);
            return;
        }

        group.addPlanStatistics(plan);
        // Dirty-checking에 의해 트랜잭션 커밋 시점에 Group 엔티티가 자동으로 업데이트됩니다.

        log.info("[accumulate] Plan(id={}) stats have been accumulated to Group(id={}).", planId, group.getId());
    }
}
