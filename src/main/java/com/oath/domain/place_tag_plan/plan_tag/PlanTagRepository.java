package com.oath.domain.place_tag_plan.plan_tag;

import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public interface PlanTagRepository extends JpaRepository<PlanTag, Long> {
    List<PlanTag> findByPlanId(Long planId);


}
