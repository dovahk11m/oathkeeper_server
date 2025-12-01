package com.oath.domain.chats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 특정 그룹의 메시지들을 최신순으로 페이징하여 조회합니다.
    Page<Chat> findByGroupIdOrderBySentAtDesc(Long groupId, Pageable pageable);

    // 특정 그룹의 마지막 메시지 1건을 조회합니다.
    Optional<Chat> findTopByGroupIdOrderBySentAtDesc(Long groupId);

    // 테스트용
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.group LEFT JOIN FETCH c.sender")
    List<Chat> findAllWithGroupAndSender(Sort sort);

    List<Chat> findByGroupIdOrderBySentAt(Long groupId);



    @Query("SELECT c FROM Chat c JOIN FETCH c.sender WHERE c.group.id = :groupId ORDER BY c.sentAt DESC")
    List<Chat> findByGroupIdWithMember(Long groupId);

    long count();


}