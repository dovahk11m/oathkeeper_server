package com.oath.domain.plan.controller;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception401;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.facade.ParticipantFacade;
import com.oath.domain.plan.facade.PlanFacade;
import com.oath.domain.plan.request.ParticipantResponse;
import com.oath.domain.plan.request.PlanRequest;
import com.oath.domain.plan.request.PlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Tag(name = "Plan API", description = "약속 관련 API")
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class PlanRestController {

    private final PlanFacade planFacade;
    private final ParticipantFacade participantFacade;
    private final MemberRepository memberRepository;

    private Member getCurrentMember(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception401("사용자를 찾을 수 없습니다."));
    }


    // 플랜 목록조회
    @Auth
    @Operation(summary = "플랜 목록 조회", description = "본인이 생성하거나 참여한 플랜 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<PlanResponse.CreatePlan>>> listPlans(HttpServletRequest request) {
        Member currentMember = getCurrentMember(request);
        List<PlanResponse.CreatePlan> dtos = planFacade.listPlans(currentMember.getId());
        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 추천 플랜 목록 조회
    @Auth
    @Operation(summary = "추천 플랜 목록 조회", description = "사용자에게 추천하는 플랜 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 플랜 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("recommend")
    public ResponseEntity<CommonResponse<List<PlanResponse.CreatePlan>>> listRecommendPlans(
            @RequestParam("currentPlanId") Long currentPlanId,
            @RequestParam("limit") Long limit
    ) {

        List<PlanResponse.CreatePlan> plans = planFacade.listRecommendPlans(
                currentPlanId,
                limit
        );
        return ResponseEntity.ok(CommonResponse.success(
                plans,
                plans.size() + "개의 플랜이 추천 검색되었습니다."
        ));
    }

    // 플랜 조회
    @Auth
    @Operation(summary = "플랜 상세 조회", description = "특정 플랜의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> getPlan(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        planFacade.validatePlanAccess(
                id,
                currentMember.getId()
        );
        PlanResponse.CreatePlan dto = planFacade.getPlanById(id);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    private Status parseStatusOrThrow(
            String statusStr,
            Status defaultStatus
    ) {
        if (statusStr == null) return defaultStatus;
        try {
            return Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new Exception400("상태 값이 올바르지 않습니다.");
        }
    }

    private LocalDateTime parseDateTimeOrThrow(String dtStr) {
        if (dtStr == null) return null;
        try {
            return LocalDateTime.parse(dtStr);
        } catch (DateTimeParseException e) {
            throw new Exception400("잘못된 날짜 형식입니다.");
        }
    }

    // 플랜 생성
    @Auth
    @Operation(summary = "플랜 생성", description = "새로운 플랜을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> createPlan(@RequestBody PlanRequest.CreatePlanRequest req) {
        LocalDateTime dt = parseDateTimeOrThrow(req.planDatetime);
        Status status = parseStatusOrThrow(
                req.status,
                Status.PLANNING
        );
        PlanResponse.CreatePlan dto = planFacade.createPlan(
                req.creatorMemberId,
                req.title,
                dt,
                status,
                req.lateFineAmount
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 플랜 수정
    @Auth
    @Operation(summary = "플랜 수정", description = "특정 플랜의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 수정 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> updatePlan(
            @PathVariable("id") Long id,
            @RequestBody PlanRequest.UpdatePlanRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        planFacade.validatePlanCreator(
                id,
                currentMember.getId()
        );
        LocalDateTime dt = parseDateTimeOrThrow(req.planDatetime);
        Status status = null;
        if (req.status != null) status = parseStatusOrThrow(
                req.status,
                null
        );
        PlanResponse.CreatePlan dto = planFacade.updatePlan(
                id,
                req.title,
                dt,
                status,
                req.tags
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 플랜 삭제
    @Auth
    @Operation(summary = "플랜 삭제", description = "특정 플랜을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 삭제 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Object>> deletePlan(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        planFacade.validatePlanCreator(
                id,
                currentMember.getId()
        );
        planFacade.deletePlan(id);
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "삭제되었습니다."
        ));
    }

    // 참가자 추가
    @Auth
    @Operation(summary = "참가자 추가", description = "플랜에 참가자를 추가합니다. 플랜 생성자만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 추가 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 가능"),
            @ApiResponse(responseCode = "404", description = "플랜 또는 멤버를 찾을 수 없음")
    })
    @PostMapping("/{planId}/participants")
    public ResponseEntity<CommonResponse<ParticipantResponse>> addParticipant(
            @PathVariable(name = "planId") Long planId,
            @RequestBody PlanRequest.ParticipantAddRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        ParticipantResponse dto = participantFacade.addParticipant(
                planId,
                req.memberId,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 참가자 삭제
    @Auth
    @Operation(summary = "참가자 삭제", description = "플랜에서 참가자를 삭제합니다. 플랜 생성자 또는 본인만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @DeleteMapping("/{planId}/participants/{participantId}")
    public ResponseEntity<CommonResponse<Object>> removeParticipant(
            @PathVariable Long participantId,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        participantFacade.removeParticipant(
                participantId,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "참가자가 삭제되었습니다."
        ));
    }

    // 참가자 상태 변경
    @Auth
    @Operation(summary = "참가자 상태 변경", description = "참가자의 플랜 참여 상태(수락/거절 등)를 변경합니다. 본인만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "상태 값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @PutMapping("/participants/{participantId}/status")
    public ResponseEntity<CommonResponse<ParticipantResponse>> changeParticipantStatus(
            @PathVariable Long participantId,
            @RequestBody PlanRequest.ParticipantStatusRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        ParticipantStatus status;
        try {
            status = ParticipantStatus.valueOf(req.status);
        } catch (IllegalArgumentException e) {
            throw new Exception400("상태 값이 올바르지 않습니다.");
        }
        ParticipantResponse dto = participantFacade.changeParticipantStatus(
                participantId,
                status,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 참가자 목록
    @Auth
    @Operation(summary = "참가자 목록 조회", description = "특정 플랜의 참가자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @GetMapping("/{planId}/participants")
    public ResponseEntity<CommonResponse<List<ParticipantResponse>>> getParticipants(
            @PathVariable Long planId,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        planFacade.validatePlanAccess(
                planId,
                currentMember.getId()
        );
        List<ParticipantResponse> dtos = participantFacade.getParticipants(planId);
        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 출발 스타트 (시간 기록)
    @Auth
    @Operation(summary = "출발 시간 기록", description = "참가자가 약속 장소를 향해 출발한 시간을 기록합니다. 본인만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "출발 시간 기록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @PostMapping("/participants/{participantId}/departure")
    public ResponseEntity<CommonResponse<ParticipantResponse>> recordDeparture(
            @PathVariable Long participantId,
            @RequestBody PlanRequest.TimeRecordRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        LocalDateTime dt = parseDateTimeOrThrow(req.time);
        ParticipantResponse dto = participantFacade.recordDeparture(
                participantId,
                dt,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 도착 완료( 시간 기록)
    @Auth
    @Operation(summary = "도착 시간 기록", description = "참가자가 약속 장소에 도착한 시간을 기록합니다. 본인만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도착 시간 기록 성공"),
            @ApiResponse(responseCode = "400", description = "플랜의 약속 시간이 설정되어 있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @PostMapping("/participants/{participantId}/arrival")
    public ResponseEntity<CommonResponse<ParticipantResponse>> recordArrival(
            @PathVariable Long participantId,
            @RequestBody PlanRequest.TimeRecordRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        LocalDateTime dt = parseDateTimeOrThrow(req.time);
        ParticipantResponse dto = participantFacade.recordArrival(
                participantId,
                dt,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 예상 출발 제안
    @Auth
    @Operation(summary = "예상 출발 시간 제안", description = "참가자의 예상 출발 시간을 제안합니다. 본인만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예상 출발 시간 제안 성공"),
            @ApiResponse(responseCode = "400", description = "플랜의 약속 시간이 설정되어 있지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @PostMapping("/participants/{participantId}/suggest-departure")
    public ResponseEntity<CommonResponse<ParticipantResponse>> suggestDeparture(
            @PathVariable Long participantId,
            @RequestBody PlanRequest.SuggestDepartureRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        ParticipantResponse dto = participantFacade.suggestExpectedDeparture(
                participantId,
                req.expectedTravelTimeMinutes,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 지각 벌금 조회
    @Auth
    @Operation(summary = "지각 벌금 조회", description = "참가자의 지각 벌금을 조회합니다. 본인 또는 플랜 생성자만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지각 벌금 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
    })
    @GetMapping("/participants/{participantId}/late-fine")
    public ResponseEntity<CommonResponse<Long>> getLateFine(
            @PathVariable Long participantId,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        Long fine = planFacade.calculateLateFine(
                participantId,
                currentMember.getId()
        );
        return ResponseEntity.ok(CommonResponse.success(fine));
    }

    // 장소 확정
    @Auth
    @Operation(summary = "장소 확정", description = "플랜의 약속 장소를 확정합니다. 플랜 생성자만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장소 확정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/confirm-place")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> confirmPlace(
            @PathVariable Long planId,
            @RequestBody PlanRequest.ConfirmPlaceRequest req,
            HttpServletRequest request
    ) {
        Member currentMember = getCurrentMember(request);
        planFacade.validatePlanCreator(
                planId,
                currentMember.getId()
        );
        Point loc = (req.longitude != null && req.latitude != null) ? new Point(
                req.longitude,
                req.latitude
        ) : null;
        PlanResponse.CreatePlan dto = planFacade.confirmPlace(
                planId,
                req.placeName,
                loc
        );
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 최종 확정
    @Operation(summary = "플랜 최종 확정", description = "플랜을 최종 확정 상태로 변경합니다. 플랜 생성자만 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 최종 확정 성공"),
            @ApiResponse(responseCode = "400", description = "이미 확정되거나 완료된 약속, 장소가 확정되지 않음, 참여자가 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/confirm")
    public ResponseEntity<?> confirmPlan(
            @PathVariable Long planId,
            HttpServletRequest request
    ) {
        Plan confirmedPlan = planFacade.confirmFinalPlan(
                planId,
                getCurrentMember(request).getId()
        );
        return ResponseEntity.ok(CommonResponse.success(
                confirmedPlan,
                "약속이 최종 확정되었습니다."
        ));
    }
}
