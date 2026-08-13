package com.health.healthcareappointmentbookingsystem.dto.dtoRequest;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotBlank(message = "Doctor Id is required ")
    private String doctorId;


    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDateTime;

    private String reasonForVisit;
}
