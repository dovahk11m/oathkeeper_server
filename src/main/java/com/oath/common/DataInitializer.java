package com.oath.common;

import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.groups.groupRepository.GroupMemberRepository;
import com.oath.domain.groups.groupService.GroupService;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.SocialType;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 개발 환경에서 애플리케이션 시작 시 테스트용 샘플 데이터를 생성합니다.
 * 이 클래스는 'local' 프로필이 활성화된 경우에만 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer implements CommandLineRunner {

    // --- [H2 DB 소속 빈] ---
    private final MemberRepository memberRepository;
    private final PlanJpaRepository planJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatRepository chatRepository;

    private final PlanEmbeddingRepository planEmbeddingRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
        // 🚀 이게 핵심! 🚀
        // H2 DB는 어차피 재시작할 때마다 날아가지만,
        // Supabase DB는 데이터가 남아있으므로, 초기화 코드가 실행되기 전에
        // Supabase 테이블을 수동으로 먼저 비워준다!
        log.info("[PG] Supabase의 기존 PlanEmbedding 데이터를 먼저 삭제합니다...");
        planEmbeddingRepository.deleteAllInBatch(); // (싹 비우기)
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        log.info("개발 환경 샘플 데이터를 생성합니다...");

        // 1. 샘플 사용자 생성
        Member user1 = createMember(
                "user1@test.com",
                "user1",
                "1234",
                Role.USER
        );
        Member user2 = createMember(
                "user2@test.com",
                "user2",
                "1234",
                Role.USER
        );
        Member admin = createMember(
                "admin@test.com",
                "admin",
                "1234",
                Role.ADMIN
        );

        // 2. 샘플 그룹 및 채팅방 생성 (GroupService 사용)
        Group sampleGroup = groupService.createGroup(
                new GroupCreateRequest("샘플 그룹"),
                user1.getEmail()
        );

        // 3. 그룹에 멤버 추가
        // GroupMember.of() 정적 팩토리 메서드를 사용하여 user2를 멤버로 추가합니다.
        groupMemberRepository.save(GroupMember.of(
                sampleGroup,
                user2
        ));

        // 5. 샘플 채팅 메시지 생성 (user1, user2가 모두 참여한 후)
        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user1,
                "안녕하세요! 샘플 데이터입니다.",
                null,
                LocalDateTime.now()
                        .minusMinutes(5)
        ));
        chatRepository.save(new Chat(
                null,
                sampleGroup,
                user2,
                "네, 반갑습니다!",
                null,
                LocalDateTime.now()
                        .minusMinutes(4)
        ));

        // ===========================================
        // 🚀 Plan / PlanEmbedding 저장 테스트 시작
        // ===========================================

        // 1. [H2 DB] 'Plan' 엔티티 생성 및 저장
        log.info("[H2] Plan 저장을 시도합니다...");
        Plan plan = Plan.builder()
                .creatorMember(user1)
                .title("저녁에 치맥하실 분 (테스트)")
                .planDatetime(LocalDateTime.now().plusHours(5))
                .status(Status.PLANNING) // ⬅️ 우리가 만든 공용 Enum 사용
                .lateFineAmount(5000L)
                .build();

        // ⬇️ H2 DB의 'plan_tb'에 저장!
        Plan savedPlan = planJpaRepository.save(plan);
        log.info("✅ [H2] Plan 저장 성공! (ID: {})", savedPlan.getId());


        // 2. [PG DB] 'PlanEmbedding' 엔티티 생성
        log.info("[PG] PlanEmbedding 저장을 시도합니다...");

        // (임시) AI가 만들어준 가짜 벡터
        float[] fakeEmbedding = new float[768];
        fakeEmbedding[0] = 0.1f; // (테스트용 가짜 값)

        PlanEmbedding embedding = PlanEmbedding.builder()
                .planId(savedPlan.getId()) // ⬅️ [핵심!] H2 DB의 Plan ID를 링크
                .embedding(fakeEmbedding)
                // 캐시 데이터 복사
                .planDatetime(savedPlan.getPlanDatetime())
                .status(savedPlan.getStatus())
                .placeLatitude(savedPlan.getPlaceLatitude())
                .placeLongitude(savedPlan.getPlaceLongitude())
                .build();

        // ⬇️ PG DB의 'plan_embeddings'에 저장!
        // (이 시점엔 ddl-auto가 이미 끝나서 테이블이 존재함)
        planEmbeddingRepository.save(embedding);
        log.info("✅ [PG] PlanEmbedding 저장 성공! (Plan ID: {})", savedPlan.getId());

        // ===========================================
        // 🚀 테스트 종료
        // ===========================================

        log.info("샘플 데이터 생성이 완료되었습니다.");
    }

    private Member createMember(
            String email,
            String username,
            String password,
            Role role
    ) {
        return memberRepository.save(Member.builder()
                                             .email(email)
                                             .username(username)
                                             .password(passwordEncoder.encode(password))
                                             .role(role)
                                             .status(Status.ACTIVE) // 상태를 ACTIVE로 설정
                                             .socialType(SocialType.LOCAL)
                                             .socialId(email)
                                             .createdAt(LocalDateTime.now())
                                             .updatedAt(LocalDateTime.now())
                                             .lastLogin(LocalDateTime.now()) // 마지막 로그인 시간 초기화
                                             .build());
    }
}