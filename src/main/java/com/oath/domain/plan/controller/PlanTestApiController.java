package com.oath.domain.plan.controller;

import com.oath.common.CommonResponse;
import com.oath.domain.locationevents.event.GpsStationaryEvent;
import com.oath.domain.locationevents.event.ParticipantArrivedEvent;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.facade.PlanFacade;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Plan Test API", description = "약속 테스트 관련 API (Local 환경 전용)")
@RestController
@RequestMapping("/api/plans") // 경로 변경
@RequiredArgsConstructor
@Profile("local") // 'local' 프로파일에서만 활성화
public class PlanTestApiController {

    private final PlanFacade planFacade;
    private final PlanJpaRepository planJpaRepository; // PlanJpaRepository 주입
    private final ParticipantRepository participantRepository; // ParticipantRepository 주입
    private final ApplicationEventPublisher eventPublisher; // ApplicationEventPublisher 주입

    @Operation(summary = "[테스트용] 플랜 수동 완료", description = "지정된 플랜을 수동으로 완료 상태로 변경합니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플랜 수동 완료 성공"),
            @ApiResponse(responseCode = "400", description = "이미 완료된 약속"),
            @ApiResponse(responseCode = "403", description = "플랜 생성자만 가능"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/test/complete-manually") // 경로 변경
    public ResponseEntity<CommonResponse<Plan>> completePlanManuallyForTest(
            @PathVariable Long planId,
            @RequestParam Long requesterId
    ) {
        Plan completedPlan = planFacade.completePlanManually(
                planId,
                requesterId
        );
        return ResponseEntity.ok(CommonResponse.success(
                completedPlan,
                "플랜이 수동으로 완료되었습니다."
        ));
    }

    @Operation(summary = "[테스트용] 모든 참가자 도착 처리", description = "지정된 플랜의 모든 참가자를 도착 처리하고 플랜 완료를 시도합니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 참가자 도착 처리 성공"),
            @ApiResponse(responseCode = "404", description = "플랜을 찾을 수 없음")
    })
    @PostMapping("/{planId}/test/mark-all-arrived") // 경로 변경
    public ResponseEntity<CommonResponse<String>> markAllParticipantsArrivedForTest(
            @PathVariable Long planId,
            @RequestParam Long requesterId
    ) { // requesterId 추가
        planFacade.markAllArrivedForTest(
                planId,
                requesterId
        ); // requesterId 전달
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "모든 참가자가 도착 처리되었습니다."
        ));
    }

    @Operation(summary = "[테스트용] 참가자 강제 정체 이벤트 발생", description = "특정 참가자를 강제로 정체 상태로 만들고 GpsStationaryEvent를 발생시킵니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정체 이벤트 발생 성공"),
            @ApiResponse(responseCode = "404", description = "플랜 또는 참가자를 찾을 수 없음")
    })
    @PostMapping("/{planId}/participants/{participantId}/test/force-stationary")
    public ResponseEntity<CommonResponse<String>> forceStationaryEvent(
            @PathVariable Long planId,
            @PathVariable Long participantId,
            @RequestParam(defaultValue = "35.1234") double lat,
            @RequestParam(defaultValue = "129.5678") double lng,
            @RequestParam(defaultValue = "5") long durationMinutes
    ) {
        com.oath.domain.plan.domain.Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new com.oath.common.exception.Exception404("참가자를 찾을 수 없습니다: " + participantId));

        eventPublisher.publishEvent(new GpsStationaryEvent(
                planId,
                participantId,
                participant.getMember()
                        .getId(),
                participant.getMember()
                        .getUsername(),
                lat, // lat
                lng, // lng
                LocalDateTime.now()
                        .minusMinutes(durationMinutes),
                durationMinutes
        ));
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "GPS 정체 이벤트 발생 성공"
        ));
    }

    @Operation(summary = "[테스트용] 참가자 강제 도착 이벤트 발생", description = "특정 참가자를 강제로 도착 상태로 만들고 ParticipantArrivedEvent를 발생시킵니다. (Local 환경 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도착 이벤트 발생 성공"),
            @ApiResponse(responseCode = "404", description = "플랜 또는 참가자를 찾을 수 없음")
    })
    @PostMapping("/{planId}/participants/{participantId}/test/force-arrived")
    public ResponseEntity<CommonResponse<String>> forceArrivedEvent(
            @PathVariable Long planId,
            @PathVariable Long participantId,
            @RequestParam(defaultValue = "35.1234") double lat,
            @RequestParam(defaultValue = "129.5678") double lng
    ) {
        com.oath.domain.plan.domain.Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new com.oath.common.exception.Exception404("참가자를 찾을 수 없습니다: " + participantId));

        eventPublisher.publishEvent(new ParticipantArrivedEvent(
                planId,
                participantId,
                participant.getMember()
                        .getId(),
                participant.getMember()
                        .getUsername(),
                lat, // lat
                lng, // lng
                LocalDateTime.now()
        ));
        return ResponseEntity.ok(CommonResponse.success(
                null,
                "참가자 도착 이벤트 발생 성공"
        ));
    }
}
