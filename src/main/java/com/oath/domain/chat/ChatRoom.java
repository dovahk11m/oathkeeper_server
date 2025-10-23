package com.oath.domain.chat;

import com.oath.domain.groups.Group;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms_tb")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group; // 이 채팅방이 속한 그룹

    @Column(nullable = false)
    private String name; // 채팅방 이름 (e.g., "그룹 A 전체 채팅방")

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
