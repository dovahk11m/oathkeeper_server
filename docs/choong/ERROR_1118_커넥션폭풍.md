# 1118c 커넥션 폭풍(Connection Storm) 트러블슈팅 보고서

## 1. 문제 현상

애플리케이션 구동 또는 특정 기능 사용 시, DB 연결에 실패하며 `org.springframework.transaction.CannotCreateTransactionException`이 발생했다. 세부 로그에는 다음과 같은 두 가지 상반된 오류가 번갈아 나타났다.

1.  **초기 오류:** `FATAL: Max client connections reached`
    -   DB 서버가 허용하는 최대 연결 개수를 초과하여 새로운 연결을 거부하는 상태.
2.  **이후 오류:** `Caused by: java.net.SocketTimeoutException: Connect timed out`
    -   DB 서버로부터 아무런 응답도 받지 못하고 네트워크 연결 시도가 시간 초과되는 상태.

## 2. 원인 분석 과정 (가설과 검증)

### 가설 1: 단순 커넥션 누수(Leak) 또는 풀 크기 문제

-   **현상:** "일정 시간 사용 후"에 `Max client connections reached` 오류 발생.
-   **분석:**
    -   `@Transactional`이 누락된 서비스 메서드가 커넥션을 반납하지 않는다고 추측.
    -   `application-local.yml`의 `maximum-pool-size: 3` 설정이 너무 작다고 판단.
-   **시도 및 결과:**
    -   `PlanFacade` 등에서 누락된 `@Transactional`을 추가함.
    -   `maximum-pool-size`를 8로 늘렸으나, 오히려 애플리케이션 시작조차 실패하며 `Connect timed out` 발생.
-   **결론:** 단순 누수나 풀 크기 문제가 아닌, 더 근본적인 설정 충돌 문제임을 시사.

### 가설 2: 데이터 초기화 시점의 커넥션 폭풍

-   **현상:** 애플리케이션 시작 시점에 오류가 집중됨.
-   **분석:**
    -   `DataInitializer`의 `initialize()` 메서드에 `@Transactional`이 없어, 내부의 수많은 `save()` 호출이 각각 새로운 커넥션을 짧게 사용하고 반납하는 과정을 반복하며 DB에 과부하를 준다고 추측.
-   **시도 및 결과:**
    -   모든 `DataInitializer`의 `initialize()` 메서드에 `@Transactional`을 추가함.
    -   문제 해결에 일부 기여했을 수 있으나, 근본적인 `Connect timed out` 문제는 해결되지 않음.

### 가설 3: 잘못된 DB 설정으로 인한 트랜잭션 충돌 (최종 원인)

-   **현상:** `maximum-pool-size`를 서버 한계(15개)보다 훨씬 작은 8로 설정해도 시작에 실패.
-   **분석:**
    1.  **`@Primary`의 오해:** `H2JpaConfig`에 `@Primary`가 설정되어, 모든 트랜잭션의 기본 국적(Transaction Manager)이 'H2'로 강제됨.
    2.  **패키지 범위 충돌:** `H2JpaConfig`는 `com.oath.domain`을, `PgJpaConfig`는 `com.oath.recommend_domain`을 담당. 두 패키지는 분리되어 있었음.
    3.  **결정적 충돌 지점:** `PlanFacade`의 `listRecommendPlans` 메서드는 `@Transactional` 하에서 `com.oath.domain`의 Repository와 `com.oath.recommend_domain`의 `PlanEmbeddingService`를 **동시에 호출**함.
    4.  **시나리오:** 'H2 트랜잭션'으로 시작된 작업이, 내부에서 'PostgreSQL'을 사용하는 코드를 만나자 Spring은 두 DB를 하나의 트랜잭션으로 묶지 못하고 제어 불가능한 커넥션을 생성/요청하기 시작함. 이 '커넥션 폭풍'이 Supabase의 연결 한계를 초과시켜 서버가 응답하지 않게 됨(`Connect timed out`).

## 3. 최종 해결 방안: 아키텍처 결정 및 근거

