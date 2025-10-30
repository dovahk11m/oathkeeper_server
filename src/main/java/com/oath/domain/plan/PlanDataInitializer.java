package com.oath.domain.plan;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.plan_tag.PlanTag;
import com.oath.domain.place_tag_plan.plan_tag.PlanTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class PlanDataInitializer implements CommandLineRunner {

    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final PlanTagRepository planTagRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (planJpaRepository.count() > 0) return;

        // 멤버 생성
        LocalDateTime now = LocalDateTime.now();

        Member alice = Member.builder()
                .username("alice")
                .email("alice@example.com")
                .socialId("alice_local_1")
                .socialType(SocialType.LOCAL)
                .profileImageUrl(null)
                .password(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Member bob = Member.builder()
                .username("bob")
                .email("bob@example.com")
                .socialId("bob_local_1")
                .socialType(SocialType.LOCAL)
                .profileImageUrl(null)
                .password(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Member carol = Member.builder()
                .username("carol")
                .email("carol@example.com")
                .socialId("carol_local_1")
                .socialType(SocialType.LOCAL)
                .profileImageUrl(null)
                .password(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        memberRepository.saveAll(Arrays.asList(
                alice,
                bob,
                carol
        ));

        // 태그 생성 및 저장
        Tag tagStudy = Tag.builder()
                .name("스터디")
                .createdAt(now)
                .build();
        Tag tagJava = Tag.builder()
                .name("자바")
                .createdAt(now)
                .build();
        Tag tagLunch = Tag.builder()
                .name("점심")
                .createdAt(now)
                .build();
        Tag tagKoreanFood = Tag.builder()
                .name("한식")
                .createdAt(now)
                .build();
        Tag tagMovie = Tag.builder()
                .name("영화")
                .createdAt(now)
                .build();
        tagRepository.saveAll(Arrays.asList(
                tagStudy,
                tagJava,
                tagLunch,
                tagKoreanFood,
                tagMovie
        ));


        // --- 자바 스터디 모임 ---
        Plan studyPlan = Plan.builder()
                .creatorMember(alice)
                .title("자바 스터디 모임")
                .planDatetime(now.plusDays(1)
                                      .withHour(19)
                                      .withMinute(0)
                                      .withSecond(0)
                                      .withNano(0))
                .status(Status.PLANNING)
                .lateFineAmount(2000L)
                .build();
        planJpaRepository.save(studyPlan);

        // Plan과 Tag 연결 (PlanTag 사용)
        PlanTag planTag1 = PlanTag.builder()
                .plan(studyPlan)
                .tag(tagStudy)
                .build();
        PlanTag planTag2 = PlanTag.builder()
                .plan(studyPlan)
                .tag(tagJava)
                .build();
        planTagRepository.saveAll(Arrays.asList(
                planTag1,
                planTag2
        ));

        // 참가자 추가
        Participant pm1 = Participant.builder()
                .plan(studyPlan)
                .member(bob)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .transportMethod("지하철")
                .startAddress("역 근처")
                .expectedTravelTimeMinutes(20)
                .build();

        Participant pm2 = Participant.builder()
                .plan(studyPlan)
                .member(carol)
                .participantStatus(ParticipantStatus.PENDING)
                .transportMethod("버스")
                .startAddress("집 근처")
                .expectedTravelTimeMinutes(30)
                .build();

        participantRepository.saveAll(Arrays.asList(
                pm1,
                pm2
        ));

        // --- 한식 점심 모임 ---
        Plan lunchPlan = Plan.builder()
                .creatorMember(bob)
                .title("한식 점심 모임")
                .planDatetime(now.plusDays(2)
                                      .withHour(12)
                                      .withMinute(30)
                                      .withSecond(0)
                                      .withNano(0))
                .status(Status.CONFIRMED)
                .lateFineAmount(1000L)
                .build();
        planJpaRepository.save(lunchPlan);

        // Plan과 Tag 연결 (PlanTag 사용)
        PlanTag planTag3 = PlanTag.builder()
                .plan(lunchPlan)
                .tag(tagLunch)
                .build();
        PlanTag planTag4 = PlanTag.builder()
                .plan(lunchPlan)
                .tag(tagKoreanFood)
                .build();
        planTagRepository.saveAll(Arrays.asList(
                planTag3,
                planTag4
        ));

        // 참가자 추가
        Participant pm3 = Participant.builder()
                .plan(lunchPlan)
                .member(alice)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .transportMethod("도보")
                .startAddress("회사 건물")
                .expectedTravelTimeMinutes(5)
                .build();

        participantRepository.save(pm3);

        // --- 영화 관람 ---
        Plan moviePlan = Plan.builder()
                .creatorMember(carol)
                .title("영화 관람")
                .planDatetime(now.minusDays(1)
                                      .withHour(20)
                                      .withMinute(0)
                                      .withSecond(0)
                                      .withNano(0))
                .status(Status.COMPLETED)
                .lateFineAmount(0L)
                .build();
        planJpaRepository.save(moviePlan);

        // Plan과 Tag 연결 (PlanTag 사용)
        PlanTag planTag5 = PlanTag.builder()
                .plan(moviePlan)
                .tag(tagMovie)
                .build();
        planTagRepository.save(planTag5);

        // 참가자 추가 (완료된 플랜에 지각/도착 정보 포함)
        Participant pm4 = Participant.builder()
                .plan(moviePlan)
                .member(alice)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .actualDepartureTime(now.minusDays(1)
                                             .withHour(19)
                                             .withMinute(30))
                .actualArrivalTime(now.minusDays(1)
                                           .withHour(20)
                                           .withMinute(5))
                .timeBurdenMinutes(5)
                .build();

        participantRepository.save(pm4);
    }
}
