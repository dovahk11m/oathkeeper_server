package com.oath.domain.terms;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.terms.dto.TermResponse;
import com.oath.domain.terms.termRepository.MemberAgreedTermRepository;
import com.oath.domain.terms.termRepository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final MemberAgreedTermRepository memberAgreedTermRepository;

    /**
     * 모든 약관 목록을 조회합니다.
     * @return 약관 DTO 리스트
     */
    public List<TermResponse> getTermsList() {
        return termRepository.findAll().stream()
                .map(TermResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 약관의 상세 정보를 조회합니다.
     * @param termId 조회할 약관의 ID
     * @return 약관 상세 정보를 담은 DTO
     */
    public TermResponse getTermById(Long termId) {
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new Exception404("해당 약관을 찾을 수 없습니다. ID: " + termId));
        return new TermResponse(term);
    }

    /**
     * 사용자의 약관 동의를 처리하고, 필수 약관에 모두 동의했는지 검증합니다.
     * @param agreedTermIds 동의한 약관 ID 목록
     * @param member 약관에 동의한 회원 엔티티
     */
    @Transactional
    public void agreeTerms(Set<Long> agreedTermIds, Member member) {
        // 1. 모든 필수 약관을 조회
        List<Term> requiredTerms = termRepository.findAllByIsRequired(true);

        // 2. 사용자가 모든 필수 약관에 동의했는지 확인
        for (Term term : requiredTerms) {
            if (!agreedTermIds.contains(term.getId())) {
                throw new Exception400(term.getTitle() + " 약관에 동의해야 합니다.");
            }
        }

        // 3. 동의 내역 저장
        List<MemberAgreedTerm> agreedTermToSave = agreedTermIds.stream()
                .map(termId -> {
                    Term term = termRepository.findById(termId)
                            .orElseThrow(() -> new Exception400("존재하지 않는 약관입니다: " + termId));
                    return MemberAgreedTerm.builder()
                            .member(member)
                            .term(term)
                            .build();
                })
                .collect(Collectors.toList());

        memberAgreedTermRepository.saveAll(agreedTermToSave);
    }
}
