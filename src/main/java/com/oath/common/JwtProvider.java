package com.oath.common;

import com.oath.domain.members.MemberRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final Long validityInMilliseconds;

    public JwtProvider(

            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-in-ms}") Long validityInMilliseconds
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    //로그인시 새 토큰 생성
    public String createToken(String email, MemberRole role) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email) // 'sub' 클레임
                .claim("role", role.name()) // 비공개 클레임
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error(
                    "잘못된 JWT 서명입니다",
                    e
            );
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다: {}", e.getMessage());
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

    //토큰에서 MemberRole 추출
    public MemberRole getRole(String token){
        String role = parseClaims(token).get("role", String.class);
        return MemberRole.valueOf(role);
    }
}
