package com.oath.domain.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {

    @Query("SELECT r FROM Reply r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.review " +
            "WHERE r.id = :id")
    Optional<Reply> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Reply r WHERE r.review.id = :reviewId")
    Optional<Reply> findByReviewId(@Param("reviewId") Long reviewId);
}

