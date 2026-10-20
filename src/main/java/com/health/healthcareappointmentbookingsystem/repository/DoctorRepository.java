package com.health.healthcareappointmentbookingsystem.repository;

import com.health.healthcareappointmentbookingsystem.entity.Doctor;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    boolean existsByEmail(String email);
    List<Doctor> findBySpecialization(Specialization specialization);

    // Serialize bookings for the same doctor so two patients cannot book one slot concurrently.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
}
