package com.oath.initializer;

import com.oath.domain.groups.Group;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.MovementStatus; // MovementStatus import 추가
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer12_Plan {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final LocationTrackRepository locationTrackRepository;
    private final PlaceRepository placeRepository;
    private final MetricsRollupService rollupService; // 통계 계산 서비스 주입
    private final MetricsPushService pushService;     // AI 서버 전송 서비스 주입

    @Transactional
    public void initialize(String... args) {
        createCompletedPlanScenario();
        createOngoingPlanScenario();
        createPendingPlanScenario(); // 새로운 시나리오 추가
    }

    private void createCompletedPlanScenario() {
        log.info("👷‍♂️ plan1 '완료된 약속' 샘플 데이터 생성 시작 (4인)");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Group sampleGroup = groupRepository.findByName("샘플 그룹").orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
        Place seomyeon = placeRepository.findByName("서면역").orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

        Plan completedPlan = Plan.builder()
                .creatorMember(user1)
                .group(sampleGroup)
                .title("주말 코딩 스터디")
                .planDatetime(LocalDateTime.now().minusDays(3))
                .status(Status.COMPLETED) // 처음부터 완료 상태로 생성
                .placeName(seomyeon.getName())
                .placeLatitude(seomyeon.getLat())
                .placeLongitude(seomyeon.getLng())
                .lateFineAmount(1000L)
                .build();
        planJpaRepository.save(completedPlan);

        // 완료된 약속이므로 모두 도착 상태로 설정
        Participant p1 = createParticipant(completedPlan, user1, completedPlan.getPlanDatetime().minusMinutes(10), MovementStatus.ARRIVED, false);
        Participant p2 = createParticipant(completedPlan, user2, completedPlan.getPlanDatetime().plusMinutes(5), MovementStatus.ARRIVED, false);
        Participant p3 = createParticipant(completedPlan, user3, completedPlan.getPlanDatetime(), MovementStatus.ARRIVED, false);
        Participant p4 = createParticipant(completedPlan, user4, completedPlan.getPlanDatetime().plusMinutes(15), MovementStatus.ARRIVED, false);

        createLocationTracks(p1, 35.2335, 129.0814, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p2, 35.1577, 129.0591, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p3, 35.1631, 129.1636, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p4, 35.1531, 129.1187, seomyeon.getLat(), seomyeon.getLng());

        log.info("👷‍♂️ plan1 '완료된 약속' 샘플 데이터 생성 완료 (Plan ID: {})", completedPlan.getId());

        // [수정] 상태 변경 로직을 타지 않으므로, 수동으로 통계 계산 및 AI 서버 전송을 트리거
        log.info("👷‍♂️ plan1에 대한 통계 계산 및 AI 서버 전송을 수동으로 실행합니다.");
        rollupService.rebuildForPlan(completedPlan.getId());
        pushService.pushPlan(completedPlan.getId());
        log.info("👷‍♂️ plan1 수동 처리 완료.");
    }

    private void createOngoingPlanScenario() {
        log.info("👷‍♂️ plan2 '진행중인 약속' 테스트 시나리오 데이터 생성 시작 (4인 중 3인 도착)");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Group sampleGroup = groupRepository.findByName("샘플 그룹").orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
        Place seomyeon = placeRepository.findByName("서면역").orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

        Plan ongoingPlan = Plan.builder()
                .creatorMember(user1)
                .group(sampleGroup)
                .title("긴급 트러블슈팅 회의")
                .planDatetime(LocalDateTime.now().plusHours(1))
                .status(Status.CONFIRMED)
                .placeName(seomyeon.getName())
                .placeLatitude(seomyeon.getLat())
                .placeLongitude(seomyeon.getLng())
                .lateFineAmount(1000L)
                .build();
        planJpaRepository.save(ongoingPlan);

        // p1은 아직 도착 안함 (이동 중), 나머지는 도착 상태
        Participant p1 = createParticipant(ongoingPlan, user1, null, MovementStatus.MOVING, true); // 아직 도착 안함, 이동 중, 위치 공유
        Participant p2 = createParticipant(ongoingPlan, user2, ongoingPlan.getPlanDatetime().minusMinutes(5), MovementStatus.ARRIVED, false);
        Participant p3 = createParticipant(ongoingPlan, user3, ongoingPlan.getPlanDatetime(), MovementStatus.ARRIVED, false);
        Participant p4 = createParticipant(ongoingPlan, user4, ongoingPlan.getPlanDatetime().plusMinutes(2), MovementStatus.ARRIVED, false);

        createLocationTracks(p1, 35.2335, 129.0814, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p2, 35.1577, 129.0591, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p3, 35.1631, 129.1636, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p4, 35.1531, 129.1187, seomyeon.getLat(), seomyeon.getLng());

        log.info("👷‍♂️ plan2 '진행중인 약속' 데이터 생성 완료 (Plan ID: {}). user1(participantId:{}) 도착 시 COMPLETED 상태로 변경됩니다.", ongoingPlan.getId(), p1.getId());
    }

    private void createPendingPlanScenario() {
        log.info("👷‍♂️ plan3 '대기중인 약속' 샘플 데이터 생성 시작 (4인, 모두 이동중)");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Group sampleGroup = groupRepository.findByName("샘플 그룹").orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
        Place seomyeon = placeRepository.findByName("서면역").orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

        Plan pendingPlan = Plan.builder()
                .creatorMember(user1)
                .group(sampleGroup)
                .title("주말 점심 식사")
                .planDatetime(LocalDateTime.now().plusHours(2)) // 2시간 후 약속
                .status(Status.CONFIRMED)
                .placeName(seomyeon.getName())
                .placeLatitude(seomyeon.getLat())
                .placeLongitude(seomyeon.getLng())
                .lateFineAmount(500L)
                .build();
        planJpaRepository.save(pendingPlan);

        // 모든 참가자가 이동 중인 상태로 설정
        Participant p1 = createParticipant(pendingPlan, user1, null, MovementStatus.MOVING, true); // 아직 도착 안함, 이동 중, 위치 공유
        Participant p2 = createParticipant(pendingPlan, user2, null, MovementStatus.MOVING, true); // 아직 도착 안함, 이동 중, 위치 공유
        Participant p3 = createParticipant(pendingPlan, user3, null, MovementStatus.MOVING, true); // 아직 도착 안함, 이동 중, 위치 공유
        Participant p4 = createParticipant(pendingPlan, user4, null, MovementStatus.MOVING, true); // 아직 도착 안함, 이동 중, 위치 공유

        // 각 참여자의 시작 위치를 다르게 설정하여 이동 경로 시뮬레이션
        createLocationTracks(p1, 35.20, 129.00, seomyeon.getLat(), seomyeon.getLng()); // 북서쪽에서 출발
        createLocationTracks(p2, 35.10, 129.10, seomyeon.getLat(), seomyeon.getLng()); // 남동쪽에서 출발
        createLocationTracks(p3, 35.18, 129.05, seomyeon.getLat(), seomyeon.getLng()); // 서쪽에서 출발
        createLocationTracks(p4, 35.12, 129.08, seomyeon.getLat(), seomyeon.getLng()); // 남쪽에서 출발

        log.info("👷‍♂️ plan3 '대기중인 약속' 샘플 데이터 생성 완료 (Plan ID: {})", pendingPlan.getId());
    }

    private Participant createParticipant(Plan plan, Member member, LocalDateTime arrivalDt, MovementStatus movementStatus, Boolean isShareLocation) {
        Participant.ParticipantBuilder builder = Participant.builder()
                .plan(plan)
                .member(member)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .actualArrivalTime(arrivalDt)
                .movementStatus(movementStatus) // MovementStatus 설정
                .isShareLocation(isShareLocation); // isShareLocation 설정

        if (arrivalDt != null) {
            long minutesDiff = ChronoUnit.MINUTES.between(plan.getPlanDatetime(), arrivalDt);
            if (minutesDiff > 0) {
                builder.arrivalStatus(ArrivalStatus.LATE);
            } else {
                builder.arrivalStatus(ArrivalStatus.ON_TIME);
            }
            builder.arrivalOffsetMinutes((int) minutesDiff);
        }

        return participantRepository.save(builder.build());
    }

    private void createLocationTracks(Participant participant, double startLat, double startLng, double endLat, double endLng) {
        LocalDateTime startTime = participant.getPlan().getPlanDatetime().minusHours(1);
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double lat = startLat + (endLat - startLat) * progress;
            double lng = startLng + (endLng - startLng) * progress;
            LocationTrack track = LocationTrack.builder()
                    .participantId(participant.getId())
                    .lat(lat)
                    .lng(lng)
                    .ts(startTime.plusMinutes(i * 5))
                    .build();
            locationTrackRepository.save(track);
        }
    }
}
