package com.health.healthcareappointmentbookingsystem.controller;

import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.DoctorResponse;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import com.health.healthcareappointmentbookingsystem.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // GET /api/doctors                         -> همه‌ی دکترها
    // GET /api/doctors?specialization=CARDIOLOGY -> فیلترشده بر اساس تخصص
    @GetMapping
    public List<DoctorResponse> getAllDoctors(@RequestParam(required = false) Specialization specialization) {
        if (specialization != null) {
            return doctorService.getDoctorsBySpecialization(specialization);
        }
        return doctorService.getAllDoctors();
    }

    // GET /api/doctors/5 -> یه دکتر خاص
    @GetMapping("/{id}")
    public DoctorResponse getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }
}