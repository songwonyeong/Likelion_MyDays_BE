package com.mydays.backend.controller;

import com.mydays.backend.config.CurrentMember;
import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.dto.category.CategoryDtos;
import com.mydays.backend.application.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /** 카테고리 등록 (name, color) */
    @PostMapping
    public ResponseEntity<CategoryDtos.Resp> create(@CurrentMember Member member,
                                                    @Valid @RequestBody CategoryDtos.CreateReq req){
        Category c = categoryService.create(member, req);
        return ResponseEntity.ok(new CategoryDtos.Resp(c.getId(), c.getName(), c.getColor()));
    }

    /** 카테고리 목록 조회 */
    @GetMapping
    public ResponseEntity<List<CategoryDtos.Resp>> list(@CurrentMember Member member){
        List<Category> list = categoryService.list(member);
        var resp = list.stream()
                .map(c -> new CategoryDtos.Resp(c.getId(), c.getName(), c.getColor()))
                .toList();
        return ResponseEntity.ok(resp);
    }
}
