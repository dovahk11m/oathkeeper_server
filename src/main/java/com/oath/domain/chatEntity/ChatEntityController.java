package com.oath.domain.chatEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatEntityController {

    private final ChatEntityService chatEntityService;
}
