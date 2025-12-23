package com.mydays.backend.dto.todo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TodoDtos {

    /**
     * 생성 요청
     * - categoryId: 필수
     * - content: 필수
     * - date: 필수 (yyyy-MM-dd)
     * - time: 선택 (HH:mm)
     * - done: 선택(기본 false 취급)
     */
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // ✅ Jackson이 필드로 직접 바인딩
    public static class CreateReq {

        @NotNull
        private Long categoryId;

        @NotBlank
        private String content;

        private Boolean done; // null이면 서비스에서 false 처리 권장

        @NotNull
        private LocalDate date;

        private LocalTime time; // null 가능
    }

    /** 조회/응답 DTO (TodoController.toResp 기준) */
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Resp {
        private Long id;

        private Long categoryId;
        private String category_name;
        private String category_color;

        private String content;
        private boolean done;

        private LocalDate date;
        private LocalTime time;
    }

    /** 완료여부 변경 요청: PATCH /api/todos/{id}/done */
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class ToggleDoneReq {
        @NotNull
        private Boolean done;
    }
}


//package com.mydays.backend.dto.todo;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//public class TodoDtos {
//
//    /** ✅ 생성 요청: categoryId로 받기 (프론트와 일치) */
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
//    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
//    public static class CreateReq {
//
//        @NotNull
//        private Long categoryId;
//
//        @NotBlank
//        private String content;
//
//        private Boolean done; // 옵션, 기본 false
//
//        @NotNull
//        private LocalDate date;
//
//        private LocalTime time; // 옵션
//    }
//
//    /** 날짜별 조회 응답 */
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class Resp {
//        private Long id;
//
//        private Long categoryId;          // ✅ 추가
//        private String category_name;
//        private String category_color;
//
//        private String content;
//        private boolean done;
//        private LocalDate date;
//        private LocalTime time;
//    }
//
//    /** 완료여부 변경 */
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
//    public static class ToggleDoneReq {
//        @NotNull
//        private Boolean done;
//    }
//}
