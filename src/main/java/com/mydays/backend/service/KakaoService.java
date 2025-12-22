package com.mydays.backend.service;

import com.mydays.backend.domain.AuthProvider;
import com.mydays.backend.domain.Member;
import com.mydays.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.client.id}")        private String clientId;          // REST API Key
    @Value("${kakao.client.secret:}")   private String clientSecret;      // 콘솔에서 '사용함'이면 반드시 전송
    @Value("${kakao.login.redirect}")   private String redirectUri;
    @Value("${kakao.logout.redirect}")  private String logoutRedirect;

    private final RestTemplate restTemplate = new RestTemplate();

    private final MemberRepository memberRepository;

    /** 인가코드로 카카오 access_token 교환 */
    public String getAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId.trim());
        params.add("redirect_uri", redirectUri.trim());
        params.add("code", code.trim());

        // client_secret은 '사용함' 설정인 경우 필수
        if (clientSecret != null && !clientSecret.isBlank()) {
            params.add("client_secret", clientSecret.trim());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Object token = res.getBody() != null ? res.getBody().get("access_token") : null;
            if (token == null) throw new IllegalStateException("kakao access_token 없음");

            return String.valueOf(token);

        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("카카오 토큰 교환 실패: " + e.getResponseBodyAsString(), e);
        }
    }

    /** kakao access_token으로 사용자 정보 조회 */
    public Map<String, Object> getUserInfo(String kakaoAccessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url, HttpMethod.GET, request, new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return res.getBody();
    }

    /**
     * kakaoId 기준 업서트만 수행하고 Member 반환
     * (토큰 발급은 Controller에서 RefreshTokenService + TokenIssuer로 단일화)
     */
    public Member processUser(Map<String, Object> userInfo) {
        Long kakaoId = Long.valueOf(String.valueOf(userInfo.get("id")));
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties   = (Map<String, Object>) userInfo.get("properties");

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String nickname = properties != null ? (String) properties.get("nickname") : null;

        Member member = memberRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> Member.builder()
                        .kakaoId(kakaoId)
                        .provider(AuthProvider.KAKAO)
                        .build());

        // 기존 회원이거나 신규 회원이거나 동일하게 최신 정보로 갱신
        if (email != null && !email.isBlank()) member.setEmail(email);
        if (nickname != null && !nickname.isBlank()) member.setUsername(nickname);

        // provider는 KAKAO로 고정(로컬 계정과 충돌 방지)
        member.setProvider(AuthProvider.KAKAO);

        member = memberRepository.save(member);

        return member;
    }

    /** 카카오 계정 로그아웃 URL 생성(옵션) */
    public String buildKakaoLogoutUrl() {
        return "https://kauth.kakao.com/oauth/logout?client_id=" + clientId + "&logout_redirect_uri=" + logoutRedirect;
    }
}
