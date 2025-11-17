package com.oath.initializer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Profile; // @Profile("local") 제거
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
// @Profile("local") // local 프로필에서 제외하여 pg Bean 오류 방지
public class DataInitializer0_Cleanup implements CommandLineRunner {

    @PersistenceContext(unitName = "pg")
    private EntityManager pgEm;

    @Override
    @Transactional("pgTransactionManager")
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ [PG] Supabase의 기존 PlanEmbedding 데이터 삭제 시작");
        pgEm.createNativeQuery("delete from oath.plan_embeddings").executeUpdate();
        log.info("👷‍♂️ [PG] PlanEmbedding 데이터 삭제 완료");
    }
}
