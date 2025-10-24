package com.oath.domain.alarms;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlarmDTO {
    private final String to;
    private final String subject;
    private final String content;
}