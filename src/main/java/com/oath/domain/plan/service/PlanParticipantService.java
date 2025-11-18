package com.oath.domain.plan.service;

import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanParticipantService {

    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final PlanCoreService planCoreService; // PlanCoreService의 getPlanById 사용

    // 참가자 추가 (생성자만 가능)
    @Transactional
    public Participant addParticipant(
            Long planId,
            Long memberId,
            Long requesterId
    ) {
        Plan plan = planCoreService.getPlanById(planId);

        // 권한 검증: 플랜 생성자만 참가자 추가 가능
        if (!plan.getCreatorMember()
                .getId()
                .equals(requesterId)) {
            throw new Exception403("플랜 생성자만 참가자를 추가할 수 있습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        Participant participant = Participant.builder()
                .plan(plan)
                .member(member)
                .participantStatus(ParticipantStatus.PENDING)
                .startAddress(member.getDefaultAddress())
                .startLatitude(member.getDefaultLat())
                .startLongitude(member.getDefaultLng())
                .build();
        Participant saved = participantRepository.save(participant);
        plan.getParticipants()
                .add(saved);

        return saved;
    }

    // 참가자 삭제 (생성자 또는 본인만 가능)
    @Transactional
    public void removeParticipant(
            Long participantId,
            Long requesterId
    ) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("해당 참가자를 찾을 수 없습니다."));

        Plan plan = participant.getPlan();
        boolean isCreator = plan.getCreatorMember()
                .getId()
                .equals(requesterId);
        boolean isSelf = participant.getMember()
                .getId()
                .equals(requesterId);

        if (!isCreator && !isSelf) {
            throw new Exception403("플랜 생성자 또는 본인만 참가자를 삭제할 수 있습니다.");
        }

        participantRepository.deleteById(participantId);
    }

    // 참가자 상태 변경 (본인만 가능)
    @Transactional
    public Participant changeParticipantStatus(
            Long participantId,
            ParticipantStatus status,
            Long requesterId
    ) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 권한 검증: 참가자 본인만 상태 변경 가능
        if (!participant.getMember()
                .getId()
                .equals(requesterId)) {
            throw new Exception403("본인의 참가 상태만 변경할 수 있습니다.");
        }

        participant.setParticipantStatus(status);

        return participantRepository.save(participant);
    }

    // 참가자 조회
    public List<Participant> getParticipants(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        return participantRepository.findByPlanId(planId);
    }
}
