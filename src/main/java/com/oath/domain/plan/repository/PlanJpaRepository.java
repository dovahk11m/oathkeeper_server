package com.oath.domain.plan.repository;

import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanJpaRepository extends JpaRepository<Plan, Long> {

    @Query("select distinct p from Plan p left join fetch p.participants pm left join fetch pm.member where p.id = :id")
    Optional<Plan> findByIdWithParticipants(@Param("id") Long id);

    @Query("select distinct p from Plan p left join fetch p.participants pm left join fetch pm.member where p.id in :ids")
    List<Plan> findAllByIdInWithParticipants(@Param("ids") List<Long> ids);

    @Query("select distinct p from Plan p left join fetch p.participants pm left join fetch pm.member")
    List<Plan> findAllWithParticipants();

    @Query("select distinct p from Plan p left join fetch p.participants pm left join fetch pm.member " +
            "where p.creatorMember.id = :memberId or exists (select 1 from Participant pt where pt.plan = p and pt.member.id = :memberId)")
    List<Plan> findAllByCreatorOrParticipant(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Plan p WHERE p.group.id = :groupId AND (:status IS NULL OR p.status = :status)")
    Page<Plan> findByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") Status status, Pageable pageable);

    @Query("SELECT p.id FROM Plan p WHERE p.group.id = :groupId AND p.status = 'COMPLETED'")
    List<Long> findCompletedPlanIdsByGroupId(@Param("groupId") Long groupId);
}
