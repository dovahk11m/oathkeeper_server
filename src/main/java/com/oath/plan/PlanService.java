package com.oath.plan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanJpaRepository planJpaRepository;
    // 플랜 조회
    public Plan getPlanById(Long planId) {
        return planJpaRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("해당 플랜을 찾을 수 없습니다."));
    }
    // 플랜 생성
    public Plan createPlan(Long planId) {
        Plan plan = planJpaRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("해당 플랜을 찾을 수 없습니다."));

        return planJpaRepository.save(plan);
    }



}
