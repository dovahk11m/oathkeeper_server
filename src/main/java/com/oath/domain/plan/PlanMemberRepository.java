package com.oath.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanMemberRepository extends JpaRepository<PlanMember, Long> {
    List<PlanMember> findByPlanId(Long planId);
}

