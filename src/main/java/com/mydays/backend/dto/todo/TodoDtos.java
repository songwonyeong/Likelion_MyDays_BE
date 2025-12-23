package com.mydays.backend.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TodoDtos {

    /** ✅ 생성 요청: categoryId로 받기 (프론트와 일치) */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReq {

        @NotNull
        private Long categoryId;

        @NotBlank
        private String content;

        private Boolean done; // 옵션, 기본 false

        @NotNull
        private LocalDate date;

        private LocalTime time; // 옵션
    }

    /** 날짜별 조회 응답 */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resp {
        private Long id;

        private Long categoryId;          // ✅ 추가
        private String category_name;
        private String category_color;

        private String content;
        private boolean done;
        private LocalDate date;
        private LocalTime time;
    }

    /** 완료여부 변경 */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ToggleDoneReq {
        @NotNull
        private Boolean done;
    }
}
