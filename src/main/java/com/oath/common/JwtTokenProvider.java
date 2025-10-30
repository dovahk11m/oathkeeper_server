package com.oath.common;

import com.oath.domain.members.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Long validityInMilliseconds;
    private final CacheManager cacheManager;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-in-ms}") Long validityInMilliseconds,
            CacheManager cacheManager
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
        this.cacheManager = cacheManager;
    }

    //로그인시 새 토큰 생성
    public String createToken(
            String email,
            Role role,
            Long memberId
    ) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim(
                        "role",
                        role.name()
                )
                .claim(
                        "memberId",
                        memberId
                )
                .expiration(validity)
                .signWith(
                        key,
                        Jwts.SIG.HS256
                        // HS256 알고리즘
                )
                .compact();
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        if (isBlacklisted(token)) {
            log.warn("Blacklisted token: {}", token);
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error(
                    "잘못된 JWT 서명입니다",
                    e
            );
        } catch (ExpiredJwtException e) {
            log.info(
                    "만료된 JWT 토큰입니다: {}",
                    e.getMessage()
            );
        } catch (UnsupportedJwtException e) {
            log.error(
                    "지원되지 않는 JWT 토큰입니다",
                    e
            );
        } catch (Exception e) {
            log.error(
                    "JWT 토큰이 잘못됐습니다",
                    e
            );
        }
        return false;
    }

    private boolean isBlacklisted(String token) {
        return cacheManager.getCache("blacklistedTokens").get(token) != null;
    }

    //클레임 정보를 추출하는 기능
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //토큰에서 이메일 추출
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    //토큰에서 Role 추출
    public Role getRole(String token) {
        String role = parseClaims(token).get(
                "role",
                String.class
        );
        return Role.valueOf(role);
    }

    //토큰에서 memberId 추출
    public Long getMemberId(String token) {
        return parseClaims(token).get(
                "memberId",
                Long.class
        );
    }

    public long getRemainingExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
