package com.mydays.backend.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TodoDtos {

    /** 생성 요청: 프론트가 categoryId로 보냄 */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReq {

        @NotNull
        @JsonProperty("categoryId")
        private Long categoryId;   // ✅ 변경: category_name -> categoryId

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

        // ✅ 응답은 기존 프론트에서 쓰기 좋게 유지
        private Long categoryId;
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
