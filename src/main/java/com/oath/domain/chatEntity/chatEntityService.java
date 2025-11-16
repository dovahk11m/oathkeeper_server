package com.oath.domain.chatEntity;

import com.oath.common.util.MaskingUtil;
import com.oath.domain.chats.Chat;

import java.util.List;

public class chatEntityService {

    public void saveMessage(Chat msg) {

        // ① 먼저 검사할 메시지인지 판단
        if (MaskingUtil.shouldInspect(msg.getContent())) {

            // ② 진짜 민감정보 추출 (Pattern, keyword 등)
            List<ChatEntity> entities = MaskingUtil.extractEntities(msg.getContent(), msg);

            // ③ 레벨 추정 (전화/주소/이메일 등)
            MaskingLevel level = MaskingUtil.detectMaskLevel(entities);

            // ④ 마스킹 적용
            String masked = MaskingUtil.maskMessage(msg.getContent(), entities, level);

            msg.setMaskedContent(masked);
        } else {
            // 검사할 필요 없는 메시지
            msg.setMaskedContent(msg.getContent());
        }

        // ⑤ DB 저장
        messageRepository.save(msg);
    }

}
