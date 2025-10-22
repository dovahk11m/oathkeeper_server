package com.oath.domain.chat;

import com.oath.domain.members.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages_tb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member sender; // 메시지를 보낸 사람

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 이 메시지가 특정 플랜과 관련이 있다면 해당 플랜의 ID를 저장합니다.
    // TODO: Plan 엔티티 생성 후 @ManyToOne(fetch = FetchType.LAZY)
    private Long planId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
}
