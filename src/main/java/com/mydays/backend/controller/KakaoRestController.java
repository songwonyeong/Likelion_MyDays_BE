package com.mydays.backend.controller;

import com.mydays.backend.application.auth.RefreshTokenService;
import com.mydays.backend.domain.Member;
import com.mydays.backend.service.KakaoService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/kakao")
public class KakaoRestController {

    private final KakaoService kakaoService;
    private final RefreshTokenService refreshTokenService;

    @Value("${frontend.redirect-uri:http://localhost:3000/main}")
    private String frontendRedirectUri;

    @Value("${refresh.cookie.name:refresh_token}") private String refreshCookieName;
    @Value("${refresh.cookie.secure:false}") private boolean refreshCookieSecure;
    @Value("${refresh.cookie.path:/}") private String refreshCookiePath;
    @Value("${refresh.cookie.same-site:Lax}") private String refreshCookieSameSite;
    @Value("${refresh.ttl-days:30}") private int refreshTtlDays;

    /**
     * ✅ 카카오 로그인 콜백:
     * - Member upsert
     * - refresh 발급해서 HttpOnly 쿠키로 내려줌
     * - access 발급/갱신은 무조건 POST /auth/token/refresh 로 통일
     */
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code,
                                      HttpServletRequest req,
                                      HttpServletResponse res) {
        try {
            String kakaoAccessToken = kakaoService.getAccessToken(code);
            var userInfo = kakaoService.getUserInfo(kakaoAccessToken);

            Member member = kakaoService.processUser(userInfo);

            String ua = req.getHeader("User-Agent");
            String ip = RefreshTokenService.safeIp(req);
            var rt = refreshTokenService.issue(member.getId(), ua, ip);

            setRefreshCookie(res, rt.getToken());

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendRedirectUri))
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendRedirectUri + "?error=oauth"))
                    .build();
        }
    }

    // =========================
    // 🔥 완전 통합: 더 이상 사용하지 않는 kakao 토큰 엔드포인트
    // =========================

    /**
     * ✅ (권장) 410 Gone: 이제 refresh는 /auth/token/refresh만 사용
     * - 프론트가 실수로 호출해도 "어디로 바꿔야 하는지" 즉시 알 수 있게 함
     * - 프론트 전환 끝나면 이 메서드 자체를 삭제해도 됨(404로)
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> deprecatedKakaoRefresh() {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "status", "gone",
                "message", "This endpoint is deprecated. Use POST /auth/token/refresh instead."
        ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> deprecatedKakaoLogout() {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "status", "gone",
                "message", "This endpoint is deprecated. Use POST /auth/token/logout instead."
        ));
    }

    @Hidden
    @PostMapping("/auth/logout-all")
    public ResponseEntity<?> deprecatedKakaoLogoutAll() {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "status", "gone",
                "message", "This endpoint is deprecated. Use POST /auth/token/logout (and revoke-all there if needed)."
        ));
    }

    @Hidden
    @GetMapping("/logout-url")
    public ResponseEntity<?> logoutUrl() {
        return ResponseEntity.ok(Map.of("url", kakaoService.buildKakaoLogoutUrl()));
    }

    // --- cookie helpers ------------------------------------------------

    private void setRefreshCookie(HttpServletResponse res, String refresh) {
        int maxAgeSec = refreshTtlDays * 24 * 60 * 60;

        Cookie c = new Cookie(refreshCookieName, refresh);
        c.setHttpOnly(true);
        c.setSecure(refreshCookieSecure);
        c.setPath(refreshCookiePath);
        c.setMaxAge(maxAgeSec);
        res.addCookie(c);

        // SameSite 보완 헤더 (브라우저별 대응)
        String header = String.format("%s=%s; Max-Age=%d; Path=%s; %s; HttpOnly; SameSite=%s",
                refreshCookieName, refresh, maxAgeSec, c.getPath(),
                refreshCookieSecure ? "Secure" : "",
                StringUtils.hasText(refreshCookieSameSite) ? refreshCookieSameSite : "Lax");
        res.addHeader("Set-Cookie", header);
    }
}
