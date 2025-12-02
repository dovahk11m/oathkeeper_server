package com.oath.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.oath.recommend_domain.plan.PlanEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(0)
public class DataInitializer0_Cleanup implements CommandLineRunner {

    private final PlanEmbeddingRepository planEmbeddingRepository;

    @Override
    @Transactional("pgTransactionManager")
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ [PG] Supabase의 기존 PlanEmbedding 데이터 삭제 시작");
        planEmbeddingRepository.deleteAllInBatch();
        log.info("👷‍♂️ [PG] PlanEmbedding 데이터 삭제 완료");
    }
}
