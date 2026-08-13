package com.health.healthcareappointmentbookingsystem.dto.dtoRequest;

import com.health.healthcareappointmentbookingsystem.entity.Role;
import com.health.healthcareappointmentbookingsystem.entity.Specialization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private String phoneNumber;

 // for patient
    private LocalDate dateOfBirth;
// for doctor
    private Specialization specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String hospitalOrClinic;
}
