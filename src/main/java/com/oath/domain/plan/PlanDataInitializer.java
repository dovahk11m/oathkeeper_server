package com.oath.domain.plan;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.domain.Tag;
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

        memberRepository.saveAll(Arrays.asList(alice, bob, carol));


        Plan studyPlan = Plan.builder()
                .creatorMember(alice)
                .title("자바 스터디 모임")
                .planDatetime(now.plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0))
                .status(Status.PLANNING)
                .lateFineAmount(2000L)
                .build();


        studyPlan.addTag(Tag.builder().tagName("스터디").build());
        studyPlan.addTag(Tag.builder().tagName("자바").build());

        planJpaRepository.save(studyPlan);


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

        participantRepository.saveAll(Arrays.asList(pm1, pm2));

        //  점심 모임
        Plan lunchPlan = Plan.builder()
                .creatorMember(bob)
                .title("한식 점심 모임")
                .planDatetime(now.plusDays(2).withHour(12).withMinute(30).withSecond(0).withNano(0))
                .status(Status.CONFIRMED)
                .lateFineAmount(1000L)
                .build();

        lunchPlan.addTag(Tag.builder().tagName("점심").build());
        lunchPlan.addTag(Tag.builder().tagName("한식").build());

        planJpaRepository.save(lunchPlan);

        Participant pm3 = Participant.builder()
                .plan(lunchPlan)
                .member(alice)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .transportMethod("도보")
                .startAddress("회사 건물")
                .expectedTravelTimeMinutes(5)
                .build();

        participantRepository.save(pm3);

        //영화
        Plan moviePlan = Plan.builder()
                .creatorMember(carol)
                .title("영화 관람")
                .planDatetime(now.minusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0))
                .status(Status.COMPLETED)
                .lateFineAmount(0L)
                .build();

        moviePlan.addTag(Tag.builder().tagName("영화").build());
        planJpaRepository.save(moviePlan);

        // 참가자(완료된 플랜에 지각/도착 정보 포함)
        Participant pm4 = Participant.builder()
                .plan(moviePlan)
                .member(alice)
                .participantStatus(ParticipantStatus.ACCEPTED)
                .actualDepartureTime(now.minusDays(1).withHour(19).withMinute(30))
                .actualArrivalTime(now.minusDays(1).withHour(20).withMinute(5))
                .timeBurdenMinutes(5)
                .build();

        participantRepository.save(pm4);
    }
}
