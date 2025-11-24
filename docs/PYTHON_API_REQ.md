# Python AI 서버 신규 API 요청 명세서

- **요청일:** 2025-11-24
- **작성자:** Oath Spring 서버 개발팀
- **목적:** 그룹(채팅방) 단위의 누적 통계 및 요약 기능을 구현하기 위해 Python AI 서버에 필요한 신규 API를 요청합니다.

---

## 1. 그룹 누적 데이터 요약 API (신규)

### 1.1. 목표
- 특정 그룹에 속한 여러 약속들의 데이터를 종합하여, "이 그룹은 평균적으로 Okm를 이동하고, 지각을 O% 합니다"와 같은 **그룹 전체의 누적 통계**를 제공하는 API가 필요합니다.

### 1.2. 제안 API
- **Endpoint**: `POST /metrics/group/summary`
- **Description**: 여러 `plan_id`에 대한 `metrics` 데이터를 종합하여 그룹 전체의 누적 통계를 계산하고 반환합니다.

### 1.3. 요청 형식 (Spring → Python)
- Request Body에 분석할 `plan_id` 목록을 JSON 배열로 전달합니다.
```json
{
  "plan_ids": [1, 5, 12, 23]
}
```

### 1.4. 필요 로직 (Python 측)
1.  Request Body로부터 `plan_ids` 목록을 받습니다.
2.  각 `plan_id`에 해당하는 `data/plan_{id}/metrics.jsonl` 파일의 모든 기록을 읽어들입니다.
3.  모든 `plan`의 모든 `metrics` 데이터를 합산하여, 그룹 전체의 누적/평균 통계(총 약속 수, 총 이동 거리, 약속당 평균 이동 거리, 총 지각 시간, 약속당 평균 지각 시간 등)를 계산합니다.
4.  계산된 누적 통계 데이터를 아래와 같은 JSON 형식으로 응답합니다.

### 1.5. 예상 응답 (Python → Spring)
```json
{
  "success": true,
  "data": {
    "group_summary": {
      "total_plans_analyzed": 4,
      "total_records": 128,
      "total_distance_km": 258.4,
      "avg_distance_per_plan_km": 64.6,
      "total_late_minutes": 45,
      "avg_late_minutes_per_plan": 11.25
    }
  }
}
```

---

## 2. 그룹 누적 데이터 자연어 요약 API (신규)

### 2.1. 목표
- 위 1번에서 계산된 **그룹 누적 통계 데이터**를 기반으로, 사람이 읽기 좋은 자연스러운 한글 요약 텍스트를 생성하는 기능이 필요합니다.

### 2.2. 제안 API
- **Endpoint**: `POST /metrics/group/summary/text`
- **Description**: 여러 `plan_id`에 대한 누적 통계를 바탕으로, LLM을 사용하여 그룹의 전반적인 활동(이동 패턴, 지각 경향 등)을 요약하는 자연어 텍스트를 생성합니다.

### 2.3. 요청 형식 (Spring → Python)
- Request Body에 분석할 `plan_id` 목록과 LLM 옵션(style, notes 등)을 함께 전달합니다.
```json
{
  "plan_ids": [1, 5, 12, 23],
  "style": "데이터 분석가처럼 객관적인 톤으로",
  "notes": "지각 빈도가 높은 경향이 있는지 분석해주세요."
}
```

### 2.4. 필요 로직 (Python 측)
1.  `plan_ids` 목록을 사용하여 위 1.4와 동일한 누적 통계 데이터를 계산합니다.
2.  계산된 누적 통계 데이터와 `style`, `notes` 등의 옵션을 조합하여 LLM에 전달할 프롬프트를 생성합니다.
3.  LLM으로부터 받은 자연어 텍스트를 정제하여 응답합니다.

### 2.5. 예상 응답 (Python → Spring)
```json
{
  "success": true,
  "data": "분석된 4개의 약속에 따르면, 이 그룹은 약속당 평균 64.6km를 이동했으며, 평균 11.25분의 지각 시간을 기록했습니다. 전반적으로 장거리 이동이 잦고, 약속 시간을 준수하는 데 약간의 어려움이 있는 경향을 보입니다."
}
```

---

## 3. 오류 처리 및 엣지 케이스 (Error Handling & Edge Cases)

### 3.1. 요청 `plan_ids` 중 일부에 문제가 있는 경우
- **정책**: API는 `404 Not Found`와 같은 에러를 반환하는 대신, **`200 OK`를 유지**하면서 응답 본문에 `warnings` 필드를 추가하여 해당 상황을 알려주는 것을 제안합니다.
- **사유**: 일부 데이터가 없더라도, 분석 가능한 데이터만으로 **부분적인 성공** 결과를 받을 수 있어 서비스 안정성이 높아집니다.
- **예상 응답 (일부 ID 누락 또는 데이터 없는 경우)**:
```json
{
  "success": true,
  "data": {
    "group_summary": {
      "total_plans_analyzed": 3,
      "total_distance_km": 210.1
    }
  },
  "warnings": [
    "plan_id '15' was not found.",
    "plan_id '23' has no metrics data."
  ]
}
```

### 3.2. 모든 `plan_ids`에 문제가 있는 경우
- **정책**: 분석할 데이터가 전혀 없는 경우에는 `409 Conflict` 에러와 함께 명확한 에러 메시지를 반환하는 것을 제안합니다.
- **예상 응답**:
```json
{
  "success": false,
  "data": null,
  "message": "No data available for the given plan_ids."
}
```

---

위 명세에 대한 검토 및 구현 가능 여부에 대한 회신 부탁드립니다. 감사합니다.
