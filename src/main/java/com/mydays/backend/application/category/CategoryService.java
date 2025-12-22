package com.mydays.backend.application.category;

import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.dto.category.CategoryDtos;
import com.mydays.backend.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ✅ 핵심: 영속 Member 프록시를 얻기 위해 추가
    private final EntityManager em;

    private Member managedMember(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return em.getReference(Member.class, member.getId());
    }

    @Transactional
    public Category create(Member member, CategoryDtos.CreateReq req) {
        Member m = managedMember(member);

        categoryRepository.findByMemberAndName(m, req.getName())
                .ifPresent(c -> { throw new IllegalArgumentException("이미 존재하는 카테고리명입니다."); });

        Category c = Category.builder()
                .member(m) // ✅ managed member 사용
                .name(req.getName())
                .color(req.getColor())
                .build();
        return categoryRepository.save(c);
    }

    @Transactional(readOnly = true)
    public List<Category> list(Member member) {
        Member m = managedMember(member);
        return categoryRepository.findAllByMemberOrderByNameAsc(m);
    }
}
