package com.oath.domain.members;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private String profileImageUrl;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerKey;

    private String defaultAddress;

    @Enumerated(EnumType.STRING)
    private OathkeeperRank oathkeeperRank;

    private Integer totalLateMinutes;

    private Integer totalEarlyMinutes;

    private boolean isPremium;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
