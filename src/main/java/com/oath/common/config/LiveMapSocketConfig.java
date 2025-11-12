package com.oath.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class LiveMapSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry reg) {
        // 안드/Flutter는 순수 WS 사용하므로 SockJS 불필요.
        // 경로는 클라이언트와 반드시 일치!
        reg.addEndpoint("/ws-location")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry reg) {
        // 심플 브로커 + 하트비트 + 스케줄러
        reg.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000}); // 10s/10s
        reg.setApplicationDestinationPrefixes("/app");
    }

    // 하트비트 스케줄러 (없으면 하트비트가 실제로 안 나감)
    @Bean
    public ThreadPoolTaskScheduler stompHeartbeatScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(1);
        ts.setThreadNamePrefix("stomp-heartbeat-");
        ts.initialize();
        return ts;
    }

    // 톰캣 WS 세션/전송 타임아웃 여유 있게
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        var c = new ServletServerContainerFactoryBean();
        c.setMaxSessionIdleTimeout(0L);   // 무제한(또는 3600000L = 1시간 등)
        c.setAsyncSendTimeout(0L);
        return c;
    }
}
