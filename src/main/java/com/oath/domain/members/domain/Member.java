package com.oath.domain.members.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.oath.domain.members.OathkeeperRank;
import com.oath.domain.terms.MemberAgreedTerm;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "members_tb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "agreedTerms") // 순환 참조 방지
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


    private String defaultAddress; // 기본 주소
    private Double defaultLat; // 기본 위도
    private Double defaultLng; // 기본 경도

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

    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference // 순환 참조 부모(정상 직렬화)
    private List<MemberAgreedTerm> agreedTerms = new ArrayList<>();

    public void updateInfo(String username, String profileImageUrl, String defaultAddress) {
        if (username != null) this.username = username;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (defaultAddress != null) this.defaultAddress = defaultAddress;
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
        this.emailVerificationToken = null; // 인증 완료 후 토큰은 제거
        this.emailVerificationTokenExpiry = null;
    }

    public void suspend() {
        this.status = Status.SUSPENDED;
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    public void generateEmailVerificationToken() {
        this.emailVerificationToken = UUID.randomUUID().toString();
        this.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24); // 토큰 유효기간 24시간
    }
}
