package com.oath.initializer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.oath.initializer.scenario.PlanScenario01_Completed;
import com.oath.initializer.scenario.PlanScenario02_Ongoing;
import com.oath.initializer.scenario.PlanScenario03_Pending;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>Plan 관련 데이터 초기화 오케스트레이터</h2>
 * <p>
 * 이 클래스는 애플리케이션 시작 시(local 프로필) 약속(Plan) 관련 샘플 데이터를 생성하는 역할을 총괄합니다. 실제 데이터 생성 로직은 각각의
 * '시나리오(Scenario)' 클래스에 위임하고, 여기서는 어떤 시나리오를 어떤 순서로 실행할지만 결정합니다.
 * </p>
 *
 * <h3>새로운 Plan 시나리오를 추가하는 방법:</h3>
 * <ol>
 * <li>`com.oath.initializer.scenario` 패키지 내에 새로운 시나리오 클래스를 생성합니다. (예:
 * `PlanScenario04_Cancelled.java`)</li>
 * <li>새로운 클래스에 `@Component` 어노테이션을 붙이고, 데이터 생성 로직을 담은 `create()` 메서드를 구현합니다.</li>
 * <li>이 클래스(`DataInitializer12_Plan`)에 새로운 시나리오 클래스를 주입(`private final ...`)받습니다.</li>
 * <li>`initialize()` 메서드 내에서 원하는 순서에 맞게 새로운 시나리오의 `create()` 메서드를 호출합니다.</li>
 * </ol>
 *
 * @see com.oath.initializer.scenario.PlanScenario01_Completed
 * @see com.oath.initializer.scenario.PlanScenario02_Ongoing
 * @see com.oath.initializer.scenario.PlanScenario03_Pending
 */
@Slf4j
@Component
@RequiredArgsConstructor
// @Transactional 제거 - 규칙 1: 트랜잭션 매니저 명시적 지정
@Profile("local")
// @Order 제거 - MasterDataInitializer에서 순서 통제
public class DataInitializer12_Plan {

    private final PlanScenario01_Completed planScenario01;
    private final PlanScenario02_Ongoing planScenario02;
    private final PlanScenario03_Pending planScenario03;

    @Transactional("h2TransactionManager")
    public void initialize(String... args) {
        log.info("=============== Plan 시나리오 데이터 초기화 시작 ===============");
        planScenario01.create();
        planScenario02.create();
        planScenario03.create();
        log.info("=============== Plan 시나리오 데이터 초기화 완료 ===============");

    }
}
