package com.oath.recommend_domain.plan.constants;

public enum TaskType {
    /**
     * 텍스트 유사성을 평가하도록 최적화된 임베딩
     *
     * @예시 - 추천 시스템, 중복 감지
     */
    SEMANTIC_SIMILARITY,

    /**
     * 사전 설정된 라벨에 따라 텍스트를 분류하도록 최적화된 임베딩
     *
     * @예시 - 감정 분석, 스팸 감지
     */
    CLASSIFICATION,

    /**
     * 유사성을 기반으로 텍스트를 클러스터링하는 데 최적화된 임베딩
     *
     * @예시 - 문서 정리, 시장 조사, 이상 감지
     */
    CLUSTERING,

    /**
     * 문서 검색에 최적화된 임베딩
     *
     * @예시 - 검색을 위해 기사, 책 또는 웹페이지를 색인 생성
     */
    RETRIEVAL_DOCUMENT,

    /**
     * 일반 검색어에 최적화된 임베딩, 쿼리에는 RETRIEVAL_QUERY를 사용하고 검색할 문서에는 RETRIEVAL_DOCUMENT를 사용
     *
     * @예시 - 맞춤검색
     */
    RETRIEVAL_QUERY,

    /**
     * 자연어 쿼리를 기반으로 코드 블록을 검색하는 데 최적화된 임베딩, 질문에는 CODE_RETRIEVAL_QUERY를 사용하고 검색할 코드 블록에는 RETRIEVAL_DOCUMENT를 사용
     *
     * @예시 - 코드 추천 및 검색
     */
    CODE_RETRIEVAL_QUERY,

    /**
     * 질문에 답변하는 문서를 찾는 데 최적화된 질문 답변 시스템의 질문 임베딩, 질문에는 QUESTION_ANSWERING를 사용하고 검색할 문서에는 RETRIEVAL_DOCUMENT를 사용
     *
     * @예시 - 채팅 상자
     */
    QUESTION_ANSWERING,

    /**
     * 확인해야 하는 진술의 임베딩으로, 진술을 뒷받침하거나 반박하는 증거가 포함된 문서를 검색하는 데 최적화, 타겟 텍스트에는 FACT_VERIFICATION를 사용하고 검색할 문서에는 RETRIEVAL_DOCUMENT를 사용
     *
     * @예시 - 자동 사실 확인 시스템
     */
    FACT_VERIFICATION
}
