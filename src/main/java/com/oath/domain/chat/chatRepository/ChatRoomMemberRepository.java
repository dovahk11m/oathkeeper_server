package com.oath.domain.chat.chatRepository;

import com.oath.domain.chat.ChatRoomMember;
import org.checkerframework.checker.units.qual.N;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    //특정 멤버가 참여하고 있는 채팅방 목록을 페이징하여 조회합니다.(fetch join)
    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.chatRoom WHERE crm.member.id = :memberId")
    Page<ChatRoomMember> findByMemberIdWithChatRoom(@Param("memberId") Long memberId, Pageable pageable);

    //특정 채팅방에 참여하고 있는 멤버 목록을 페이징하여 조회합니다.(fetch join)
    @Query(value = "SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.member WHERE crm.chatRoom.id = :roomId",
           countQuery = "SELECT count(crm) FROM ChatRoomMember crm WHERE crm.chatRoom.id = :roomId")
    Page<ChatRoomMember> findByChatRoomId(@Param("roomId") Long roomId, Pageable pageable);
}