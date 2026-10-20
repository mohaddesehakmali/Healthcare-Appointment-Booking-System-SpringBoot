package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.LoginRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.RegisterRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AuthResponse;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.mapper.UserMapper;
import com.health.healthcareappointmentbookingsystem.repository.*;
import com.health.healthcareappointmentbookingsystem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) throw new IllegalStateException("Admin accounts cannot be self-registered");
        if (request.getRole() == Role.PATIENT) {
            if (request.getDateOfBirth() == null || request.getGender() == null ||
                request.getGender().isBlank() || request.getNationalId() == null ||
                request.getNationalId().isBlank()) {
                throw new IllegalArgumentException("Patient birth date, gender and national ID are required");
            }
        } else if (request.getRole() == Role.DOCTOR) {
            if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank() ||
                request.getSpecialization() == null || request.getLicenseNumber() == null ||
                request.getLicenseNumber().isBlank() || request.getHospitalOrClinic() == null ||
                request.getHospitalOrClinic().isBlank() || request.getYearsOfExperience() == null ||
                request.getYearsOfExperience() < 0) {
                throw new IllegalArgumentException("Doctor phone, specialization, license, hospital and experience are required");
            }
        } else throw new IllegalArgumentException("Invalid role");
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }
        User user = User.builder().email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()).enabled(true).build();
        user = userRepository.save(user);
        if (request.getRole() == Role.PATIENT) patientRepository.save(UserMapper.toPatientEntity(request, user));
        else doctorRepository.save(UserMapper.toDoctorEntity(request, user));
        return AuthResponse.builder().token(jwtService.generateToken(user))
                .email(user.getEmail()).role(user.getRole()).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
        return AuthResponse.builder().token(jwtService.generateToken(user))
                .email(user.getEmail()).role(user.getRole()).build();
    }
}
