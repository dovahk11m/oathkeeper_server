package com.oath.initializer;

import com.oath.domain.terms.Term;
import com.oath.domain.terms.termRepository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("local")
@Component
@RequiredArgsConstructor
public class DataInitializer44_Term implements CommandLineRunner {

    private final TermRepository termRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 약관 데이터가 없으면 기본값 생성
        if (termRepository.count() == 0) {
            termRepository.saveAll(List.of(
                    Term.builder().title("서비스 이용약관").content("서비스 이용약관 내용입니다...").isRequired(true).build(),
                    Term.builder().title("개인정보 수집 및 이용 동의").content("개인정보 수집 및 이용 동의 내용입니다...").isRequired(true).build(),
                    Term.builder().title("개인정보 제3자 정보제공 동의").content("개인정보 제3자 정보제공 동의 내용입니다...").isRequired(true).build(),
                    Term.builder().title("위치기반 서비스 이용약관 동의").content("위치기반 서비스 이용약관 동의 내용입니다...").isRequired(true).build()
            ));
        }
    }
}
