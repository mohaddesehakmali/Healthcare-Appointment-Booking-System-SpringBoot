package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AppointmentRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AppointmentResponse;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.mapper.AppointmentMapper;
import com.health.healthcareappointmentbookingsystem.repository.AppointmentRepository;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import com.health.healthcareappointmentbookingsystem.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int MIN_AGE_TO_BOOK = 13;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /** Patient books a new appointment (always starts as PENDING). */
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        User currentUser = getCurrentUser();

        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Patient profile not found for current user"));

        validateMinimumAge(patient);

        Doctor doctor = doctorRepository.findById(Long.valueOf(request.getDoctorId()))
                .orElseThrow(() -> new IllegalStateException("Doctor not found with id: " + request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setStatus(AppointmentStatus.PENDING);

        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    /** Doctor confirms a pending appointment. */
    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CONFIRMED);
    }

    /** Doctor rejects a pending appointment. */
    @Transactional
    public AppointmentResponse rejectAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.REJECTED);
    }

    /** Patient or doctor cancels an appointment. */
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    private AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Appointment not found with id: " + appointmentId));
        appointment.setStatus(newStatus);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    /** List of appointments for the current logged-in patient. */
    public List<AppointmentResponse> getMyAppointmentsAsPatient() {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Patient profile not found for current user"));
        return appointmentRepository.findByPatientId(patient.getId()).stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    /** List of appointments for the current logged-in doctor. */
    public List<AppointmentResponse> getMyAppointmentsAsDoctor() {
        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for current user"));
        return appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    /** Only the pending appointments for the current logged-in doctor. */
    public List<AppointmentResponse> getPendingAppointmentsForDoctor() {
        User currentUser = getCurrentUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for current user"));
        return appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING).stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    /** Patients under the minimum age cannot book an appointment independently. */
    private void validateMinimumAge(Patient patient) {
        if (patient.getDateOfBirth() == null) {
            throw new IllegalStateException("Patient date of birth is not set; cannot verify age eligibility");
        }
        int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < MIN_AGE_TO_BOOK) {
            throw new IllegalStateException(
                    "Patients under " + MIN_AGE_TO_BOOK + " years old cannot book an appointment independently. "
                            + "A parent or guardian must book on their behalf."
            );
        }
    }

    /** Resolves the currently authenticated user from the security context (set by the JWT filter). */
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}