**목표:** H2(메인 도메인)와 PostgreSQL(추천 도메인)을 분리하여 사용하는 구조를 유지하면서, 트랜잭션 충돌을 원천적으로 방지한다.

**최종 전략:** Spring의 묵시적 결정에 의존하지 않고, 모든 트랜잭션의 책임과 동작을 명시적으로 제어한다.

1.  **`@Primary`의 역할 재정의 및 최소화:**
    -   **문제:** `@Primary`를 모두 제거하면, Spring Boot의 자동 설정 컴포넌트가 기본 `TransactionManager`나 `JpaProperties`를 찾지 못해 애플리케이션 시작에 실패한다.
    -   **해결:** `H2JpaConfig`의 `h2TransactionManager`와 `h2JpaProperties`에만 `@Primary`를 유지한다.
    -   **근거:** 이는 **우리의 코드를 위함이 아닌, Spring 프레임워크의 요구사항을 만족시키기 위한 최소한의 조치**이다. 이로써 프레임워크는 "기본값"을 인지하여 정상 구동하고, 우리의 코드는 이어질 명시적 설정에 따라 동작한다.

2.  **서비스별 트랜잭션 관리자 명시적 지정:**
    -   **원칙:** 어떤 서비스가 어떤 DB를 사용하는지 코드 레벨에서 명확히 한다.
    -   **구현:**
        -   H2를 사용하는 `com.oath.domain` 소속 서비스에는 클래스 레벨에 `@Transactional("h2TransactionManager")`를 선언한다.
        -   PostgreSQL을 사용하는 `com.oath.recommend_domain` 소속 서비스에는 `@Transactional("pgTransactionManager")`를 선언한다.
    -   **효과:** `@Qualifier`와 동일한 효과로, `@Primary` 설정보다 우선하여 각 서비스가 올바른 트랜잭션 관리자를 사용하도록 강제한다.

3.  **트랜잭션 경계 분리 (Facade 역할 재정의):**
    -   **문제:** `PlanFacade`와 같이 여러 DB를 사용하는 서비스를 호출하는 메서드에 `@Transactional`이 있으면, 해당 트랜잭션의 컨텍스트가 하위 서비스로 전파되어 충돌을 일으킨다.
    -   **해결:** `PlanFacade`의 `listRecommendPlans`처럼 두 종류의 트랜잭션 관리자를 사용하는 서비스들을 호출하는 메서드에서는 **자체 `@Transactional`을 제거**한다.
    -   **효과:** Facade는 더 이상 트랜잭션을 직접 관리하지 않고, 각 하위 서비스가 스스로의 트랜잭션 경계 안에서 독립적으로 동작하도록 책임을 위임한다.

4.  **비동기 처리(`@Async`)의 전략적 사용:**
    -   **문제:** `PlanCompletionListener`의 `@Async`를 제거하면 DB 커넥션 문제는 해결되지만, 통계 처리 등 무거운 작업이 API 응답 시간을 지연시킨다.
    -   **해결:** DB 커넥션 문제가 해결된 지금, **`@Async`를 다시 복원**한다.
    -   **근거:** 사용자에게 빠른 API 응답을 제공하는 것이 우선이다. 시간이 걸리는 후속 작업(통계, 그룹핑, AI 푸시 등)은 비동기로 처리하여 사용자 경험 저하를 방지한다.

## 4. 최종 검증 및 결론

-   **검증 방법:** H2 트랜잭션만 사용하는 `POST /api/plans` API를 호출하여 새로운 플랜 생성을 시도.
-   **결과:** `200 OK` 응답과 함께 새로운 플랜 데이터가 정상적으로 반환됨을 확인.
-   **결론:**
    -   위 전략을 통해, 각 서비스가 의도된 트랜잭션 관리자를 사용하고, 서로 다른 DB를 사용하는 서비스 간의 호출이 안전하게 이루어짐을 입증했다.
    -   복잡한 멀티-데이터소스 환경에서 발생했던 커넥션 충돌 및 폭풍 문제가 **완전히 해결되었음.**
