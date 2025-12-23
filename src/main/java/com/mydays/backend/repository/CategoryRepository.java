package com.mydays.backend.repository;

import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByMemberOrderByNameAsc(Member member);
    Optional<Category> findByMemberAndName(Member member, String name);
    Optional<Category> findByIdAndMember(Long id, Member member);
}
