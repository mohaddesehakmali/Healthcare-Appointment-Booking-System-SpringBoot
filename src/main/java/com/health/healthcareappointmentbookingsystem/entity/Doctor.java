package com.health.healthcareappointmentbookingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "doctor")
@Data
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column (unique = true ,nullable = false)
    private String phone;

    @Column (unique = true,nullable = false)
    private String email;

    @Column (unique = true ,nullable = false)
    private String licenseNumber;

    @Column (nullable = false )
    private String hospital;

    @Column (nullable = false )
    private String yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false )
    private  Specialization specialization;
}
