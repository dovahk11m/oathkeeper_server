package com.oath.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 특정 채팅방의 메시지들을 최신순으로 페이징하여 조회합니다.
    Page<Chat> findByGroupIdOrderBySentAtDesc(Long groupId, Pageable pageable);

    // 특정 채팅방의 마지막 메시지 1건을 조회합니다.
    Optional<Chat> findTopByGroupIdOrderBySentAtDesc(Long groupId);
}