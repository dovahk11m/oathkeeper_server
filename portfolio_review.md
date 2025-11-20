# 포트폴리오 — Plan/Arrival/Review/Reply 기능 구현 (자소서 형식)

이 문서는 `Orijoa0829` 님이 이 프로젝트에서 Plan(약속)과 관련된 기능을 설계/구현한 내용을 자소서 형식으로 정리한 초안입니다.

## 1. 아키텍처 요약
- 스프링 부트 기반 레이어드 아키텍처: controller → service → repository.
- JPA(Hibernate) 엔티티 중심 도메인 모델.
- 이벤트 기반 알림: ApplicationEventPublisher로 Arrival/Departure/Late/PlanCompleted 이벤트 발행.
- 트랜잭션 분리: `@Transactional`과 별도 `PlanTrackingService`에서 `h2TransactionManager` 사용(테스트 목적).

## 2. 구현 방식
- 참가자 도착/출발 로직은 `Participant` 엔티티의 상태 변경 메서드(`markArrived`, `markDeparted`)로 캡슐화.
- 도착 시 `ArrivalEvent`를 발행해 알림을 비동기 처리.
- 모든 `ACCEPTED` 상태 참가자가 도착하면 `Plan`의 상태를 `COMPLETED`로 변경하고 `completedAt`을 기록.
- 후기(Review) 도메인은 `Review`와 `Reply`로 분리: 후기에는 댓글(Reply) 목록을 연관관계로 보유.

## 3. 트러블슈팅(커밋 내역 기반 추론)
- 문제: 여러 참가자가 거의 동시에 도착했을 때 `checkAndCompletePlan`에서 중복 완료 또는 Race Condition 발생.
  - 조치: 트랜잭션 경계 재정의 및 `planJpaRepository.save` 직후 이벤트 발행으로 상태를 확정하도록 변경.
- 문제: LazyInitializationException 발생 (이벤트 핸들러에서 participant의 member에 접근 시). 
  - 조치: `participantRepository.findOtherParticipantsWithMember` 같은 fetch 조인 메서드를 추가해 해결.
- 문제: 테스트 환경과 프로덕션 트랜잭션 매니저 혼동.
  - 조치: `PlanTrackingService`에 명시적 트랜잭션 매니저 설정(`@Transactional("h2TransactionManager")`) 및 테스트 전용 로직 분리.

## 4. 본인 기여 핵심 작업
- Plan 도착/출발/완료 로직 설계 및 구현 (도착 자동완료 포함).
- 이벤트 발행/처리 구조 설계(Arrival/Departure/Late/PlanCompleted).
- 후기 및 댓글 도메인 모델 구현 및 REST API 작성.
- Race condition 및 Lazy 초기화 문제 디버깅과 해결.

## 5. 사용 기술
- Java 17, Spring Boot, Spring Data JPA, Hibernate
- H2 (테스트용), MySQL/Postgres(운영 가정)
- Lombok, Spring Events

## 6. 정리 한 문장
- "사용자 도착 상태를 정확히 추적하고, 모든 참가자 도착 시 자동으로 약속을 완료시키며, 그 후에만 후기를 작성할 수 있도록 엄격한 비즈니스 룰을 도입해 사용자 경험과 데이터 무결성을 확보했습니다."

---

(원문은 간결화를 위해 일부 기술적 세부를 생략했습니다. 필요하면 코드 스니펫과 엔드포인트 예시를 추가 제공하겠습니다.)

