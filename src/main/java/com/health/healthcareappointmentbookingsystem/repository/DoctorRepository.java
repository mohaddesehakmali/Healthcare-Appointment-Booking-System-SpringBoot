package com.health.healthcareappointmentbookingsystem.repository;

import com.health.healthcareappointmentbookingsystem.entity.Doctor;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    @Override
    Optional<Doctor> findById(Long aLong);
    Optional<Doctor> findByUserId(Long userId);
    boolean existsByEmail(String email);
    List<Doctor> findBySpecialization(Specialization specialization);
}
