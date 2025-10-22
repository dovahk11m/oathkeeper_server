package com.oath.domain.chat.chatRepository;

import com.oath.domain.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 메시지들을 최신순으로 페이징하여 조회(과거 메시지 불러오기, 스크롤 올리기)
    Page<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Pageable pageable);
}