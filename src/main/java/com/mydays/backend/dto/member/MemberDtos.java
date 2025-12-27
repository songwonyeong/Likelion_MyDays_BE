package com.mydays.backend.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class MemberDtos {

    @Getter
    @AllArgsConstructor
    public static class MeResp {
        private String email;
        private String name;
    }
}
