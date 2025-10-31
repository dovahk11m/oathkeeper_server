package com.oath.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class LiveMapSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override public void registerStompEndpoints(StompEndpointRegistry reg) {
        reg.addEndpoint("/ws-location").setAllowedOriginPatterns("*");
    }
    @Override public void configureMessageBroker(MessageBrokerRegistry reg) {
        reg.enableSimpleBroker("/topic");
        reg.setApplicationDestinationPrefixes("/app");
    }
}
