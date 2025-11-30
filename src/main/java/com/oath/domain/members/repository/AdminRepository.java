package com.oath.domain.members.repository;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.ActiveChartDto;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.visitors.VisitorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT new com.oath.domain.members.dto.AdminResponse$popularPlanTag (t.id, t.name, count(pp), count(p))
            FROM Plan p
            LEFT JOIN p.participants pp
            LEFT JOIN p.planTags pt
            LEFT JOIN pt.tag t
            WHERE p.createdAt BETWEEN :startDate AND :endDate
            GROUP BY t.id, t.name
            ORDER BY count(pp) DESC, count(p) DESC
            """)
    List<AdminResponse.popularPlanTag> populrPlanTag(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

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

//    @Query("""
//            SELECT new com.oath.domain.members.dto.AdminResponse$popularPlaceTag (t.id, t.name, count())
//            FROM Tag t
//            JOIN t.placeTags pt
//            WHERE p.createdAt BETWEEN :startDate AND :endDate
//            GROUP BY t.id, t.name
//            ORDER BY count(p) DESC
//            """)
//    List<AdminResponse> popularPlaceTag(
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate
//    );

    @Query("""
            SELECT new com.oath.domain.members.dto.ActiveChartDto(
                HOUR(c.sentAt), DAY_OF_WEEK(c.sentAt), COUNT(c.id)
            )
            FROM Chat c
            GROUP BY HOUR(c.sentAt), DAY_OF_WEEK(c.sentAt)
            ORDER BY HOUR(c.sentAt), DAY_OF_WEEK(c.sentAt)
            """)
    List<ActiveChartDto> activeChart();

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
            SELECT new com.oath.domain.members.dto.AdminResponse$PlanDto(
                p.id, p.creatorMember.id, p.planDatetime, p.placeName, p.title, null, null
            )
            FROM Plan p
            JOIN GroupMember gm ON p.creatorMember.id = gm.member.id
            WHERE gm.group.id = :groupId
            """)
    List<AdminResponse.PlanDto> getPlanList(@Param("groupId") Long groupId);

    @Query("""
            SELECT tag.name, createdAt, COUNT(pt.id)
            FROM PlanTag pt
            WHERE createdAt BETWEEN :startDate AND :endDate
            GROUP BY tag, createdAt
            ORDER BY COUNT(pt.id) DESC
            """)
    List<Object[]> getDailyTagCount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(
        value = """
        SELECT
            FORMATDATETIME(p.created_at, 'yyyy-MM') AS "month",
            COUNT(DISTINCT p.id) AS planCount,
            COUNT(pp.id) AS participantCount
        FROM plan_tb p
        LEFT JOIN plan_participants_tb pp
        ON p.id = pp.plan_id
        WHERE p.created_at >= DATEADD('MONTH', -5, CURRENT_DATE())
        GROUP BY "month"
        ORDER BY "month"
        """,
        nativeQuery = true
    )
    List<AdminResponse.MonthlyCount> getMonthlyCount();


    @Query("""
    SELECT new com.oath.domain.members.dto.AdminResponse$activeCount(
        c.sender.username,
        COUNT(c.id)
    )
    FROM Chat c
    WHERE c.sentAt >= :oneMonthAgo
    GROUP BY c.sender.id, c.sender.username
    ORDER BY COUNT(c.id) DESC
    """)
    List<AdminResponse.activeCount> getActiveCount(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);


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



}
