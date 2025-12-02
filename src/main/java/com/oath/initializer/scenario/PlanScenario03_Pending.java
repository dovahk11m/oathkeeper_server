package com.oath.initializer.scenario;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanScenario03_Pending {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final LocationTrackRepository locationTrackRepository;
    private final PlaceRepository placeRepository;

    @Transactional("h2TransactionManager")
    public void create() {
        log.info("👷‍♂️ [Scenario 3] '대기중인 약속' 샘플 데이터 생성 시작");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
        Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
        Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
        Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
        Group sampleGroup = groupRepository.findByName("샘플 그룹")
                .orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
        Place seomyeon = placeRepository.findByName("서면역")
                .orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

        Plan pendingPlan = Plan.builder().creatorMember(user1).group(sampleGroup).title("주말 점심 식사")
                .planDatetime(LocalDateTime.now().plusHours(2)).status(Status.CONFIRMED)
                .placeName(seomyeon.getName()).placeLatitude(seomyeon.getLat())
                .placeLongitude(seomyeon.getLng()).lateFineAmount(500L).build();
        planJpaRepository.save(pendingPlan);

        Participant p1 = createParticipant(pendingPlan, user1, null, MovementStatus.MOVING, true);
        Participant p2 = createParticipant(pendingPlan, user2, null, MovementStatus.MOVING, true);
        Participant p3 = createParticipant(pendingPlan, user3, null, MovementStatus.MOVING, true);
        Participant p4 = createParticipant(pendingPlan, user4, null, MovementStatus.MOVING, true);

        createLocationTracks(p1, 35.20, 129.00, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p2, 35.10, 129.10, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p3, 35.18, 129.05, seomyeon.getLat(), seomyeon.getLng());
        createLocationTracks(p4, 35.12, 129.08, seomyeon.getLat(), seomyeon.getLng());

        log.info("👷‍♂️ [Scenario 3] '대기중인 약속' 샘플 데이터 생성 완료 (Plan ID: {})", pendingPlan.getId());
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

        List<LocationTrack> tracks = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double lat = startLat + (endLat - startLat) * progress;
            double lng = startLng + (endLng - startLng) * progress;
            LocationTrack track = LocationTrack.builder().participantId(participant.getId())
                    .lat(lat).lng(lng).ts(startTime.plusMinutes(i * 5)).build();
            tracks.add(track);
        }

        locationTrackRepository.saveAll(tracks); // 배치 저장!
    }
}
