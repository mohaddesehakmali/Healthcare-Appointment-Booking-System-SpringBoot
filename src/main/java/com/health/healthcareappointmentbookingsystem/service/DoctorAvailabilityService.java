package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AvailabilityRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.TimeSlotResponse;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.repository.AppointmentRepository;
import com.health.healthcareappointmentbookingsystem.repository.DoctorAvailabilityRepository;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private static final int SLOT_MINUTES = 30;

    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    /** Doctor defines a recurring weekly working window (e.g. Monday 09:00–17:00). */
    public void setAvailability(AvailabilityRequest request) {
        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for current user"));

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalStateException("End time must be after start time");
        }

        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        availabilityRepository.save(availability);
    }

    /** Returns free 30-minute slots for a given doctor on a given date. */
    public List<TimeSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        List<DoctorAvailability> windows =
                availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());

        if (windows.isEmpty()) {
            return List.of();
        }

        // Appointments that already occupy time on this date (not cancelled/rejected)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateTimeBetweenAndStatusIn(
                        doctorId,
                        startOfDay,
                        endOfDay,
                        List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
                );

        Set<LocalDateTime> bookedStarts = existingAppointments.stream()
                .map(Appointment::getAppointmentDateTime)
                .collect(Collectors.toSet());

        List<TimeSlotResponse> freeSlots = new ArrayList<>();

        for (DoctorAvailability window : windows) {
            LocalTime slotStartTime = window.getStartTime();

            while (slotStartTime.plusMinutes(SLOT_MINUTES).compareTo(window.getEndTime()) <= 0) {
                LocalDateTime slotStart = date.atTime(slotStartTime);
                LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);

                if (!bookedStarts.contains(slotStart)) {
                    freeSlots.add(new TimeSlotResponse(slotStart, slotEnd));
                }

                slotStartTime = slotStartTime.plusMinutes(SLOT_MINUTES);
            }
        }

        return freeSlots;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}