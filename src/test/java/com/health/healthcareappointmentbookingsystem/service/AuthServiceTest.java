package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.RegisterRequest;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.repository.*;
import com.health.healthcareappointmentbookingsystem.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock PatientRepository patients;
    @Mock DoctorRepository doctors;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager manager;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    RegisterRequest patientRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFullName("Jane"); r.setEmail("jane@test.com"); r.setPassword("password123");
        r.setRole(Role.PATIENT); r.setDateOfBirth(LocalDate.of(2000,1,1));
        r.setNationalId("1234567890"); r.setGender("Female");
        return r;
    }

    @Test void rejectsAdministratorRegistration() {
        var request = patientRequest();
        request.setRole(Role.ADMIN);
        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("cannot be self-registered");
    }

    @Test void rejectsMissingPatientDetails() {
        var request = patientRequest();
        request.setGender(null);
        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void duplicateEmailIsRejected() {
        var request = patientRequest();
        when(users.existsByEmail(request.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already registered");
    }

    @Test void registrationEncodesPasswordAndCreatesPatient() {
        var request = patientRequest();
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(users.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0); u.setId(1L); return u;
        });
        when(jwt.generateToken(any(User.class))).thenReturn("test-token");
        var result = service.register(request);
        assertThat(result.getToken()).isEqualTo("test-token");
        assertThat(result.getEmail()).isEqualTo("jane@test.com");
        verify(patients).save(argThat(p -> p.getNationalId().equals("1234567890") &&
                p.getDateOfBirth().equals(LocalDate.of(2000,1,1))));
    }
}
