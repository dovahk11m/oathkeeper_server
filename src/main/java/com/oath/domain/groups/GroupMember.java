package com.oath.domain.groups;

import com.oath.domain.members.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "group_member_tb")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    /**
     * 새로운 GroupMember 엔티티를 생성하는 정적 팩토리 메서드입니다.
     */
    public static GroupMember of(Group group, Member member) {
        return GroupMember.builder()
                .group(group)
                .member(member)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
