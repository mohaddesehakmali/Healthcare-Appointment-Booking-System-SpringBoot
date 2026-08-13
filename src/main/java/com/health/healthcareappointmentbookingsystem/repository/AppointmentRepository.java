package com.health.healthcareappointmentbookingsystem.repository;

import com.health.healthcareappointmentbookingsystem.entity.Appointment;
import com.health.healthcareappointmentbookingsystem.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
 List<Appointment> findByPatientId(Long patientId);
 List<Appointment> findByDoctorId(Long doctorId);
 List<Appointment> findByDoctorIdAndStatus(Long DoctorrId , AppointmentStatus status);
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetweenAndStatusIn(
            Long doctorId, LocalDateTime start, LocalDateTime end, List<AppointmentStatus> statuses);
}
