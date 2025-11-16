package com.oath.domain.chatEntity;

import com.oath.domain.chats.Chat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_entity_tb")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="chat_message_id")
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private EntityType type; // EMAIL, PHONE, NAME, ADDRESS

    private int startIndex; // text에서 시작 위치
    private int endIndex;   // text에서 끝 위치
    private String value;   // 실제 값
}
