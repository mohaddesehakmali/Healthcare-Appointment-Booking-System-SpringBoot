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
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @NotNull
    private Role role;

    private String phoneNumber;

    // Required only for patient registration
    private LocalDate dateOfBirth;
    private String gender;
    private String nationalId;

    // Required only for doctor registration
    private Specialization specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String hospitalOrClinic;
}
