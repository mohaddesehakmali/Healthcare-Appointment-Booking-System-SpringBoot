package com.health.healthcareappointmentbookingsystem.dto.dtoResponse;

import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String email;
    private Specialization specialization;
    private Integer yearsOfExperience;
    private String hospitalOrClinic;
}