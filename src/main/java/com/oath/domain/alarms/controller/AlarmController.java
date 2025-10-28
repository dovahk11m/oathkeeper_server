package com.oath.domain.alarms.controller;

import com.oath.common.CommonResponse;
import com.oath.common.auth.Auth;
import com.oath.common.exception.Exception401;
import com.oath.domain.alarms.strategies.alarmlocals.AlarmLocalEntity;
import com.oath.domain.alarms.strategies.alarmlocals.AlarmLocalRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {

    private final AlarmLocalRepository alarmLocalRepository;
    private final MemberRepository memberRepository;

    private Member getCurrentMember(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception401("사용자를 찾을 수 없습니다."));
    }

    /**
     * WebSocket 연결 정보 제공
     */
    @Auth
    @GetMapping("/ws-info")
    public ResponseEntity<CommonResponse<Map<String, String>>> getWebSocketInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("endpoint", "/ws-stomp");
        info.put("subscribe", "/topic/alarms/{userEmail}");
        info.put("description", "WebSocket 연결 후 /topic/alarms/{본인이메일}을 구독하세요");

        return ResponseEntity.ok(CommonResponse.success(info));
    }

    /**
     * 본인의 알람 목록 조회
     */
    @Auth
    @GetMapping
    public ResponseEntity<CommonResponse<List<AlarmLocalEntity>>> getMyAlarms(HttpServletRequest request) {
        Member currentMember = getCurrentMember(request);
        List<AlarmLocalEntity> alarms = alarmLocalRepository.findByMemberOrderByCreatedAtDesc(currentMember);
        return ResponseEntity.ok(CommonResponse.success(alarms));
    }

    /**
     * 읽지 않은 알람 개수 조회
     */
    @Auth
    @GetMapping("/unread-count")
    public ResponseEntity<CommonResponse<Long>> getUnreadCount(HttpServletRequest request) {
        Member currentMember = getCurrentMember(request);
        Long count = alarmLocalRepository.countByMemberAndIsReadFalse(currentMember);
        return ResponseEntity.ok(CommonResponse.success(count));
    }
}

