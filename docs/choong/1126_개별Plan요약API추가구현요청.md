# [요청] 개별 'Plan' 요약 API 추가 구현 요청

**수신:** Python AI 서버 개발팀
**발신:** Oath 서버 개발팀
**작성일:** 2025-11-26
**관련 문서:** `PYTHON(파이썬AI)_API.md` (v2)

안녕하세요, Python AI 서버 개발팀.

먼저, 여러 `plan_id`를 기반으로 상세한 그룹 리포트를 생성해주는 v2 통합 API(`POST /metrics/group/summary`)를 훌륭하게 구현해주셔서 감사합니다.

Java 서버 측에서 '약속 완료' 시나리오의 후처리 로직을 구현하던 중, v2 API만으로는 해결할 수 없는 필수 요구사항이 발견되어 연락드립니다.

#### **배경 및 문제 상황**

현재 '약속(Plan)'이 하나 완료되면, 서버는 두 가지 종류의 요약을 필요로 합니다.

1.  방금 막 완료된 **개별 'Plan'**에 대한 요약
2.  그 Plan이 속한 **'Group'** 전체에 대한 누적 요약

현재의 v2 API는 여러 `plan_id`를 받아 '그룹' 단위의 요약을 생성하는 데에는 완벽하지만, **단일 `plan_id`를 대상으로 하는 '개별 플랜' 요약을 생성하는 기능은 제공하지 않는 것**으로 파악됩니다.

이 기능이 과거 v1 API에는 존재했었거나, v2 통합 과정에서 개별 요약 기능이 누락된 것으로 추정됩니다.

#### **요청 사항**

**개별 `plan_id` 하나를 받아, 해당 Plan에 대한 요약 텍스트를 반환하는 API의 추가 또는 복구를 요청**드립니다.

저희가 제안하는 API 명세는 다음과 같습니다.

*   **Endpoint:** `POST /metrics/plan/{plan_id}/summary`
*   **Request Body:** 없음 (필요한 `plan_id`가 URL 경로에 포함)
*   **Success Response (200 OK):**
    ```json
    {
      "success": true,
      "data": {
        "plan_id": 2,
        "text_summary": "약속 #2의 요약입니다. 총 이동 거리는 23.63km, 총 이동 시간은 200분이었습니다..."
      }
    }
    ```
*   **Failure Response (404 Not Found 등):**
    ```json
    {
      "success": false,
      "message": "해당 plan_id에 대한 데이터를 찾을 수 없습니다."
    }
    ```

#### **사용 시나리오**

이 API는 Java 서버의 `PlanCompletionListener`에서 다음과 같은 순서로 호출될 예정입니다.

1.  `PlanCompletedEvent` 발생
2.  `rollupService`가 해당 `plan_id`의 통계(이동 거리 등)를 계산하여 `plan_tb`에 저장
3.  **(요청 API 호출)** `POST /metrics/plan/{plan_id}/summary`를 호출
4.  응답받은 `text_summary`를 `plan_tb`의 `summary` 필드에 저장
5.  이후, 그룹 통계 누적 및 그룹 요약 요청 등 다음 단계 진행

#### **결론**

'그룹 요약'과 '플랜 요약'은 생성 시점과 분석 대상이 명확히 다르므로, 두 기능을 모두 지원하는 API가 반드시 필요합니다.

바쁘시겠지만, 위 요청 사항에 대한 검토를 부탁드립니다. API 명세는 논의 후 얼마든지 조율 가능합니다.

협조에 미리 감사드립니다.
