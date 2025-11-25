package com.oath.common.auth;

import com.oath.common.exception.Exception401;
import com.oath.common.exception.Exception403;
import com.oath.common.JwtTokenProvider;
import com.oath.domain.members.domain.Role;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        log.debug("AuthInterceptor preHandle called for URI: {}", request.getRequestURI()); // 디버그 로그 추가

        // 1. 요청 핸들러가 HandlerMethod가 아니면 통과 (e.g., 정적 리소스 요청)
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. @Auth 어노테이션이 없으면 통과
        Auth auth = handlerMethod.getMethodAnnotation(Auth.class);
        if (auth == null) {
            log.debug("No @Auth annotation found for {}. Skipping authentication.", handlerMethod.getMethod().getName());
            return true;
        }
        log.debug("@Auth annotation found for {}. Proceeding with authentication.", handlerMethod.getMethod().getName());


        // 3. 헤더에서 토큰 추출 및 검증 (early return)
        String token = resolveToken(request);
        log.debug("Resolved token: {}", token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"); // 토큰 앞부분만 로깅
        
        boolean isValidToken = jwtTokenProvider.validateToken(token);
        log.debug("Token validation result: {}", isValidToken);

        if (token == null || !isValidToken) {
            log.warn("Authentication failed for URI: {}. Token is null or invalid.", request.getRequestURI());
            throw new Exception401("인증되지 않은 사용자입니다.");
        }

        // 4. 역할(Role) 검사 (인가)
        Role[] requiredRoles = auth.roles();
        if (requiredRoles.length > 0) {
            boolean hasPermission = hasPermission(token, requiredRoles);
            log.debug("Role check for token. Required roles: {}, User has permission: {}", Arrays.toString(requiredRoles), hasPermission);
            if (!hasPermission) {
                log.warn("Authorization failed for URI: {}. User does not have required roles: {}", request.getRequestURI(), Arrays.toString(requiredRoles));
                throw new Exception403("해당 페이지에 접근할 권한이 없습니다.");
            }
        }

        // 5. (선택) 컨트롤러에서 사용자 정보를 사용할 수 있도록 request에 저장
        String email = jwtTokenProvider.getSubject(token);
        Long memberId = jwtTokenProvider.getMemberId(token);

        request.setAttribute(
                "userEmail",
                email
        );
        request.setAttribute(
                "memberId",
                memberId
        );
        log.debug("Authentication successful for URI: {}. User email: {}, memberId: {}", request.getRequestURI(), email, memberId);

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        log.debug("Authorization header: {}", bearerToken); // Authorization 헤더 전체 로깅
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean hasPermission(String token, Role[] requiredRoles) {
        Role userRole = jwtTokenProvider.getRole(token);
        log.debug("User role from token: {}", userRole);
        return Arrays.stream(requiredRoles).anyMatch(requiredRole -> requiredRole == userRole);
    }
}
