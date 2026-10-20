package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AppointmentRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AppointmentResponse;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.mapper.AppointmentMapper;
import com.health.healthcareappointmentbookingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private static final int MIN_AGE_TO_BOOK = 13;
    private static final int SLOT_MINUTES = 30;
    private static final List<AppointmentStatus> ACTIVE = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        User user = currentUser();
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Patient profile not found"));
        validateMinimumAge(patient);
        LocalDateTime start = request.getAppointmentDateTime();
        if (start == null || !start.isAfter(LocalDateTime.now()) || start.getMinute() % 30 != 0
                || start.getSecond() != 0 || start.getNano() != 0) {
            throw new IllegalArgumentException("Appointment must be in a future 30-minute slot");
        }
        Doctor doctor = doctorRepository.findByIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new IllegalStateException("Doctor not found with id: " + request.getDoctorId()));
        LocalTime begin = start.toLocalTime();
        LocalTime finish = begin.plusMinutes(SLOT_MINUTES);
        boolean available = availabilityRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), start.getDayOfWeek())
                .stream().anyMatch(w -> !begin.isBefore(w.getStartTime()) && !finish.isAfter(w.getEndTime()));
        if (!available) throw new IllegalStateException("Doctor is not available at the requested time");
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateTimeAndStatusIn(doctor.getId(), start, ACTIVE)) {
            throw new IllegalStateException("This appointment slot is already booked");
        }
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(start);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setStatus(AppointmentStatus.PENDING);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long id) { return changeStatus(id, AppointmentStatus.CONFIRMED); }
    @Transactional
    public AppointmentResponse rejectAppointment(Long id) { return changeStatus(id, AppointmentStatus.REJECTED); }
    @Transactional
    public AppointmentResponse cancelAppointment(Long id) { return changeStatus(id, AppointmentStatus.CANCELLED); }

    private AppointmentResponse changeStatus(Long id, AppointmentStatus next) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Appointment not found: " + id));
        User user = currentUser();
        Role role = user.getRole();
        if (next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.REJECTED) {
            if (role != Role.DOCTOR || !appointment.getDoctor().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Only the assigned doctor can confirm or reject");
            }
            if (appointment.getStatus() != AppointmentStatus.PENDING) {
                throw new IllegalStateException("Only pending appointments can be confirmed or rejected");
            }
        } else {
            boolean ownsAppointment = (role == Role.PATIENT &&
                    appointment.getPatient().getUser().getId().equals(user.getId()))
                    || (role == Role.DOCTOR && appointment.getDoctor().getUser().getId().equals(user.getId()));
            if (role != Role.ADMIN && !ownsAppointment) throw new AccessDeniedException("You cannot cancel this appointment");
            if (appointment.getStatus() != AppointmentStatus.PENDING &&
                    appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                throw new IllegalStateException("Only pending or confirmed appointments may be cancelled");
            }
        }
        appointment.setStatus(next);
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentsAsPatient() {
        Patient patient = patientRepository.findByUserId(currentUser().getId())
                .orElseThrow(() -> new IllegalStateException("Patient profile not found"));
        return appointmentRepository.findByPatientId(patient.getId()).stream().map(AppointmentMapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentsAsDoctor() {
        Doctor doctor = doctorRepository.findByUserId(currentUser().getId())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found"));
        return appointmentRepository.findByDoctorId(doctor.getId()).stream().map(AppointmentMapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPendingAppointmentsForDoctor() {
        Doctor doctor = doctorRepository.findByUserId(currentUser().getId())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found"));
        return appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING)
                .stream().map(AppointmentMapper::toResponse).toList();
    }
    private void validateMinimumAge(Patient patient) {
        if (patient.getDateOfBirth() == null) throw new IllegalStateException("Patient birth date is missing");
        if (Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() < MIN_AGE_TO_BOOK) {
            throw new IllegalStateException("Patients under 13 cannot book independently");
        }
    }
    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
