package com.oath.initializer;

import com.oath.domain.visitors.Visitor;
import com.oath.domain.visitors.VisitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(22)
public class DataInitializer22_Visitor implements CommandLineRunner {

    private final VisitorRepository visitorRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 방문자 데이터 생성 시작");

        createVisitor("127.0.0.1", "Mozilla/5.", "2025-10-01");
        createVisitor("127.0.0.2", "Mozilla/5.", "2025-10-03");
        createVisitor("127.0.0.3", "Mozilla/5.", "2025-10-05");
        createVisitor("127.0.0.4", "Mozilla/5.", "2025-10-10");
        createVisitor("127.0.0.5", "Mozilla/5.", "2025-10-11");
        createVisitor("127.0.0.6", "Mozilla/5.", "2025-10-16");
        createVisitor("127.0.0.7", "Mozilla/5.", "2025-10-20");
        createVisitor("127.0.0.8", "Mozilla/5.", "2025-10-22");
        createVisitor("127.0.0.9", "Mozilla/5.", "2025-10-25");
        createVisitor("127.0.0.10", "Mozilla/5.", "2025-10-30");

        log.info("👷‍♂️ 샘플 방문자 데이터 생성 완료");
    }

    private Visitor createVisitor(String ipAddress, String userAgent, String visitedDate) {
        return visitorRepository.save(Visitor.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .visitedDate(LocalDate.parse(visitedDate))
                .build());
    }
}
