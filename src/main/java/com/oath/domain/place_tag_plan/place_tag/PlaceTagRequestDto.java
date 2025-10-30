package com.oath.domain.place_tag_plan.place_tag;

import lombok.Getter;
import lombok.Setter;

public class PlaceTagRequestDto {

    @Getter
    @Setter
    public static class ConnectDto {
        private Long placeId;
        private Long tagId;
    }
}
