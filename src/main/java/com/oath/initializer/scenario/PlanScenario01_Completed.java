package com.oath.initializer.scenario;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.MovementStatus;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.SummaryStatus; // SummaryStatus import 추가
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanScenario01_Completed {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final LocationTrackRepository locationTrackRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void create() {
        log.info("👷‍♂️ [Scenario 1] '완료된 약속' 샘플 데이터 생성 시작");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Group sampleGroup = groupRepository.findByName("샘플 그룹")
                .orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
        Place seomyeon = placeRepository.findByName("서면역")
                .orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

        // 1. 통계 필드를 제외하고 Plan 객체 생성
        Plan completedPlan =
                Plan.builder().creatorMember(user1).group(sampleGroup).title("주말 코딩 스터디")
                        .planDatetime(LocalDateTime.now().minusDays(3)).status(Status.COMPLETED)
                        .placeName(seomyeon.getName()).placeLatitude(seomyeon.getLat())
                        .placeLongitude(seomyeon.getLng()).lateFineAmount(1000L).build();

        // 2. 통계 값 설정 (총 지각시간, 정시도착인원, 총 이동거리, 총 이동시간)
        completedPlan.updateStatistics(20, 2, 7.1, 152);

        // 3. 더미 AI 요약 데이터 및 상태 설정
        String realisticSummary =
                "약속 #1의 요약입니다. 총 4명의 기록을 분석했습니다. 그룹의 총 이동 거리는 약 25.5km, 총 이동 시간은 152분이었습니다. 멤버별로는 user2님이 8.5km(45분), user1님이 7.2km(40분)를 이동했습니다. 다음 약속은 조금 더 여유롭게 준비하면 모두가 편안하게 만날 수 있을 거예요.";
        completedPlan.setSummary(realisticSummary);
        completedPlan.setSummaryStatus(SummaryStatus.COMPLETED);

        // 4. Plan 최종 저장
        planJpaRepository.save(completedPlan);

        // 5. 참가자 생성
        Participant p1 = createParticipant(completedPlan, user1,
                completedPlan.getPlanDatetime().minusMinutes(10), MovementStatus.ARRIVED, false);
        Participant p2 = createParticipant(completedPlan, user2,
                completedPlan.getPlanDatetime().plusMinutes(5), MovementStatus.ARRIVED, false);
        Participant p3 = createParticipant(completedPlan, user3, completedPlan.getPlanDatetime(),
                MovementStatus.ARRIVED, false);
        Participant p4 = createParticipant(completedPlan, user4,
                completedPlan.getPlanDatetime().plusMinutes(15), MovementStatus.ARRIVED, false);

        // 서면역 중심 근거리 좌표 설정 (검증 가능한 거리)
        createLocationTracks(p1, 35.1676, 129.0596, seomyeon.getLat(), seomyeon.getLng()); // 북쪽
                                                                                           // 1km,
                                                                                           // 예상:
                                                                                           // ~1.1km
        createLocationTracks(p2, 35.1576, 129.0796, seomyeon.getLat(), seomyeon.getLng()); // 동쪽
                                                                                           // 2km,
                                                                                           // 예상:
                                                                                           // ~2.0km
        createLocationTracks(p3, 35.1351, 129.0596, seomyeon.getLat(), seomyeon.getLng()); // 남쪽
                                                                                           // 2.5km,
                                                                                           // 예상:
                                                                                           // ~2.5km
        createLocationTracks(p4, 35.1576, 129.0446, seomyeon.getLat(), seomyeon.getLng()); // 서쪽
                                                                                           // 1.5km,
                                                                                           // 예상:
                                                                                           // ~1.5km
        // 총 예상 거리: 약 7.1km

        log.info("👷‍♂️ [Scenario 1] '완료된 약속' 샘플 데이터 생성 완료 (Plan ID: {})", completedPlan.getId());
    }

    private Participant createParticipant(Plan plan, Member member, LocalDateTime arrivalDt,
            MovementStatus movementStatus, Boolean isShareLocation) {
        Participant.ParticipantBuilder builder = Participant.builder().plan(plan).member(member)
                .participantStatus(ParticipantStatus.ACCEPTED).actualArrivalTime(arrivalDt)
                .movementStatus(movementStatus).isShareLocation(isShareLocation);

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

    private void createLocationTracks(Participant participant, double startLat, double startLng,
            double endLat, double endLng) {
        LocalDateTime startTime = participant.getPlan().getPlanDatetime().minusHours(1);
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double lat = startLat + (endLat - startLat) * progress;
            double lng = startLng + (endLng - startLng) * progress;
            LocationTrack track = LocationTrack.builder().participantId(participant.getId())
                    .lat(lat).lng(lng).ts(startTime.plusMinutes(i * 5)).build();
            locationTrackRepository.save(track);
        }
    }
}
