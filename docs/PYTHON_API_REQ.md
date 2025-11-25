# Python AI 서버 API 최종 명세서 (v2)

## 📢 Python AI 서버 개발팀에 대한 요청사항

안녕하세요, Python AI 서버 개발팀!

Spring 서버에서 그룹 요약 기능 구현이 완료됨에 따라, Python AI 서버 API 명세서(v2)를 최종 업데이트했습니다.
이 문서에 명시된 API 스펙에 맞춰 Python AI 서버가 정상적으로 동작하는지 확인 부탁드립니다.

특히, `java.net.http.HttpClient`를 사용하여 통신하도록 변경되었으며,
타임아웃 설정(연결 5초, 요청 35초)이 적용되었습니다.

감사합니다.

- **작성일:** 2025-11-25 (업데이트)
- **작성자:** Oath Spring 서버 개발팀 / Python AI 서버 개발팀
- **목적:** 그룹(채팅방) 단위의 누적 통계 및 요약 기능을 위한 Python AI 서버 API의 최종 명세입니다.

---

## 1. 그룹 통합 요약 API (v2)

### 1.1. 목표
- 특정 그룹에 속한 여러 약속들의 데이터를 종합하여, 그룹 전체의 누적 통계와 함께 사람이 읽기 좋은 자연스러운 한글 요약 텍스트를 **하나의 API 호출로** 제공합니다.

### 1.2. API 엔드포인트
- **Endpoint**: `POST /metrics/group/summary`
- **Description**: 여러 `plan_id`에 대한 `metrics` 데이터를 종합하여 그룹 전체의 누적 통계를 계산하고, LLM을 사용하여 그룹의 전반적인 활동을 요약하는 자연어 텍스트를 생성하여 반환합니다.

### 1.3. 요청 형식 (Spring → Python)
- Request Body에 분석할 `plan_id` 목록과 LLM 옵션(style, notes, mode 등)을 JSON 형식으로 전달합니다.

**Content-Type:** `application/json`

```json
{
  "plan_ids": [1, 5, 12, 23],
  "style": "데이터 분석가처럼 객관적인 톤으로",
  "notes": "지각 빈도가 높은 경향이 있는지 분석해주세요.",
  "mode": "llm"
}
```

#### 필드 설명

| 필드       | 타입             | 필수    | 기본값  | 설명                                |
| ---------- | ---------------- | ------- | ------- | ----------------------------------- |
| `plan_ids` | `array<integer>` | ✅ 필수 | -       | 분석할 plan_id 목록 (최소 1개)      |
| `style`    | `string`         | 선택    | `""`    | 텍스트 스타일 (예: "친근한 톤으로") |
| `notes`    | `string`         | 선택    | `""`    | 추가 요청사항                       |
| `mode`     | `string`         | 선택    | `"llm"` | 생성 모드: `"rules"` or `"llm"`     |

### 1.4. 필요 로직 (Python 측)
1.  Request Body로부터 `plan_ids` 목록과 `style`, `notes`, `mode` 옵션을 받습니다.
2.  각 `plan_id`에 해당하는 `data/plan_{id}/metrics.jsonl` 파일의 모든 기록을 읽어들입니다.
3.  모든 `plan`의 모든 `metrics` 데이터를 합산하여, 그룹 전체의 누적/평균 통계(총 약속 수, 총 이동 거리, 약속당 평균 이동 거리, 총 지각 시간, 약속당 평균 지각 시간 등)를 계산합니다.
4.  계산된 누적 통계 데이터와 `style`, `notes`, `mode` 옵션을 조합하여 LLM에 전달할 프롬프트를 생성합니다. (`mode`가 `"rules"`인 경우 LLM 호출 없이 규칙 기반 요약 생성)
5.  LLM으로부터 받은 자연어 텍스트를 정제하여 누적 통계 데이터와 함께 아래와 같은 JSON 형식으로 응답합니다.

### 1.5. 예상 응답 (Python → Spring)

#### 정상 응답 (200 OK)

```json
{
  "success": true,
  "data": {
    "group_summary": {
      "total_plans_analyzed": 4,
      "total_records": 128,
      "total_distance_km": 258.4,
      "avg_distance_per_plan_km": 64.6,
      "total_travel_minutes": 450,
      "avg_travel_minutes_per_plan": 112.5,
      "total_late_minutes": 45,
      "avg_late_minutes_per_plan": 11.25,
      "total_wait_minutes": 20,
      "avg_wait_minutes_per_plan": 5.0
    },
    "text_summary": "분석된 4개의 약속에 따르면, 이 그룹은 약속당 평균 64.6km를 이동했으며, 평균 11.25분의 지각 시간을 기록했습니다. 전반적으로 장거리 이동이 잦고, 약속 시간을 준수하는 데 약간의 어려움이 있는 경향을 보입니다."
  },
  "warnings": null
}
```

#### 부분 성공 응답 (200 OK + warnings)

- **정책**: API는 `404 Not Found`와 같은 에러를 반환하는 대신, **`200 OK`를 유지**하면서 응답 본문에 `warnings` 필드를 추가하여 해당 상황을 알려줍니다.
- **사유**: 일부 데이터가 없더라도, 분석 가능한 데이터만으로 **부분적인 성공** 결과를 받을 수 있어 서비스 안정성이 높아집니다.
- **예상 응답 (일부 ID 누락 또는 데이터 없는 경우)**:
```json
{
  "success": true,
  "data": {
    "group_summary": {
      "total_plans_analyzed": 3,
      "total_records": 96,
      "total_distance_km": 210.1,
      "avg_distance_per_plan_km": 70.0,
      "total_travel_minutes": 350,
      "avg_travel_minutes_per_plan": 116.6,
      "total_late_minutes": 30,
      "avg_late_minutes_per_plan": 10.0,
      "total_wait_minutes": 15,
      "avg_wait_minutes_per_plan": 5.0
    },
    "text_summary": "분석된 3개의 약속에 따르면..."
  },
  "warnings": [
    "plan_id '15' was not found.",
    "plan_id '23' has no metrics data."
  ]
}
```

#### 전체 실패 응답 (409 Conflict)

- **정책**: 분석할 데이터가 전혀 없는 경우에는 `409 Conflict` 에러와 함께 명확한 에러 메시지를 반환합니다.
- **예상 응답**:
```json
{
  "success": false,
  "data": null,
  "message": "No data available for the given plan_ids."
}
```

---

## 2. 응답 시간 고려사항

| 모드    | 예상 응답 시간 | 권장 사용처                    |
| ------- | -------------- | ------------------------------ |
| `rules` | 1~2초          | 실시간 조회, 빠른 응답 필요 시 |
| `llm`   | 5~30초         | 상세 분석, 비동기 처리         |

---

## 3. 기타 참고사항

- `style`과 `notes` 필드는 선택 사항입니다. 기본값(`""`)으로 동작하며, `mode`가 `"rules"`인 경우 무시됩니다.
- `warnings`가 있는 경우에도 `success: true`와 `200 OK`가 반환되므로, 클라이언트에서는 `warnings` 필드의 존재 여부를 확인하여 적절히 처리해야 합니다.

---
