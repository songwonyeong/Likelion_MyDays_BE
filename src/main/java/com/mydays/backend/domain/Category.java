package com.mydays.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "category",
        uniqueConstraints = @UniqueConstraint(name="uk_category_member_name", columnNames = {"member_id","name"}),
        indexes = @Index(name="idx_category_member", columnList = "member_id"))
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 20)
    private String color; // 예: #AABBCC or "blue-500"

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void prePersist(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now(); }
}
