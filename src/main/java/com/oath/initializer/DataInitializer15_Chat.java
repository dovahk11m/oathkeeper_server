package com.oath.initializer;

import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.groupRepository.GroupMemberRepository; // 새로 필요
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(15)
public class DataInitializer15_Chat implements CommandLineRunner {

    private final ChatRepository chatRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository; // 추가
    private final MemberRepository memberRepository;
    private final PlanJpaRepository planRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("💬 샘플 그룹멤버 및 채팅 데이터 생성 시작");

        List<Group> groups = groupRepository.findAll();
        List<Member> members = memberRepository.findAll();
        List<Plan> plans = planRepository.findAll();

        if (groups.isEmpty() || members.isEmpty()) {
            log.warn("Group 또는 Member 데이터가 부족합니다. 채팅 생성 중단");
            return;
        }

        // --- 그룹 멤버 랜덤 생성 ---
        for (Group group : groups) {
            // 각 그룹마다 최소 2명 이상 멤버 배정
            int memberCount = 2 + random.nextInt(Math.min(5, members.size() - 1));
            for (int i = 0; i < memberCount; i++) {
                Member member = members.get(random.nextInt(members.size()));

                // 이미 등록된 멤버는 중복 방지
                boolean exists = groupMemberRepository.existsByGroupAndMember(group, member);
                if (!exists) {
                    GroupMember gm = GroupMember.of(group, member); // 생성자 또는 팩토리 메서드
                    groupMemberRepository.save(gm);
                }
            }
        }

        // --- 채팅 생성 ---
        int chatCount = 50;

        for (int i = 0; i < chatCount; i++) {
            Group group = groups.get(random.nextInt(groups.size()));
            List<Member> groupMembers = groupMemberRepository.findMembersByGroup(group.getId());

            if (groupMembers.isEmpty()) continue; // 그룹에 멤버가 없으면 스킵

            Member sender = groupMembers.get(random.nextInt(groupMembers.size()));

            Long planId = (plans.isEmpty() || random.nextBoolean())
                    ? null
                    : plans.get(random.nextInt(plans.size())).getId();

            String content = getRandomMessage(i);

            Chat chat = Chat.of(group, sender, content, planId);
            chatRepository.save(chat);
        }

        log.info("💬 샘플 그룹멤버 및 채팅 데이터 생성 완료 (채팅 {}개)", chatCount);
    }

    private String getRandomMessage(int index) {
        String[] samples = {
                "안녕하세요 👋",
                "오늘 일정은 어떻게 되나요?",
                "좋아요! 오후에 회의해요.",
                "점심 뭐 먹을까요?",
                "이거 자료 공유드려요.",
                "좋은 아침이에요 ☀️",
                "오늘 회의는 취소될 수도 있어요.",
                "내일 일정 미리 조율해볼까요?",
                "그 부분은 제가 맡을게요!",
                "확인했습니다 🙌"
        };
        return samples[random.nextInt(samples.length)] + " (" + (index + 1) + ")";
    }
}
