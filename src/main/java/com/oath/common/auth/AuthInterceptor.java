package com.oath.common.auth;

import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception403;
import com.oath.common.token.JwtProvider;
import com.oath.domain.members.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        // 1. @Auth 어노테이션이 붙어 있는지 확인
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Auth auth = handlerMethod.getMethodAnnotation(Auth.class);
        if (auth == null) {
            return true;
        }

        // 2. 헤더에서 토큰 추출 및 검증
        String token = resolveToken(request);
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new Exception401("인증되지 않은 사용자입니다.");
        }

        // 3. 역할(Role) 검사 (인가)
        MemberRole[] requiredRoles = auth.roles();
        if (requiredRoles.length > 0) {
            MemberRole userRole = jwtProvider.getRole(token);
            boolean hasPermission = Arrays.stream(requiredRoles).anyMatch(requiredRole -> requiredRole == userRole);

            if (!hasPermission) {
                throw new Exception403("해당 페이지에 접근할 권한이 없습니다.");
            }
        }

        // 4. (선택) 컨트롤러에서 사용자 정보를 사용할 수 있도록 request에 저장
        String email = jwtProvider.getSubject(token);
        request.setAttribute(
                "userEmail",
                email
        );

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
