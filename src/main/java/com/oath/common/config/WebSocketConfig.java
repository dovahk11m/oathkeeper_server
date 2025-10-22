package com.oath.common.config;

import org.springframework.context.annotation.Configuration;
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
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
        registry.addEndpoint("ws-stomp").setAllowedOriginPatterns("*").withSockJS();
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

}//end
