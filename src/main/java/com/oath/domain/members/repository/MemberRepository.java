package com.oath.domain.members.repository;

import com.oath.domain.members.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);

    List<Member> findByEmailIn(List<String> emails);

    Optional<Member> findByUsernameAndEmail(String username, String email);

    Optional<Member> findByEmailVerificationToken(String token);

    List<Member> findAll();
}
