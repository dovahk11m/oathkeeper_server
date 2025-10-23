package com.oath.domain.plan;

import com.oath.common.CommonResponse;
import com.oath.common.exception.Exception400;
import com.oath.domain.plan.request.PlanMemberResponse;
import com.oath.domain.plan.request.PlanRequest;
import com.oath.domain.plan.request.PlanResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanRestController {

    private final PlanService planService;

    public PlanRestController(PlanService planService) {
        this.planService = planService;
    }

    // 플랜 목록조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<PlanResponse.CreatePlan>>> listPlans() {
        List<PlanResponse.CreatePlan> dtos = planService.listPlansDto();
        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 플랜 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> getPlan(@PathVariable("id") Long id) {
        PlanResponse.CreatePlan dto = planService.getPlanDtoById(id);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    private Status parseStatusOrThrow(String statusStr, Status defaultStatus) {
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
    @PostMapping
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> createPlan(@RequestBody PlanRequest.CreatePlanRequest req) {
        LocalDateTime dt = parseDateTimeOrThrow(req.planDatetime);
        Status status = parseStatusOrThrow(req.status, Status.PLANNING);
        PlanResponse.CreatePlan dto = planService.createPlanDto(req.creatorMemberId, req.title, dt, status, req.lateFineAmount);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 플랜 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> updatePlan(@PathVariable("id") Long id,
                                                           @RequestBody PlanRequest.UpdatePlanRequest req) {
        LocalDateTime dt = parseDateTimeOrThrow(req.planDatetime);
        Status status = null;
        if (req.status != null) status =
                parseStatusOrThrow(req.status, null);
        PlanResponse.CreatePlan dto = planService.updatePlanDto(id, req.title, dt, status, req.tags);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 플랜 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Object>> deletePlan(@PathVariable("id") Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(CommonResponse.success(null, "삭제되었습니다."));
    }

    // 참가자 추가
    @PostMapping("/{planId}/participants")
    public ResponseEntity<CommonResponse<PlanMemberResponse>> addParticipant(@PathVariable Long planId,
                                                                     @RequestBody PlanRequest.ParticipantAddRequest req) {
        PlanMemberResponse dto = planService.addParticipantDto(planId, req.memberId);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 참가자 삭제
    @DeleteMapping("/{planId}/participants/{participantId}")
    public ResponseEntity<CommonResponse<Object>> removeParticipant(@PathVariable Long participantId) {
        planService.removeParticipant(participantId);
        return ResponseEntity.ok(CommonResponse.success(null, "참가자가 삭제되었습니다."));
    }

    // 참가자 상태 변경
    @PutMapping("/participants/{participantId}/status")
    public ResponseEntity<CommonResponse<PlanMemberResponse>> changeParticipantStatus(@PathVariable Long participantId, @RequestBody PlanRequest.ParticipantStatusRequest req) {
        ParticipantStatus status;
        try {
            status = ParticipantStatus.valueOf(req.status);
        } catch (IllegalArgumentException e) {
            throw new Exception400("상태 값이 올바르지 않습니다.");
        }
        PlanMemberResponse dto = planService.changeParticipantStatusDto(participantId, status);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 참가자 목록
    @GetMapping("/{planId}/participants")
    public ResponseEntity<CommonResponse<List<PlanMemberResponse>>> getParticipants(@PathVariable Long planId) {
        List<PlanMemberResponse> dtos = planService.getParticipantsDto(planId);
        return ResponseEntity.ok(CommonResponse.success(dtos));
    }

    // 출발 시간 기록
    @PostMapping("/participants/{participantId}/departure")
    public ResponseEntity<CommonResponse<PlanMemberResponse>> recordDeparture(@PathVariable Long participantId, @RequestBody PlanRequest.TimeRecordRequest req) {
        LocalDateTime dt = parseDateTimeOrThrow(req.time);
        PlanMemberResponse dto = planService.recordDepartureDto(participantId, dt);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 도착 시간 기록
    @PostMapping("/participants/{participantId}/arrival")
    public ResponseEntity<CommonResponse<PlanMemberResponse>> recordArrival(@PathVariable Long participantId, @RequestBody PlanRequest.TimeRecordRequest req) {
        LocalDateTime dt = parseDateTimeOrThrow(req.time);
        PlanMemberResponse dto = planService.recordArrivalDto(participantId, dt);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 예상 출발 제안
    @PostMapping("/participants/{participantId}/suggest-departure")
    public ResponseEntity<CommonResponse<PlanMemberResponse>> suggestDeparture(@PathVariable Long participantId, @RequestBody PlanRequest.SuggestDepartureRequest req) {
        PlanMemberResponse dto = planService.suggestExpectedDepartureDto(participantId, req.expectedTravelTimeMinutes);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    // 지각 벌금 조회
    @GetMapping("/participants/{participantId}/late-fine")
    public ResponseEntity<CommonResponse<Long>> getLateFine(@PathVariable Long participantId) {
        Long fine = planService.calculateLateFine(participantId);
        return ResponseEntity.ok(CommonResponse.success(fine));
    }

    // 장소 확정
    @PostMapping("/{planId}/confirm-place")
    public ResponseEntity<CommonResponse<PlanResponse.CreatePlan>> confirmPlace(@PathVariable Long planId, @RequestBody PlanRequest.ConfirmPlaceRequest req) {
        PlanResponse.CreatePlan dto = planService.confirmPlaceDto(planId, req.placeName, req.latitude, req.longitude);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }

}
