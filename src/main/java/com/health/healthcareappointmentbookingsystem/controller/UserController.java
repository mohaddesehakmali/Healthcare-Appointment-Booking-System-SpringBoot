package com.health.healthcareappointmentbookingsystem.controller;

import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.DoctorResponse;
import com.health.healthcareappointmentbookingsystem.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DoctorService doctorService;

    @Autowired
    public UserController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/{id}")
    public DoctorResponse showDoctor(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }
}