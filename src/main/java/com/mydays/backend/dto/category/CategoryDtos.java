package com.mydays.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class CategoryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReq {
        @NotBlank @Size(max=60)
        private String name;
        @NotBlank @Size(max=20)
        private String color;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resp {
        private Long id;
        private String name;
        private String color;
    }
}
