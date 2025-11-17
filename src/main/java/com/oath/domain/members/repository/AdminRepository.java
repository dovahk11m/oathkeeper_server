package com.oath.domain.members.repository;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.ActiveChartDto;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.visitors.VisitorResponse;
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

}
