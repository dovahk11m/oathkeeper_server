package com.oath.domain.chatEntity;

import com.oath.common.util.MaskingUtil;
import com.oath.domain.chats.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatEntityService {

    private final ChatEntityRepository chatEntityRepository;

    public Chat saveMessage(Chat msg) {

        // 먼저 검사할 메시지인지 판단
        if (MaskingUtil.shouldInspect(msg.getContent())) {

            // 진짜 민감정보 추출 (Pattern, keyword 등)
            List<ChatEntity> entities = MaskingUtil.extractEntities(msg.getContent(), msg);
            chatEntityRepository.saveAll(entities);

            // 마스킹 적용
            String masked = MaskingUtil.maskMessage(msg.getContent(), entities);

            Chat maskedChat = Chat.builder()
                    .id(msg.getId())
                    .group(msg.getGroup())
                    .sender(msg.getSender())
                    .content(msg.getContent())
                    .maskedContent(masked)
                    .planId(msg.getPlanId())
                    .build();

            return maskedChat;

        } else {
            // 검사할 필요 없는 메시지
            Chat maskedChat = Chat.builder()
                    .id(msg.getId())
                    .group(msg.getGroup())
                    .sender(msg.getSender())
                    .content(msg.getContent())
                    .planId(msg.getPlanId())
                    .build();

            return maskedChat;

        }

    }

}
