package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.RegisterRequest;
import com.health.healthcareappointmentbookingsystem.entity.Role;
import com.health.healthcareappointmentbookingsystem.entity.User;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import com.health.healthcareappointmentbookingsystem.repository.PatientRepository;
import com.health.healthcareappointmentbookingsystem.repository.UserRepository;
import com.health.healthcareappointmentbookingsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest patientRequest;

    @BeforeEach
    void setUp() {
        patientRequest = new RegisterRequest();
        patientRequest.setFullName("Sara Ahmadi");
        patientRequest.setEmail("sara@example.com");
        patientRequest.setPassword("password123");
        patientRequest.setRole(Role.PATIENT);
        patientRequest.setDateOfBirth(LocalDate.of(2000, 1, 1));
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("sara@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(patientRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_throwsException_whenRoleIsAdmin() {
        patientRequest.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(any())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(patientRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be self-registered");
    }

    @Test
    void register_succeeds_forNewPatient() {
        when(userRepository.existsByEmail("sara@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        var response = authService.register(patientRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("sara@example.com");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);
    }
}