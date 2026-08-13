package com.health.healthcareappointmentbookingsystem.service;

import com.health.healthcareappointmentbookingsystem.dto.dtoRequest.AppointmentRequest;
import com.health.healthcareappointmentbookingsystem.entity.*;
import com.health.healthcareappointmentbookingsystem.repository.AppointmentRepository;
import com.health.healthcareappointmentbookingsystem.repository.DoctorRepository;
import com.health.healthcareappointmentbookingsystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User currentUser;
    private Patient youngPatient;
    private Patient adultPatient;
    private Doctor doctor;
    private AppointmentRequest request;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("patient@example.com").role(Role.PATIENT).build();

        // Fake a logged-in user (this is what JwtAuthenticationFilter would normally set)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())
        );

        youngPatient = new Patient();
        youngPatient.setId(10L);
        youngPatient.setFullName("Kid Patient");
        youngPatient.setDateOfBirth(LocalDate.now().minusYears(10)); // 10 years old

        adultPatient = new Patient();
        adultPatient.setId(11L);
        adultPatient.setFullName("Adult Patient");
        adultPatient.setDateOfBirth(LocalDate.now().minusYears(30)); // 30 years old

        doctor = new Doctor();
        doctor.setId(20L);
        doctor.setFullName("Dr. Smith");
        doctor.setSpecialization(Specialization.CARDIOLOGY);

        request = new AppointmentRequest();
        request.setDoctorId(20L);
        request.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        request.setReasonForVisit("Checkup");
    }

    @Test
    void bookAppointment_throwsException_whenPatientUnder13() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(youngPatient));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("under 13");
    }

    @Test
    void bookAppointment_succeeds_whenPatientIsAdult() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(adultPatient));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        var response = appointmentService.bookAppointment(request);

        assertThat(response.getPatientName()).isEqualTo("Adult Patient");
        assertThat(response.getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void bookAppointment_throwsException_whenDoctorNotFound() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(adultPatient));
        when(doctorRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Doctor not found");
    }
}