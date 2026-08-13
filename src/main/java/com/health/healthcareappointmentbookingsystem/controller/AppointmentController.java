package com.health.healthcareappointmentbookingsystem.controller;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AppointmentRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AppointmentResponse;
import com.health.healthcareappointmentbookingsystem.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request));
    }

    @GetMapping("/me/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> getMyAppointmentsAsPatient() {
        return appointmentService.getMyAppointmentsAsPatient();
    }

    @GetMapping("/me/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponse> getMyAppointmentsAsDoctor() {
        return appointmentService.getMyAppointmentsAsDoctor();
    }

    @GetMapping("/me/doctor/pending")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponse> getPendingAppointments() {
        return appointmentService.getPendingAppointmentsForDoctor();
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse confirmAppointment(@PathVariable Long id) {
        return appointmentService.confirmAppointment(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse rejectAppointment(@PathVariable Long id) {
        return appointmentService.rejectAppointment(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public AppointmentResponse cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }
}