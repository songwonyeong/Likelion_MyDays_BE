package com.mydays.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "todo",
        indexes = {
                @Index(name="idx_todo_member_date", columnList = "member_id,scheduled_date"),
                @Index(name="idx_todo_category", columnList = "category_id")
        })
public class Todo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유자 (보안 스코프) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    /** 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="category_id", nullable = false)
    private Category category;

    /** 내용 */
    @Column(nullable = false, length = 255)
    private String content;

    /** 완료 여부 */
    @Column(nullable = false)
    private boolean done;

    /** 일정 날짜/시간 (시간은 옵션) */
    @Column(name="scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name="scheduled_time")
    private LocalTime scheduledTime; // nullable

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void prePersist(){ createdAt = updatedAt = Instant.now(); if (!this.done) this.done = false; }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now(); }
}
