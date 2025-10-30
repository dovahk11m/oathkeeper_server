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
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    long countByIpAddressAndVisitedDate(String ipAddress, LocalDate visitedDate);

    long countByVisitedDate(LocalDate visitedDate);

    @Query(""" 
            SELECT new com.oath.domain.visitors.VisitorResponse (v.visitedDate, count(v))
            FROM Visitor v
            WHERE v.visitedDate BETWEEN :startDate AND :endDate
            GROUP BY v.visitedDate
            ORDER BY v.visitedDate DESC
            """)
    List<VisitorResponse> findByVisitedDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
        );
}
