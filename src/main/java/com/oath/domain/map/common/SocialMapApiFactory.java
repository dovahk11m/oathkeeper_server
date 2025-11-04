package com.oath.domain.map.common;

import com.oath.common.exception.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialMapApiFactory {

    private final List<SocialMapApiStrategy> socialMapApiStrategies;

    public SocialMapApiStrategy find(SocialMapType type) {
        for (SocialMapApiStrategy socialMapApiStrategy : socialMapApiStrategies) {
            if (socialMapApiStrategy.match(type)) return socialMapApiStrategy;
        }

        throw new Exception400("해당되는 소셜 Map은 존재하지 않습니다.");
    }
}
