package com.oath.common.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String secreyKey;
    private final int expiration;
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secret}") String secreyKey, @Value("${jwt.expiration}") int expiration) {
        this.secreyKey = secreyKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secreyKey), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role) {

        Date now = new Date();
        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+ expiration*60*1000L))
                .signWith(SECRET_KEY)
                .compact();
        return token;
    }
}
