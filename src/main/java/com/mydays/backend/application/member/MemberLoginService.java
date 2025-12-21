package com.mydays.backend.application.member;

import com.mydays.backend.domain.AuthProvider;
import com.mydays.backend.domain.Member;
import com.mydays.backend.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLoginService {

    private final MemberJpaRepository repo;
    private final PasswordEncoder encoder;

    public Member login(String email, String rawPassword) {
        Member m = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 로컬 로그인만 허용
        if (m.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalStateException("카카오로 가입된 계정입니다. 카카오 로그인을 이용해주세요.");
        }

        // 비번 검사
        String hash = m.getPasswordHash();
        if (hash == null || !encoder.matches(rawPassword, hash)) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return m;
    }
}
