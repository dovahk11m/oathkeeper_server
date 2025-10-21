package com.oath.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId; // FK chat_rooms.id

    @Column(nullable = false)
    private Long memberId; // FK members.id

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
