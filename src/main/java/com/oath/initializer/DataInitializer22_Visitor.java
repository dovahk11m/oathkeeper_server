package com.oath.initializer;


import com.oath.domain.visitors.Visitor;
import com.oath.domain.visitors.VisitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
// @Order(22) 제거
public class DataInitializer22_Visitor {

    private final VisitorRepository visitorRepository;

    @Transactional
    public void initialize(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 방문자 데이터 생성 시작");

        for (int i = 0; i < 10; i++) {
            visitorRepository.save(Visitor.builder()
                    .ipAddress("192.168.0." + i)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36")
                    .visitedDate(LocalDate.now().minusDays(i))
                    .build());
        }

        log.info("👷‍♂️ 샘플 방문자 데이터 생성 완료");
    }
}
