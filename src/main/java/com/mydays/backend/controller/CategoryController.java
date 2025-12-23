package com.mydays.backend.controller;

import com.mydays.backend.application.category.CategoryService;
import com.mydays.backend.config.CurrentMember;
import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.dto.category.CategoryDtos;
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

    /** 카테고리 목록 */
    @GetMapping
    public ResponseEntity<List<CategoryDtos.Resp>> list(@CurrentMember Member member) {
        List<Category> list = categoryService.list(member);
        return ResponseEntity.ok(list.stream().map(this::toResp).toList());
    }

    /** 카테고리 생성 */
    @PostMapping
    public ResponseEntity<CategoryDtos.Resp> create(@CurrentMember Member member,
                                                    @Valid @RequestBody CategoryDtos.CreateReq req) {
        Category c = categoryService.create(member, req);
        return ResponseEntity.ok(toResp(c));
    }

    /** 카테고리 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDtos.Resp> update(@CurrentMember Member member,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody CategoryDtos.UpdateReq req) {
        Category updated = categoryService.update(member, id, req);
        return ResponseEntity.ok(toResp(updated));
    }

    /** 카테고리 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentMember Member member, @PathVariable Long id) {
        categoryService.delete(member, id);
        return ResponseEntity.noContent().build();
    }

    private CategoryDtos.Resp toResp(Category c) {
        // ✅ null-safe: name/color가 null이어도 500 안 터지게
        return CategoryDtos.Resp.builder()
                .id(c.getId())
                .name(c.getName() == null ? "" : c.getName())
                .color(c.getColor() == null ? "#000000" : c.getColor())
                .build();
    }
}
