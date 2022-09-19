package com.codestates.seb006main.Image.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ImageDto {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Post {

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Patch {

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Response {
        private Long imageId;
        private String storedName;

        @Builder
        public Response(Long imageId, String storedName) {
            this.imageId = imageId;
            this.storedName = storedName;
        }
    }
}
