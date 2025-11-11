package com.mydays.backend.repository;

import com.mydays.backend.domain.Member;
import com.mydays.backend.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    /** 특정 회원 + 날짜 기준 할일 목록 */
    List<Todo> findAllByMemberAndScheduledDateOrderByScheduledTimeAscIdAsc(Member member, LocalDate date);

    /** 특정 회원의 개별 할일 */
    Optional<Todo> findByIdAndMember(Long id, Member member);
}
