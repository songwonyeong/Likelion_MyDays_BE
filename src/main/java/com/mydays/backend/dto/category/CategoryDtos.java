package com.mydays.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class CategoryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReq {
        @NotBlank private String name;
        private String color;
    }

    // ✅ 수정 요청
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReq {
        @NotBlank private String name;
        private String color;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resp {
        private Long id;
        private String name;
        private String color;
    }
}
