package com.oath.domain.place_tag_plan.plan_tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;


public interface PlanTagRepository extends JpaRepository<PlanTag, Long> {
    List<PlanTag> findByPlanId(Long planId);
}
