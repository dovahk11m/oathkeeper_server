package com.oath.initializer;


import com.oath.domain.terms.Term;
import com.oath.domain.terms.termRepository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
// @Order(44) 제거
public class DataInitializer44_Term {

    private final TermRepository termRepository;

    @Transactional
    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 약관 데이터 생성 시작");

        if (termRepository.count() == 0) {
            termRepository.save(Term.builder()
                    .title("서비스 이용약관")
                    .content("서비스 이용약관 내용입니다. 동의해주세요.")
                    .isRequired(true)
                    .build());
            termRepository.save(Term.builder()
                    .title("개인정보 처리방침")
                    .content("개인정보 처리방침 내용입니다. 동의해주세요.")
                    .isRequired(true)
                    .build());
            termRepository.save(Term.builder()
                    .title("마케팅 정보 수신 동의")
                    .content("마케팅 정보 수신 동의 내용입니다. 선택 사항입니다.")
                    .isRequired(false)
                    .build());
            termRepository.save(Term.builder()
                    .title("위치 정보 이용 동의")
                    .content("위치 정보 이용 동의 내용입니다. 선택 사항입니다.")
                    .isRequired(false)
                    .build());
        }

        log.info("👷‍♂️ 샘플 약관 데이터 생성 완료");
    }
}
