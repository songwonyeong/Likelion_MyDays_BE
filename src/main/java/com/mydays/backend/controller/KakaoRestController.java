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

    /**
     * ✅ 프론트 리다이렉트 목적지 (로그인 성공 후 이동)
     * - Azure: FRONTEND_REDIRECT_URI 로 주입 (예: https://likelion-my-days-fe.vercel.app/main)
     * - 없으면 로컬 기본값으로 fallback
     */
    @Value("${frontend.redirect-uri:${FRONTEND_REDIRECT_URI:http://localhost:3000/main}}")
    private String frontendRedirectUri;

    @Value("${refresh.cookie.name:refresh_token}") private String refreshCookieName;

    /**
     * ✅ 배포에서는 true 권장 (https + cross-site 쿠키)
     * Azure 환경변수: REFRESH_COOKIE_SECURE=true
     */
    @Value("${refresh.cookie.secure:${REFRESH_COOKIE_SECURE:false}}")
    private boolean refreshCookieSecure;

    @Value("${refresh.cookie.path:/}") private String refreshCookiePath;

    /**
     * ✅ 배포(Vercel <-> Azure)면 None 권장
     * Azure 환경변수: REFRESH_COOKIE_SAME_SITE=None
     */
    @Value("${refresh.cookie.same-site:${REFRESH_COOKIE_SAME_SITE:Lax}}")
    private String refreshCookieSameSite;

    @Value("${refresh.ttl-days:30}") private int refreshTtlDays;

    /**
     * ✅ 카카오 로그인 콜백 (백엔드 인가 방식)
     * - 카카오에서 code를 받음
     * - Member upsert
     * - refresh 발급해서 HttpOnly 쿠키로 내려줌
     * - 프론트로 이동 (frontendRedirectUri)
     *
     * 중요: 카카오 개발자 콘솔 Redirect URI에
     *  - https://<AZURE_BACKEND>/kakao/callback
     * 가 등록되어 있어야 함.
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
    // 🔥 더 이상 사용하지 않는 kakao 토큰 엔드포인트 (호환용)
    // =========================

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

        // SameSite 보완 헤더
        String sameSite = StringUtils.hasText(refreshCookieSameSite) ? refreshCookieSameSite : "Lax";
        String secure = refreshCookieSecure ? "Secure; " : "";

        String header = String.format(
                "%s=%s; Max-Age=%d; Path=%s; %sHttpOnly; SameSite=%s",
                refreshCookieName, refresh, maxAgeSec, refreshCookiePath,
                secure, sameSite
        );
        res.addHeader("Set-Cookie", header);
    }
}
