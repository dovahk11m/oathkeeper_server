package com.oath.domain.chat.chatRepository;

import com.oath.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 그룹 ID로 채팅방 조회
    Optional<ChatRoom> findByGroupId(Long groupId);
}