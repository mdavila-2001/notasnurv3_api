package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "degree")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Degree extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
}
