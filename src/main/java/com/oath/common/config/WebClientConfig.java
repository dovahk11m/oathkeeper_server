// src/main/java/com/oath/common/config/WebClientConfig.java
package com.oath.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 API와의 비동기 통신을 위한 WebClient 설정을 담당합니다.
 */
@Configuration
public class WebClientConfig {

    /**
     * Python AI 서버와의 통신을 위한 전용 WebClient Bean을 생성합니다.
     *
     * - baseUrl: AI 서버의 기본 주소를 설정합니다.
     * - MetricsOrchestrationController에서 이 Bean을 주입받아 사용합니다.
     *
     * @implNote 현재 개발 환경(학원 컴퓨터)의 네트워크 제약으로 인해,
     *           MetricsOrchestrationController에서 이 WebClient를 사용하는 실제 통신 로직은
     *           임시로 주석 처리되어 있습니다. 하지만 컨트롤러의 의존성 주입(DI) 자체는
     *           여전히 필요하므로, 이 Bean 설정은 반드시 유지되어야 합니다.
     *
     * @param objectMapper JSON 직렬화/역직렬화를 위한 ObjectMapper
     * @return AI 서버 통신용으로 설정된 WebClient 인스턴스
     */
    @Bean
    public WebClient aiClient(ObjectMapper objectMapper) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> {
                    c.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    c.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    c.defaultCodecs().maxInMemorySize(1_000_000);
                })
                .build();

        return WebClient.builder()
                .baseUrl("http://192.168.0.187:8001") // localhost를 실제 IP로 변경
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }
}
