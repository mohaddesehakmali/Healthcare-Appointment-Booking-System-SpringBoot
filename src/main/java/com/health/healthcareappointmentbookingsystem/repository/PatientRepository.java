package com.health.healthcareappointmentbookingsystem.repository;

import com.health.healthcareappointmentbookingsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByUserId(Long userId);
}
