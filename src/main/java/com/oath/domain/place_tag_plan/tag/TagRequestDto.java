package com.oath.domain.place_tag_plan.tag;

import lombok.Getter;
import lombok.Setter;

public class TagRequestDto {

    @Getter
    @Setter
    public static class CreateTagDto {
        private String name;
    }

    @Getter
    @Setter
    public static class UpdateTagDto {
        private String name;
    }
}
