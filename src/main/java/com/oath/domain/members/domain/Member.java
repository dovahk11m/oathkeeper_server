package com.oath.domain.members.domain;

import com.oath.domain.members.OathkeeperRank;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members_tb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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

    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    private String socialId;

    private String defaultAddress;

    @Enumerated(EnumType.STRING)
    private OathkeeperRank oathkeeperRank;

    private Integer totalLateMinutes;

    private Integer totalEarlyMinutes;

    private boolean isPremium;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    private LocalDateTime bannedUntil;


    public void updateInfo(String email) {
        this.email = email;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.status = Status.DEACTIVATED;
    }

    public void inactive() {
        this.status = Status.INACTIVE;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void suspend() {
        this.status = Status.SUSPENDED;
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }
}
