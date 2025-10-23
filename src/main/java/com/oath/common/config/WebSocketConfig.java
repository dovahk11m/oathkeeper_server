package com.oath.common.config;

import com.oath.common.auth.StompInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/*
http 통신 -- 프로토콜 업그레이드 /ws-stomp (by클라이언트)

/topic/* 특정 방으로 발송되는 메시지만 받겠다
/app/* 특정 방으로 메시지를 보내겠다

 */

//메시지 브로커
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 연결 시 JWT 인증을 처리할 인터셉터
    private final StompInterceptor stompInterceptor;

    /*메시지 브로커 설정
    /topic 으로 시작하는 경로는 이 브로커가 처리한다
    브로커는 해당 경로를 구독하는 클라이언트에 메시지를 전파한다(브로드캐스팅)
     */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //해당 경로 구독하는 클라에 전송시 경로
        registry.enableSimpleBroker("/topic");

        //클라이언트가 서버로 전송시 경로
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 브라우저 환경을 위한 SockJS 지원 엔드포인트
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // 2. Postman 등 테스트 도구를 위한 순수 WebSocket 엔드포인트
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        //클라가 서버로 보내는 메시지 최대치 (1MB)
        registry.setMessageSizeLimit(1024 * 1024);
        //서버가 클라로 보내는 버퍼의 최대치
        registry.setSendBufferSizeLimit(1024 * 1024);
        //대기시간 최대치 (20)
        registry.setSendTimeLimit(20000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트로부터 들어오는 메시지 채널에 커스텀 인터셉터를 추가합니다.
        registration.interceptors(stompInterceptor);
    }

}//end
