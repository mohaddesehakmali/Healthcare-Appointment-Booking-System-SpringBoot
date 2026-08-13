package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.LoginRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.RegisterRequest;
import com.health.healthcareappointmentbookingsystem.dto.dtoResponse.AuthResponse;
import com.health.healthcareappointmentbookingsystem.entity.Doctor;
import com.health.healthcareappointmentbookingsystem.entity.Patient;
import com.health.healthcareappointmentbookingsystem.entity.Role;
import com.health.healthcareappointmentbookingsystem.entity.User;
import com.health.healthcareappointmentbookingsystem.mapper.UserMapper;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import com.health.healthcareappointmentbookingsystem.repository.PatientRepository;
import com.health.healthcareappointmentbookingsystem.repository.UserRepository;
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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }

        if (request.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Admin accounts cannot be self-registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (request.getRole() == Role.PATIENT) {
            Patient patient = UserMapper.toPatientEntity(request, user);
            patientRepository.save(patient);
        } else if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = UserMapper.toDoctorEntity(request, user);
            doctorRepository.save(doctor);
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}