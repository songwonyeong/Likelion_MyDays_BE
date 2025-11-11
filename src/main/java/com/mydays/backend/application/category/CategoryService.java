package com.mydays.backend.application.category;

import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.dto.category.CategoryDtos;
import com.mydays.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(Member member, CategoryDtos.CreateReq req){
        categoryRepository.findByMemberAndName(member, req.getName())
                .ifPresent(c -> { throw new IllegalArgumentException("이미 존재하는 카테고리명입니다."); });

        Category c = Category.builder()
                .member(member)
                .name(req.getName())
                .color(req.getColor())
                .build();
        return categoryRepository.save(c);
    }

    @Transactional(readOnly = true)
    public List<Category> list(Member member){
        return categoryRepository.findAllByMemberOrderByNameAsc(member);
    }
}
