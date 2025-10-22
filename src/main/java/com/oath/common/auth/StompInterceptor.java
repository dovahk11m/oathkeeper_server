package com.oath.common.auth;

import com.oath.domain.members.MemberRole;
import com.oath.common.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * WebSocket 연결 시 STOMP 헤더의 JWT 토큰을 검증하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP 연결 요청일 때만 토큰 검증
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

            if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
                validateAndSetAuthentication(authHeader, accessor);
            } else {
                log.warn("STOMP connection failed: Missing or invalid Authorization header.");
                throw new MessageDeliveryException("인증 헤더가 필요합니다.");
            }
        }
        return message;
    }

    private void validateAndSetAuthentication(String authHeader, StompHeaderAccessor accessor) {
        String token = authHeader.substring(BEARER_PREFIX.length());

        if (jwtProvider.validateToken(token)) {
            String email = jwtProvider.getSubject(token);
            MemberRole role = jwtProvider.getRole(token);

            StompPrincipal principal = new StompPrincipal(email, role);
            accessor.setUser(principal);
            log.info("STOMP connection authenticated for user: {}", email);
        } else {
            log.warn("STOMP connection failed: Invalid JWT token.");
            throw new MessageDeliveryException("유효하지 않은 토큰");
        }
    }
}