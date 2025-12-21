package com.mydays.backend.auth.controller;

import com.mydays.backend.application.auth.RefreshTokenService;
import com.mydays.backend.application.member.MemberLoginService;
import com.mydays.backend.auth.dto.LoginDtos.*;
import com.mydays.backend.auth.service.TokenIssuer;
import com.mydays.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
public class LoginController {

    private final MemberLoginService memberLoginService;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenService refreshTokenService;

    @Value("${refresh.cookie.name:refresh_token}")
    private String refreshCookieName;

    @Value("${refresh.cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${refresh.cookie.path:/}")
    private String refreshCookiePath;

    @Value("${refresh.cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    @Value("${refresh.ttl-days:30}")
    private int refreshTtlDays;

    public LoginController(MemberLoginService memberLoginService,
                           TokenIssuer tokenIssuer,
                           RefreshTokenService refreshTokenService) {
        this.memberLoginService = memberLoginService;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req,
                                             HttpServletRequest httpReq,
                                             HttpServletResponse httpRes) {

        // 1) 이메일/비번 검증
        var member = memberLoginService.login(req.email(), req.password());

        // 2) Access Token 발급
        String accessToken = tokenIssuer.issueAccessToken(
                member.getId(),
                member.getEmail(),
                "USER" // role 컬럼 없으면 일단 USER 고정
        );

        // 3) Refresh Token 발급 → HttpOnly 쿠키로 내려줌
        String ua = httpReq.getHeader("User-Agent");
        String ip = RefreshTokenService.safeIp(httpReq);
        var rt = refreshTokenService.issue(member.getId(), ua, ip);

        int maxAgeSec = refreshTtlDays * 24 * 60 * 60;
        CookieUtil.add(httpRes, refreshCookieName, rt.getToken(), maxAgeSec,
                refreshCookiePath, refreshCookieSecure, refreshCookieSameSite);

        // 4) Access Token만 바디로
        return ResponseEntity.ok(new JwtResponse(accessToken));
    }
}
