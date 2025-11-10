package com.oath.initializer;

import com.oath.domain.members.domain.Member;

import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.plan_tag.PlanTag;
import com.oath.domain.place_tag_plan.plan_tag.PlanTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;

import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.geo.Point;
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
@Order(7)
public class DataInitializer7_PlanRandom implements CommandLineRunner {

    private final PlanJpaRepository planRepository;
    private final PlanTagRepository planTagRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 플랜 & 플랜태그 랜덤 데이터 생성 시작");

        List<Member> members = memberRepository.findAll();
        List<Tag> tags = tagRepository.findAll();

        if (members.isEmpty() || tags.isEmpty()) {
            log.warn("멤버 또는 태그 데이터가 부족합니다. 생성 중단");
            return;
        }

        int planCount = 15; // 생성할 플랜 수

        for (int i = 0; i < planCount; i++) {
            Member creator = members.get(random.nextInt(members.size()));

            Plan plan = Plan.builder()
                    .creatorMember(creator)
                    .title("더미 플랜 " + (i + 1))
                    .planDatetime(LocalDateTime.now().plusDays(random.nextInt(30)))
                    .status(Status.PLANNING)
                    .lateFineAmount(5000L + random.nextInt(10000))
                    .build();

            // 랜덤 위치 (위도: 35~38, 경도: 126~129)
            double latitude = 35 + random.nextDouble() * 3;
            double longitude = 126 + random.nextDouble() * 3;
            plan.confirmPlace("장소 " + (i + 1), new Point(longitude, latitude));

            planRepository.save(plan);

            // 랜덤 태그 1~4개 선택
            int tagCount = 1 + random.nextInt(4);
            for (int j = 0; j < tagCount; j++) {
                Tag tag = tags.get(random.nextInt(tags.size()));
                savePlanTag(plan, tag);
            }
        }

        log.info("👷‍♂️ 샘플 플랜 & 플랜태그 랜덤 데이터 생성 완료");
    }

    private void savePlanTag(Plan plan, Tag tag) {
        // 이미 연결되어 있으면 중복 방지
        boolean exists = plan.getPlanTags().stream()
                .anyMatch(pt -> pt.getTag().getId().equals(tag.getId()));
        if (exists) return;

        PlanTag planTag = PlanTag.builder()
                .plan(plan)
                .tag(tag)
                .createdAt(LocalDateTime.now())
                .build();
        plan.getPlanTags().add(planTag);
        planTagRepository.save(planTag);
    }
}
