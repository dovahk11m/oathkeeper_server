package com.oath.domain.plan.repository;

import com.oath.domain.plan.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByPlanId(Long planId);

    // 김성훈 10.24 _ 유저 아이디를 이용한 사용자 이름 조회 통신 
    @Query("""
           select p.member.id as id, p.member.username as name
           from Participant p
           where p.plan.id = :planId
           """)
    List<MemberIdName> findMemberIdNameByPlanId(@Param("planId") Long planId);

    interface MemberIdName {
        Long getId();
        String getName();
    }
}

