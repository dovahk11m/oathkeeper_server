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
public class PlanScenario02_Ongoing {

        private final MemberRepository memberRepository;
        private final GroupRepository groupRepository;
        private final PlanJpaRepository planJpaRepository;
        private final ParticipantRepository participantRepository;
        private final LocationTrackRepository locationTrackRepository;
        private final PlaceRepository placeRepository;

        @Transactional("h2TransactionManager")
        public void create() {
                log.info("👷‍♂️ [Scenario 2] '진행중인 약속' 샘플 데이터 생성 시작");

                Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();
                Member user2 = memberRepository.findByEmail("user2@test.com").orElseThrow();
                Member user3 = memberRepository.findByEmail("user3@test.com").orElseThrow();
                Member user4 = memberRepository.findByEmail("user4@test.com").orElseThrow();
                Group sampleGroup = groupRepository.findByName("샘플 그룹")
                                .orElseThrow(() -> new RuntimeException("샘플 그룹을 찾을 수 없습니다."));
                Place seomyeon = placeRepository.findByName("서면역")
                                .orElseThrow(() -> new RuntimeException("서면역 장소를 찾을 수 없습니다."));

                Plan ongoingPlan = Plan.builder().creatorMember(user1).group(sampleGroup)
                                .title("긴급 트러블슈팅 회의").planDatetime(LocalDateTime.now().plusHours(1))
                                .status(Status.CONFIRMED).placeName(seomyeon.getName())
                                .placeLatitude(seomyeon.getLat()).placeLongitude(seomyeon.getLng())
                                .lateFineAmount(1000L).build();
                planJpaRepository.save(ongoingPlan);

                Participant p1 = createParticipant(ongoingPlan, user1, null, MovementStatus.MOVING,
                                true);
                Participant p2 = createParticipant(ongoingPlan, user2,
                                ongoingPlan.getPlanDatetime().minusMinutes(5),
                                MovementStatus.ARRIVED, false);
                Participant p3 = createParticipant(ongoingPlan, user3,
                                ongoingPlan.getPlanDatetime(), MovementStatus.ARRIVED, false);
                Participant p4 = createParticipant(ongoingPlan, user4,
                                ongoingPlan.getPlanDatetime().plusMinutes(2),
                                MovementStatus.ARRIVED, false);

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

                log.info("👷‍♂️ [Scenario 2] '진행중인 약속' 데이터 생성 완료 (Plan ID: {}). user1(participantId:{}) 도착 시 COMPLETED 상태로 변경됩니다.",
                                ongoingPlan.getId(), p1.getId());
        }

        private Participant createParticipant(Plan plan, Member member, LocalDateTime arrivalDt,
                        MovementStatus movementStatus, Boolean isShareLocation) {
                Participant.ParticipantBuilder builder = Participant.builder().plan(plan)
                                .member(member).participantStatus(ParticipantStatus.ACCEPTED)
                                .actualArrivalTime(arrivalDt).movementStatus(movementStatus)
                                .isShareLocation(isShareLocation);

                if (arrivalDt != null) {
                        long minutesDiff = ChronoUnit.MINUTES.between(plan.getPlanDatetime(),
                                        arrivalDt);
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
                        LocationTrack track = LocationTrack.builder()
                                        .participantId(participant.getId()).lat(lat).lng(lng)
                                        .ts(startTime.plusMinutes(i * 5)).build();
                        tracks.add(track);
                }

                locationTrackRepository.saveAll(tracks); // 배치 저장!
        }
}
