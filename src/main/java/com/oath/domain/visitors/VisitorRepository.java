package com.oath.domain.visitors;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository {

    long countByIpAdressAndVisitedDate(String ipAddress, LocalDate visitedDate);

    long countByVisitedDate(LocalDate visitedDate);

    @Query(""" 
            SELECT new com.oath.domain.visitors.VisitCountDto (v.visitedDate, count(v) AS count)
            FROM visitor v
            WHERE v.visitedDate BETWEEN :startDate AND :endDate
            GROUP BY visitedDate
            ORDER BY visitedDate DESC
            """)
    List<VisitorResponse.PeriodCount> countVisitsGroupByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
        );
}
