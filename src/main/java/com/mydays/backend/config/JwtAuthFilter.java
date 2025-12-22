package com.mydays.backend.config;

import com.mydays.backend.domain.Member;
import com.mydays.backend.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;
    private final String jwtSecret;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String uri = request.getRequestURI();
        return uri.startsWith("/auth/")
                || uri.startsWith("/kakao/")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        // ✅ OPTIONS는 무조건 통과
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        // =========================
        // ✅ 1) JWT 검증(여기서만 401)
        // =========================
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // JWT 자체가 잘못된 경우에만 401
            System.out.println("[JWT FAIL] " + e.getClass().getName() + " : " + e.getMessage());
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        // =========================
        // ✅ 2) memberId 클레임 확인
        // =========================
        Long memberId = claims.get("memberId", Long.class);
        if (memberId == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token (no memberId)");
            return;
        }

        // =========================
        // ✅ 3) DB 조회 + request attribute 세팅
        //    (여기서 터지는 예외는 401로 바꾸지 않음)
        // =========================
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found: " + memberId));

        // ✅ CurrentMemberArgumentResolver가 읽는 키
        req.setAttribute("authMember", member);
        // (선택) 기존 코드 호환
        req.setAttribute("currentMember", member);

        // 이제부터 발생하는 예외는 “진짜 서버 에러(500)”로 보이게 둔다
        chain.doFilter(req, res);
    }
}
