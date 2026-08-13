package com.health.healthcareappointmentbookingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table (name = "patient")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(nullable = false)
    private String fullName;
    @Column(length = 100, nullable = false, unique = true)
    private String email;
    private String phoneNumber;
    private String address;
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    @Column(nullable = false)
    private String gender;
    @Column(nullable = false)
    private String nationalId;
    @Enumerated(EnumType.STRING)
    private BloodType bloodType;
}