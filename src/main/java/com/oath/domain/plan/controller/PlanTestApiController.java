package com.oath.domain.plan.controller;

import com.oath.common.CommonResponse;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.facade.PlanFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Plan Test API", description = "약속 테스트 관련 API (Local 환경 전용)")
@RestController
@RequestMapping("/api/test/plans")
@RequiredArgsConstructor
@Profile("local") // 'local' 프로파일에서만 활성화
public class PlanTestApiController {

    private final PlanFacade planFacade;

    @Operation(summary = "[테스트용] 플랜 수동 완료", description = "지정된 플랜을 수동으로 완료 상태로 변경합니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 수동 완료 성공"),
            @ApiResponse(responseCode = "400", description = "이미 완료된 약속"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/complete-manually")
    public ResponseEntity<CommonResponse<Plan>> completePlanManuallyForTest(@PathVariable Long planId, @RequestParam Long requesterId) {
        Plan completedPlan = planFacade.completePlanManually(planId, requesterId);
        return ResponseEntity.ok(CommonResponse.success(completedPlan, "플랜이 수동으로 완료되었습니다."));
    }

    @Operation(summary = "[테스트용] 모든 참가자 도착 처리", description = "지정된 플랜의 모든 참가자를 도착 처리하고 플랜 완료를 시도합니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 참가자 도착 처리 성공"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/mark-all-arrived")
    public ResponseEntity<CommonResponse<String>> markAllParticipantsArrivedForTest(@PathVariable Long planId, @RequestParam Long requesterId) { // requesterId 추가
        planFacade.markAllArrivedForTest(planId, requesterId); // requesterId 전달
        return ResponseEntity.ok(CommonResponse.success(null, "모든 참가자가 도착 처리되었습니다."));
    }
}
