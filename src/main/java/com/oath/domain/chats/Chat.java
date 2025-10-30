package com.oath.domain.chats;

import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_tb")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member sender;

    @Column(nullable = false, length = 1000)
    private String content;

    private Long planId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    /**
     * 새로운 Chat 엔티티를 생성하는 정적 팩토리 메서드입니다.
     */
    public static Chat of(Group group, Member sender, String content, Long planId) {
        return Chat.builder()
                .group(group)
                .sender(sender)
                .content(content)
                .planId(planId)
                .sentAt(LocalDateTime.now())
                .build();
    }
}