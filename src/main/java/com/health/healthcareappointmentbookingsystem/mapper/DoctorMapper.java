package com.health.healthcareappointmentbookingsystem.mapper;

import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.DoctorResponse;
import com.health.healthcareappointmentbookingsystem.entity.Doctor;

public class DoctorMapper {

    public static DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .email(doctor.getEmail())
                .specialization(doctor.getSpecialization())
                .yearsOfExperience(Integer.valueOf(doctor.getYearsOfExperience()))
                .hospitalOrClinic(doctor.getHospital())
                .build();
    }
}