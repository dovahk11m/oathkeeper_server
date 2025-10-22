package com.oath.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms_tb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Group 엔티티 생성 후 @ManyToOne(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private Long groupId; // 이 채팅방이 속한 그룹의 ID

    @Column(nullable = false)
    private String name; // 채팅방 이름 (e.g., "그룹 A 전체 채팅방")

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
