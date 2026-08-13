package com.health.healthcareappointmentbookingsystem.dto.dtoResponse;

import com.health.healthcareappointmentbookingsystem.entity.AppointmentStatus;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String patientName;
    private String doctorName;
    private Specialization specialization;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String notes;
}