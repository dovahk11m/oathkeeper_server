package com.oath.domain.visitors;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;

    private final MemberRepository memberRepository;

    public void saveVisitor (Long groupId, Long adminId) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow();

        Visitor visitor = Visitor.builder()
                .groupId(groupId)
                .admin(admin)
                .readAt(LocalDateTime.now())
                .build();

        visitorRepository.save(visitor);
    }

}
