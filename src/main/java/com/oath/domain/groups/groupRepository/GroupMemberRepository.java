package com.oath.domain.groups.groupRepository;

import com.oath.domain.chats.Chat;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.GroupMember;
import com.oath.domain.members.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 특정 그룹에 속한 멤버의 수를 계산합니다.
    long countByGroupId(Long groupId);

    // 특정 그룹의 특정 멤버 정보를 조회합니다.
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    // 특정 그룹에 특정 멤버가 존재하는지 확인합니다.
    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    // 특정 그룹에 속한 여러 멤버의 정보를 한 번에 조회합니다.
    List<GroupMember> findByGroupIdAndMemberIdIn(Long groupId, List<Long> memberIds);

    // 특정 멤버가 속한 그룹 목록을 페이징하여 조회합니다. (N+1 방지)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.member.id = :memberId")
    Page<GroupMember> findByMemberIdWithGroup(@Param("memberId") Long memberId, Pageable pageable);

    // 특정 그룹의 멤버 목록을 페이징하여 조회합니다. (N+1 방지)
    @Query(value = "SELECT gm FROM GroupMember gm JOIN FETCH gm.member WHERE gm.group.id = :groupId",
           countQuery = "SELECT count(gm) FROM GroupMember gm WHERE gm.group.id = :groupId")
    Page<GroupMember> findByGroupIdWithMember(@Param("groupId") Long groupId, Pageable pageable);

    @Query("SELECT gm.member FROM GroupMember gm WHERE gm.group.id = :groupId")
    List<Member> findMembersByGroup(@Param("groupId") Long groupId);

    @Query("SELECT c FROM Chat c JOIN FETCH c.sender WHERE c.group.id = :groupId AND c.sender.id = :senderId ORDER BY c.sentAt")
    List<Chat> findByGroupAndSenderWithFetch(@Param("groupId") Long groupId, @Param("senderId") Long senderId);

    boolean existsByGroupAndMember(Group group, Member member);

    @Query("""
    SELECT gm.member.email
    FROM GroupMember gm
    WHERE gm.group.id = :groupId
    """)
    List<String> findMemberEmailsByGroupId(@Param("groupId") Long groupId);


}