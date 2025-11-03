package com.oath.domain.members.repository;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.dto.AdminResponse;
import com.oath.domain.visitors.VisitorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT new com.oath.domain.members.dto.AdminResponse$popularPlanTag (t.id, t.name, count(pp), count(p))
            FROM Plan p
            JOIN p.participants pp
            JOIN p.planTags pt
            JOIN pt.tag t
            WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate
            GROUP BY t.id, t.name
            ORDER BY count(pp) DESC, count(p) DESC
            """)
    List<AdminResponse.popularPlanTag> populrPlanTag(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
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

}
