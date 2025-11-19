package com.oath.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.plan " +
            "WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.plan " +
            "WHERE r.plan.id = :planId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByPlanIdWithDetails(@Param("planId") Long planId);

    @Query(value = "SELECT r FROM Review r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.plan " +
            "WHERE r.author.id = :authorId " +
            "ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.author.id = :authorId")
    Page<Review> findByAuthorIdWithDetails(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.plan.id = :planId AND r.author.id = :authorId")
    boolean existsByPlanIdAndAuthorId(@Param("planId") Long planId, @Param("authorId") Long authorId);
}
