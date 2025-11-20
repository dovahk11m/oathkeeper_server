# 새로운 이벤트 리스너 작성 가이드

이 문서는 우리 프로젝트에서 새로운 비동기 이벤트 리스너를 작성하는 방법을 안내합니다.
`CreateGroupListener`를 예시로, 새로운 기능(예: "새로운 멤버가 그룹에 추가되었을 때 알림 보내기")을 구현하는 과정을 따라가 봅니다.

---

### 1단계: 이벤트(Event) 정의

먼저, 전달할 데이터를 담을 이벤트 클래스를 만듭니다. 이벤트는 특정 도메인(예: `groups`)의 `event` 패키지 안에 생성합니다.

- **규칙**: 이벤트 객체는 `final` 필드를 가진 불변(Immutable) 객체로 설계합니다.

**예시: `NewMemberAddedEvent.java`**
```java
package com.oath.domain.groups.groupEvent;

import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NewMemberAddedEvent {
    private final Group group;
    private final Member newMember; // 새로 추가된 멤버
    private final Member inviter;   // 초대한 사람
}
```

---

### 2단계: 서비스에서 이벤트 발행(Publish)

핵심 비즈니스 로직이 성공적으로 완료된 후, `ApplicationEventPublisher`를 사용하여 이벤트를 발행합니다.

- **규칙**: `@Transactional` 메서드 안에서, 모든 DB 작업이 끝난 후 마지막에 이벤트를 발행합니다.

**예시: `GroupService.addMembers()`**
```java
@Transactional
public void addMembers() {
    // ... 멤버 추가 로직 ...
    groupMemberRepository.saveAll(newGroupMembers);

    // 새로 추가된 멤버들에게 알림 이벤트를 발행
    newGroupMembers.forEach(groupMember -> {
        eventPublisher.publishEvent(new NewMemberAddedEvent(
            group,
            groupMember.getMember(),
            requester
        ));
    });
}
```

---

### 3단계: 리스너(Listener) 작성

`alarms/listeners` 패키지 안에, 발행된 이벤트를 수신하여 실제 알림 로직을 처리할 리스너 클래스를 작성합니다.

- **규칙**: 아래 템플릿의 모든 어노테이션과 구조를 그대로 따르는 것을 권장합니다.

**예시: `NewMemberAddedListener.java` (템플릿)**
```java
package com.oath.domain.alarms.listeners;

import com.oath.domain.alarms.AlarmDTO;
import com.oath.domain.alarms.AlarmFactory;
import com.oath.domain.alarms.AlarmSender;
import com.oath.domain.groups.groupEvent.NewMemberAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewMemberAddedListener {

    private final AlarmFactory alarmFactory;

    // application.yml에서 'on-new-member' 설정을 찾고, 없으면 'default' 설정을 사용
    @Value("${notification.policy.on-new-member:${notification.policy.default}}")
    private String notificationType;

    @Async // 1. 비동기 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 2. 새로운 트랜잭션 시작
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 3. 이전 트랜잭션 커밋 후 실행
    public void handleNewMemberAddedEvent(NewMemberAddedEvent event) {
        log.info("[신규 멤버 추가 이벤트 수신: 그룹명='{}', 신규멤버='{}']",
                event.getGroup().getName(),
                event.getNewMember().getUsername());

        // 1. 알림 내용 생성
        String subject = String.format("'%s' 그룹에 초대되었습니다.", event.getGroup().getName());
        String content = String.format("'%s'님이 당신을 '%s' 그룹에 초대했습니다.",
                event.getInviter().getUsername(),
                event.getGroup().getName());

        // 2. 알림 DTO 생성
        AlarmDTO request = new AlarmDTO(event.getNewMember().getEmail(), subject, content);

        // 3. 팩토리를 통해 적절한 Sender를 찾아 알림 발송
        AlarmSender sender = alarmFactory.findSender(notificationType);
        sender.send(request);
    }
}
```

---

### 4단계: `application.yml` 설정 (선택 사항)

만약 이 이벤트에 대해 기본 알림 방식(`default`)과 다른 방식을 사용하고 싶다면, `application.yml`에 설정을 추가합니다.

```yaml
notification:
  policy:
    default: LOCAL
    on-new-member: EMAIL # 신규 멤버 추가 시에는 이메일만 보내기
```