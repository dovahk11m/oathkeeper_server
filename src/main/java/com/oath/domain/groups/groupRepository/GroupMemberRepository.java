package com.oath.domain.groups.groupRepository;

import com.oath.domain.groups.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 특정 그룹에 속한 멤버의 수를 계산합니다.
    long countByGroupId(Long groupId);

    // 특정 그룹의 특정 멤버 정보를 조회합니다.
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    // 특정 멤버가 속한 그룹 목록을 페이징하여 조회합니다. (N+1 방지)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.member.id = :memberId")
    Page<GroupMember> findByMemberIdWithGroup(@Param("memberId") Long memberId, Pageable pageable);
}