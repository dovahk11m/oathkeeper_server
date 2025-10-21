package com.oath.domain.groups;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId; // FK groups.id

    @Column(nullable = false)
    private Long memberId; // FK members.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;
}
