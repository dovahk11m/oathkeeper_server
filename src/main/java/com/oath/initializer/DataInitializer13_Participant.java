package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(13)
public class DataInitializer13_Participant implements CommandLineRunner {

    private final ParticipantRepository participantRepository;
    private final PlanJpaRepository planRepository;
    private final MemberRepository memberRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        log.info("🧍 참가자(Participant) 더미 데이터 생성 시작");

        List<Plan> plans = planRepository.findAll();
        List<Member> members = memberRepository.findAll();

        if (plans.isEmpty() || members.isEmpty()) {
            log.warn("❌ Plan 또는 Member 데이터가 부족합니다. Participant 생성 중단");
            return;
        }

        int count = 0;
        for (Plan plan : plans) {
            // 각 Plan당 2~4명 랜덤 참가자 생성
            int participantCount = 2 + random.nextInt(3);
            for (int i = 0; i < participantCount; i++) {
                Member member = members.get(random.nextInt(members.size()));

                // 중복 방지: 같은 플랜에 같은 멤버가 이미 참가했는지 확인
                boolean exists = participantRepository.existsByPlanAndMember(plan, member);
                if (exists) continue;

                Participant participant = Participant.builder()
                        .plan(plan)
                        .member(member)
                        .participantStatus(randomStatus())
                        .transportMethod(randomTransport())
                        .startAddress(randomAddress())
                        .startLatitude(35.15 + random.nextDouble() * 0.1)
                        .startLongitude(129.05 + random.nextDouble() * 0.1)
                        .expectedTravelTimeMinutes(20 + random.nextInt(40))
                        .expectedDepartureTime(LocalDateTime.now().plusMinutes(random.nextInt(30)))
                        .actualDepartureTime(null)
                        .actualArrivalTime(null)
                        .arrivalStatus(null)
                        .arrivalOffsetMinutes(null)
                        .timeBurdenMinutes(random.nextInt(30))
                        .departureFailureReason(null)
                        .departed(false)
                        .lastLiveTs(null)
                        .isShareLocation(true)
                        .build();

                participantRepository.save(participant);
                count++;
            }
        }

        log.info("✅ 참가자(Participant) 더미 데이터 생성 완료 (총 {}명)", count);
    }

    private ParticipantStatus randomStatus() {
        ParticipantStatus[] values = ParticipantStatus.values();
        return values[random.nextInt(values.length)];
    }

    private String randomTransport() {
        String[] transports = {"WALK", "BIKE", "CAR", "TRANSIT"};
        return transports[random.nextInt(transports.length)];
    }

    private String randomAddress() {
        String[] addresses = {
                "서울특별시 강남구 테헤란로 231",
                "부산광역시 해운대구 센텀중앙로 90",
                "대구광역시 중구 동성로 23",
                "인천광역시 연수구 송도국제대로 123",
                "대전광역시 유성구 대학로 77",
                "광주광역시 서구 상무대로 123"
        };
        return addresses[random.nextInt(addresses.length)];
    }
}
