package com.oath.domain.members.repository;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT new com.oath.domain.members.dto.AdminResponse$PlanTagPie (t.name, count(p))
            FROM Plan p
            LEFT JOIN p.planTags pt
            LEFT JOIN pt.tag t
            WHERE p.createdAt BETWEEN :startDate AND :endDate
            GROUP BY t.name
            ORDER BY count(p) DESC
            """)
    List<AdminResponse.PlanTagPie> PlanTagPie(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
            SELECT new com.oath.domain.members.dto.AdminResponse$ChatMemberDto (
                m.id, m.username, m.profileImageUrl, gm.group.id
            )
            FROM Member m
            JOIN GroupMember gm ON m.id = gm.member.id
            WHERE gm.group.id = :groupId
            """)
    List<AdminResponse.ChatMemberDto> chatMember(@Param("groupId") Long groupId);

    @Query("""
    SELECT new com.oath.domain.members.dto.AdminResponse$GroupList(
        g.id, g.name, g.createdAt, COUNT(gm.id),
        (SELECT MAX(c.sentAt) FROM Chat c WHERE c.group.id = g.id)
    )
    FROM Group g
    JOIN GroupMember gm
    ON g.id = gm.group.id
    GROUP BY g.id, g.name, g.createdAt
    ORDER BY g.id DESC
    """)
    Page<AdminResponse.GroupList> getGroupList(Pageable pagable);

    @Query("""
        SELECT new com.oath.domain.members.dto.AdminResponse$GroupList(
            g.id,
            g.name,
            g.createdAt,
            (SELECT COUNT(gm2.id) FROM GroupMember gm2 WHERE gm2.group.id = g.id),
            (SELECT MAX(c.sentAt) FROM Chat c WHERE c.group.id = g.id)
        )
        FROM Group g
        WHERE g.name LIKE %:keyword%
        """)
    Page<AdminResponse.GroupList> findByGroupName(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT new com.oath.domain.members.dto.AdminResponse$GroupList(
            g.id,
            g.name,
            g.createdAt,
            (SELECT COUNT(gm2.id) FROM GroupMember gm2 WHERE gm2.group.id = g.id),
            (SELECT MAX(c.sentAt) FROM Chat c WHERE c.group.id = g.id)
        )
        FROM Group g
        WHERE EXISTS (
            SELECT 1 FROM GroupMember gm
             WHERE gm.group.id = g.id
               AND gm.member.email LIKE %:keyword%
        )
        """)
    Page<AdminResponse.GroupList> findByMemberEmail(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = """
    SELECT
        monthWeek,
        SUM(planCount) OVER (ORDER BY minDate) AS PlanCount
    FROM (
        SELECT
            CONCAT(MONTH(p.created_at), '월 ', FLOOR((DAY(p.created_at)-1)/7)+1, '주') AS monthWeek,
            COUNT(DISTINCT p.id) AS planCount,
            MIN(p.created_at) AS minDate
        FROM plan_tb p
        WHERE p.created_at >= DATEADD('MONTH', -2, CURRENT_DATE())
        GROUP BY monthWeek
    ) AS weekly
    ORDER BY minDate
    """,
            nativeQuery = true
    )
    List<AdminResponse.PlanCount> getPlanCount();

    @Query(
            value = """
    SELECT
        monthWeek,
        SUM(participantCount) OVER (ORDER BY minDate) AS ParticipantCount
    FROM (
        SELECT
            CONCAT(MONTH(p.created_at), '월 ', FLOOR((DAY(p.created_at)-1)/7)+1, '주') AS monthWeek,
            COUNT(pp.id) AS participantCount,
            MIN(p.created_at) AS minDate
        FROM plan_tb p
        LEFT JOIN plan_participants_tb pp
            ON p.id = pp.plan_id
        WHERE p.created_at >= DATEADD('MONTH', -2, CURRENT_DATE())
        GROUP BY monthWeek
    ) AS weekly
    ORDER BY minDate
    """,
            nativeQuery = true
    )
    List<AdminResponse.ParticipantCount> getParticipantCount();

    @Query("""
    SELECT m
    FROM Member m
    WHERE (:type = 'username' AND m.username LIKE %:keyword%)
       OR (:type = 'email' AND m.email LIKE %:keyword%)
    """)
    Page<Member> searchMember(
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT new com.oath.domain.members.dto.AdminResponse$ChatListDto (
        c.group.id, COUNT(DISTINCT c.id), COUNT(DISTINCT gm.member.id), MAX(c.sentAt)
    )
    FROM Chat c
    JOIN GroupMember gm
    ON c.group.id = gm.group.id
    WHERE c.group.id = :groupId
    GROUP BY c.group.id
    """)
    List<AdminResponse.ChatListDto> getChatList(@Param("groupId") Long groupId);
}
