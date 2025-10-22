package com.oath.domain.chat.chatRepository;

import com.oath.domain.chat.ChatRoomMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 채팅방 목록 페이징 조회(N+1 방지용 fetch join)
    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.chatRoom WHERE crm.member.id = :memberId")
    Page<ChatRoomMember> findByMemberIdWithChatRoom(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

}