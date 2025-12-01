package com.oath.common.util;

import com.oath.domain.chatEntity.ChatEntity;
import com.oath.domain.chatEntity.EntityType;
import com.oath.domain.chatEntity.MaskingLevel;
import com.oath.domain.chats.Chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingUtil {

    public static boolean shouldInspect(String message) {
        // ① 정규식 가능성 체크 (전화번호, 이메일)
        boolean patternMatch = message.contains("@") || message.matches(".*\\d{2,3}-\\d{3,4}-\\d{4}.*");

        // ② 키워드 기반 체크
        String[] sensitiveKeywords = {"전화", "이메일", "계좌", "주소"};
        boolean keywordMatch = Arrays.stream(sensitiveKeywords).anyMatch(message::contains);

        // 둘 중 하나라도 true이면 민감정보 추출 수행
        return patternMatch || keywordMatch;
    }


    public static List<ChatEntity> extractEntities(String text, Chat chat) {
        List<ChatEntity> entities = new ArrayList<>();

        // 전화번호
        Pattern phonePattern = Pattern.compile("(\\d{2,3})-?(\\d{3,4})-?(\\d{4})");
        Matcher phoneMatcher = phonePattern.matcher(text);
        while (phoneMatcher.find()) {
            ChatEntity e = ChatEntity.builder()
                    .chat(chat)
                    .type(EntityType.PHONE)
                    .startIndex(phoneMatcher.start())
                    .endIndex(phoneMatcher.end())
                    .realValue(phoneMatcher.group())
                    .build();
            entities.add(e);
        }

        // 이메일
        Pattern emailPattern = Pattern.compile("([a-zA-Z0-9._%+-]{2,})@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher emailMatcher = emailPattern.matcher(text);
        while (emailMatcher.find()) {
            ChatEntity e = ChatEntity.builder()
                    .chat(chat)
                    .type(EntityType.EMAIL)
                    .startIndex(emailMatcher.start())
                    .endIndex(emailMatcher.end())
                    .realValue(emailMatcher.group())
                    .build();
            entities.add(e);
        }

        // 주민번호, 주소 등 필요하면 추가
        return entities;
    }

    public static String maskMessage(String text, List<ChatEntity> entities) {
        StringBuilder masked = new StringBuilder(text);

        for (int i = entities.size() - 1; i >= 0; i--) {
            ChatEntity e = entities.get(i);

                int start = e.getStartIndex();
                int end = e.getEndIndex();
                masked.replace(start, end, "*".repeat(end - start));

        }
        return masked.toString();
    }

//    public static MaskingLevel detectMaskLevel(List<ChatEntity> entities) {
//        boolean hasSensitive = entities.stream().anyMatch(e ->
//                e.getType() == EntityType.PHONE || e.getType() == EntityType.EMAIL || e.getType() == EntityType.ADDRESS
//        );
//
//        if (hasSensitive) return MaskingLevel.STRICT;
//        else return MaskingLevel.MEDIUM;
//    }
//
//
//    private static boolean shouldMask(EntityType type, MaskingLevel level) {
//        return switch(level) {
//            case STRICT -> true;
//            case MEDIUM -> type == EntityType.PHONE || type == EntityType.EMAIL;
//            case NONE -> false;
//        };
//    }
}
