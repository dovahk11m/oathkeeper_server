package com.oath.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring Boot 애플리케이션의 캐시 설정을 담당하는 클래스입니다.
 * 이 설정은 JWT 토큰 블랙리스트 관리를 위해 Caffeine 기반의 인메모리 캐시를 구성합니다.
 *
 * 사용법:
 * 1. `@EnableCaching` 어노테이션을 통해 캐시 기능을 활성화합니다.
 * 2. `cacheManager` Bean을 통해 캐시 매니저를 생성하고, "blacklistedTokens"라는 이름의 캐시를 등록합니다.
 * 3. `Expiry` 인터페이스를 구현하여 캐시 항목의 만료 시간을 동적으로 설정합니다.
 *    - 로그아웃 시, 토큰의 남은 유효 시간을 만료 시간으로 설정하여 캐시에 저장합니다.
 *    - 이를 통해 만료된 토큰은 자동으로 캐시에서 제거됩니다.
 * 4. 서비스 레이어에서 `CacheManager`를 주입받아 "blacklistedTokens" 캐시를 사용하여
 *    로그아웃된 토큰을 추가하거나 조회할 수 있습니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        Expiry<Object, Object> expiry = new Expiry<Object, Object>() {
            @Override
            public long expireAfterCreate(Object key, Object value, long currentTime) {
                // value is the remaining duration in nanoseconds
                return (Long) value;
            }

            @Override
            public long expireAfterUpdate(Object key, Object value, long currentTime, @NonNegative long currentDuration) {
                return (Long) value;
            }

            @Override
            public long expireAfterRead(Object key, Object value, long currentTime, @NonNegative long currentDuration) {
                // Do not change expiry on read
                return currentDuration;
            }
        };

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfter(expiry);
        cacheManager.setCaffeine(caffeine);
        cacheManager.setCacheNames(List.of("blacklistedTokens"));
        return cacheManager;
    }
}
