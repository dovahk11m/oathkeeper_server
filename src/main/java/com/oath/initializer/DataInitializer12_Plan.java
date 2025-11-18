package com.oath.initializer;

import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.locationevents.domain.LocationTrack;
import com.oath.domain.locationevents.repository.LocationTrackRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.plan.ArrivalStatus;
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

    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ plan1 '완료된 약속' 샘플 데이터 생성 시작 (4인)");

        // 1. 필요한 엔티티 조회
        Member user1 = memberRepository.findByEmail("user1@test.com")
                .orElseThrow(); // 김철수 (부산대)
        Member user2 = memberRepository.findByEmail("user2@test.com")
                .orElseThrow(); // 이영희 (서면)
        Member user3 = memberRepository.findByEmail("user3@test.com")
                .orElseThrow(); // 박민철 (해운대)
        Member user4 = memberRepository.findByEmail("user4@test.com")
                .orElseThrow(); // 최상혁 (광안리)
        Group sampleGroup = groupRepository.findByName("샘플 그룹")
                .orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다. DataInitializer4_Group이 먼저 실행되었는지 확인하세요."));
        Place seomyeon = placeRepository.findByName("서면역")
                .orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다. DataInitializer8_Place가 먼저 실행되었는지 확인하세요."));

        // 2. '완료된' 약속 생성 (과거 시간)
        Plan completedPlan = Plan.builder()
                .creatorMember(user1)
                .group(sampleGroup)
                .title("주말 코딩 스터디")
                .planDatetime(LocalDateTime.now()
                                      .minusDays(3)) // 과거 약속
                .status(Status.COMPLETED) // 완료 상태
                .placeName(seomyeon.getName()) // Place 엔티티에서 이름 가져오기
                .placeLatitude(seomyeon.getLat()) // Place 엔티티에서 위도 가져오기
                .placeLongitude(seomyeon.getLng()) // Place 엔티티에서 경도 가져오기
                .lateFineAmount(1000L)
                .build();
        planJpaRepository.save(completedPlan);

        // 3. 참가자 생성
        Participant participant1 = createParticipant(
                completedPlan,
                user1
        );
        Participant participant2 = createParticipant(
                completedPlan,
                user2
        );
        Participant participant3 = createParticipant(
                completedPlan,
                user3
        );
        Participant participant4 = createParticipant(
                completedPlan,
                user4
        );

        // 4. 각 참가자에 대한 위치 기록(LocationTrack) 생성
        createLocationTracks(
                participant1,
                35.2335,
                129.0814,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 부산대 -> 서면
        createLocationTracks(
                participant2,
                35.1577,
                129.0591,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 서면 -> 서면 (이동 없음)
        createLocationTracks(
                participant3,
                35.1631,
                129.1636,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 해운대 -> 서면
        createLocationTracks(
                participant4,
                35.1531,
                129.1187,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 광안리 -> 서면

        log.info(
                "👷‍♂️ plan1 '완료된 약속' 샘플 데이터 생성 완료 (Plan ID: {})",
                completedPlan.getId()
        );

        log.info("👷‍♂️ plan2 '진행중인 약속' 테스트 시나리오 데이터 생성 시작 (4인 중 3인 도착)");

        // 5. '진행중인' 약속 생성 (30분 전 시작)
        Plan ongoingPlan = Plan.builder()
                .creatorMember(user1)
                .group(sampleGroup)
                .title("주중 코딩 스터디")
                .planDatetime(LocalDateTime.now()
                                      .minusMinutes(30))
                .status(Status.CONFIRMED)
                .placeName(seomyeon.getName())
                .placeLatitude(seomyeon.getLat())
                .placeLongitude(seomyeon.getLng())
                .lateFineAmount(1000L)
                .build();
        planJpaRepository.save(ongoingPlan);

        // 6. 참가자 생성 (3명은 사전 도착 처리)
        Participant ongoingParticipant1 = createParticipant(
                ongoingPlan,
                user1
        ); // user1: 아직 도착 안함
        Participant ongoingParticipant2 = createParticipant(
                ongoingPlan,
                user2,
                ongoingPlan.getPlanDatetime()
                        .minusMinutes(5)
        ); // user2: 5분 전 도착
        Participant ongoingParticipant3 = createParticipant(
                ongoingPlan,
                user3,
                ongoingPlan.getPlanDatetime()
        ); // user3: 정시 도착
        Participant ongoingParticipant4 = createParticipant(
                ongoingPlan,
                user4,
                ongoingPlan.getPlanDatetime()
                        .plusMinutes(2)
        ); // user4: 2분 지각

        // 7. 각 참가자에 대한 위치 기록(LocationTrack) 생성
        createLocationTracks(
                ongoingParticipant1,
                35.2335,
                129.0814,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 부산대 -> 서면
        createLocationTracks(
                ongoingParticipant2,
                35.1577,
                129.0591,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 서면 -> 서면 (이동 없음)
        createLocationTracks(
                ongoingParticipant3,
                35.1631,
                129.1636,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 해운대 -> 서면
        createLocationTracks(
                ongoingParticipant4,
                35.1531,
                129.1187,
                seomyeon.getLat(),
                seomyeon.getLng()
        ); // 광안리 -> 서면

        log.info(
                "👷‍♂️ plan2 '진행중인 약속' 데이터 생성 완료 (Plan ID: {}). user1(participantId:{}) 도착 시 COMPLETED 상태로 변경됩니다.",
                ongoingPlan.getId(),
                ongoingParticipant1.getId()
        );
    }

    private Participant createParticipant(
            Plan plan,
            Member member
    ) {
        return createParticipant(
                plan,
                member,
                null
        );
    }

    private Participant createParticipant(
            Plan plan,
            Member member,
            LocalDateTime arrivalDt
    ) {
        Participant.ParticipantBuilder builder = Participant.builder()
                .plan(plan)
                .member(member)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .actualArrivalTime(arrivalDt);

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

    private void createLocationTracks(
            Participant participant,
            double startLat,
            double startLng,
            double endLat,
            double endLng
    ) {
        LocalDateTime startTime = participant.getPlan()
                .getPlanDatetime()
                .minusHours(1);
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double lat = startLat + (endLat - startLat) * progress;
            double lng = startLng + (endLng - startLng) * progress;
            LocationTrack track = LocationTrack.builder()
                    .participantId(participant.getId())
                    .lat(lat)
                    .lng(lng)
                    .ts(startTime.plusMinutes(i * 5)) // 5분 간격으로 이동
                    .build();
            locationTrackRepository.save(track);
        }
    }
}
