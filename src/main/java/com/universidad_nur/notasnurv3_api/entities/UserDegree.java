package com.universidad_nur.notasnurv3_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_degrees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "degree_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserDegree extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)

    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id", nullable = false)

    private Degree degree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private ProfileType type; // STUDENT, TEACHER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private AcademicStatus status; // ACTIVE, INACTIVE, GRADUATED, DROPPED_OUT
}