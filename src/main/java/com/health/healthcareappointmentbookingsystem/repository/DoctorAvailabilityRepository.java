package com.health.healthcareappointmentbookingsystem.repository;

import com.health.healthcareappointmentbookingsystem.entity.AppointmentStatus;
import com.health.healthcareappointmentbookingsystem.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorAvailabilityRepository  extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
