package com.oath.common.auth;


import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception403;
import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import org.springframework.lang.NonNull;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.oath.common.JwtTokenProvider;
import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception403;
import com.oath.domain.members.domain.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        // 1. 요청 핸들러가 HandlerMethod가 아니면 통과 (e.g., 정적 리소스 요청)
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. @Auth 어노테이션이 없으면 통과
        Auth auth = handlerMethod.getMethodAnnotation(Auth.class);
        if (auth == null) {
            return true;
        }

        // 3. 헤더에서 토큰 추출 및 검증 (early return)
        String token = resolveToken(request);
        log.warn("HEADER AUTH => {}", request.getHeader("Authorization"));
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }

        // 4. 역할(Role) 검사 (인가)
        Role[] requiredRoles = auth.roles();
        if (requiredRoles.length > 0 && !hasPermission(token, requiredRoles)) {
            throw new Exception403("해당 페이지에 접근할 권한이 없습니다.");
        }

        // 5. (선택) 컨트롤러에서 사용자 정보를 사용할 수 있도록 request에 저장
        String email = jwtTokenProvider.getSubject(token);
        Long memberId = jwtTokenProvider.getMemberId(token);

        request.setAttribute(
                "userEmail",
                email);
        request.setAttribute(
                "memberId",
                memberId);

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        if (request.getCookies() != null) {
            for(Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean hasPermission(String token, Role[] requiredRoles) {
        Role userRole = jwtTokenProvider.getRole(token);
        return Arrays.stream(requiredRoles).anyMatch(requiredRole -> requiredRole == userRole);
    }
}
