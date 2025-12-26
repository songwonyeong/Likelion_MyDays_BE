package com.mydays.backend.application.member;

import com.mydays.backend.auth.service.EmailVerificationService;
import com.mydays.backend.domain.AuthProvider;
import com.mydays.backend.domain.Member;
import com.mydays.backend.infrastructure.member.MemberJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MemberSignupService {

    private final MemberJpaRepository repo;
    private final PasswordEncoder encoder;
    private final Optional<EmailVerificationService> emailVerificationService;

    public MemberSignupService(MemberJpaRepository repo,
                               PasswordEncoder encoder,
                               Optional<EmailVerificationService> emailVerificationService) {
        this.repo = repo;
        this.encoder = encoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public Member signupLocal(String email, String rawPassword, String name, String emailVerifiedJwt) {

        // ✅ mail.enabled=false 환경이면 EmailVerificationService가 없을 수 있음
        EmailVerificationService evs = emailVerificationService.orElseThrow(
                () -> new IllegalStateException("현재 이메일 인증 기능이 비활성화되어 있습니다. (mail.enabled=false)")
        );

        // 1) 이메일 검증 토큰 → 이메일 추출
        String verifiedEmail = evs.parseEmailFromEmailJwt(emailVerifiedJwt);
        if (!email.equalsIgnoreCase(verifiedEmail)) {
            throw new IllegalArgumentException("이메일 인증 토큰이 요청 이메일과 일치하지 않습니다.");
        }

        // 2) 중복 체크
        if (repo.existsByEmail(email)) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        // 3) 도메인 생성/저장
        Member m = new Member();
        m.setEmail(email);
        m.setUsername(name);
        m.setPasswordHash(encoder.encode(rawPassword));
        m.setProvider(AuthProvider.LOCAL);
        return repo.save(m);
    }
}
