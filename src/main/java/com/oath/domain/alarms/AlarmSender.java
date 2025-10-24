package com.oath.domain.alarms;

public interface AlarmSender {

    void send(AlarmDTO request);

    boolean supports(String type);
}
