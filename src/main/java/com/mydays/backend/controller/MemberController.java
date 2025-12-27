package com.mydays.backend.controller;

import com.mydays.backend.config.CurrentMember;
import com.mydays.backend.domain.Member;
import com.mydays.backend.dto.member.MemberDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

    /**
     * 내 계정 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDtos.MeResp> me(@CurrentMember Member member) {
        return ResponseEntity.ok(
                new MemberDtos.MeResp(
                        member.getEmail(),
                        member.getUsername()
                )
        );
    }
}
