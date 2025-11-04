package com.oath.domain.map.google;

import com.oath.domain.map.common.SocialMapApiStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleMapService {

    private final SocialMapApiStrategy socialMapApiStrategy;
}
