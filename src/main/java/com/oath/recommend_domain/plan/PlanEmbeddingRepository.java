package com.oath.recommend_domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanEmbeddingRepository extends JpaRepository<PlanEmbedding, Long> {
    /**
     * @1. 지정된 벡터(@Param("queryVector"))와 가장 유사한(코사인 거리가 가장 가까운)
     * PlanEmbedding을 상위 limit 만큼 찾는다.
     * @2. '<->' 연산자는 pgvector에서 "코사인 거리"를 계산하는 연산자
     * (유사도가 높을수록 거리는 0에 가까워짐)
     */
    @Query(value = "SELECT * FROM plan_embeddings ORDER BY embedding <-> CAST(:queryVector AS vector) LIMIT :limit",
            nativeQuery = true)
    List<PlanEmbedding> findTopSimilarPlans(
            @Param("queryVector") float[] queryVector,
            @Param("limit") Long limit
    );


    @Query("SELECT p FROM PlanEmbedding p WHERE p.planId = :planId")
    Optional<PlanEmbedding> findByPlanId(@Param("planId") Long planId);
}
