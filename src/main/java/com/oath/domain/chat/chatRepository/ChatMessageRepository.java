package com.oath.domain.chat.chatRepository;

import com.oath.domain.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 메시지들을 최신순으로 페이징하여 조회합니다.
    Page<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Pageable pageable);

    // 특정 채팅방의 마지막 메시지 1건을 조회합니다.
    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Long chatRoomId);
}