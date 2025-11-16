package com.oath.domain.chatEntity;

public enum MaskingLevel {
    STRICT,    // 이름, 전화, 이메일, 주소, 주민번호 모두 마스킹
    MEDIUM,    // 전화번호 일부, 이메일 일부만 마스킹
    NONE       // 원문 그대로
}

