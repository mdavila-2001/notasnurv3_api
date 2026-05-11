package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "component")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@SQLDelete(sql = "UPDATE component SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at is NULL")
public class Components extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private EvaluationPlan plan;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    private String description;
}
