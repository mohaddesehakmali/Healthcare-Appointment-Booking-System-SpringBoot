package com.health.healthcareappointmentbookingsystem.controller;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AvailabilityRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.TimeSlotResponse;
import com.health.healthcareappointmentbookingsystem.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> setAvailability(@Valid @RequestBody AvailabilityRequest request) {
        availabilityService.setAvailability(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{doctorId}/slots")
    public List<TimeSlotResponse> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availabilityService.getAvailableSlots(doctorId, date);
    }
}