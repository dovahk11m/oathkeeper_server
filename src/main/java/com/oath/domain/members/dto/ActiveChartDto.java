package com.oath.domain.members.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ActiveChartDto {

        @JsonProperty("x")
        private int hour;
        @JsonProperty("y")
        private int dayOfWeek;
        @JsonProperty("v")
        private Long count;

        public ActiveChartDto(int hour, int dayOfWeek, Long count) {
            this.hour = hour;
            this.dayOfWeek = dayOfWeek;
            this.count = count;
        }

}
